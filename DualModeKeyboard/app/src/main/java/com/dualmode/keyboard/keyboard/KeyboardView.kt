package com.dualmode.keyboard.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.themes.KeyboardTheme
import com.dualmode.keyboard.themes.Themes
import com.dualmode.keyboard.utils.dp
import com.dualmode.keyboard.utils.sp
import kotlin.math.abs
import kotlin.math.min

/**
 * Premium keyboard renderer. Draws all keys on a Canvas with rounded corners,
 * soft shadows, subtle vertical gradients and a fast press highlight.
 *
 * Performance notes:
 * - Plain characters commit on ACTION_DOWN for instant typing feedback.
 * - Keys with long-press alternates commit on ACTION_UP so the popup works.
 * - invalidate() is called only on state changes, never per frame.
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface Listener {
        fun onKey(key: Key)
    }

    private data class KeyRect(val key: Key, val rect: RectF)

    private class Popup(
        val source: Key,
        val keys: List<Key>,
        val rects: List<RectF>,
        val bounds: RectF
    ) {
        var selectedIndex: Int? = null
    }

    var listener: Listener? = null

    var rows: List<List<Key>> = emptyList()
        set(value) {
            field = value
            geometryDirty = true
            requestLayout()
            invalidate()
        }

    var ctrlActive: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /** Sensitive editors (passwords) disable long-press popups. */
    var popupsEnabled: Boolean = true

    // ---- metrics (set from settings, in px) ----
    private var keyHeightPx = 54f.dp(context)
    private var spacingPx = 4f.dp(context)
    private var radiusPx = 14f.dp(context)
    private var fontSizePx = 18f.sp(context)
    private val edgePaddingPx = 3f.dp(context)
    private val shadowOffsetPx = 2f.dp(context)
    private val popupHeightPx = 46f.dp(context)

    private var theme: KeyboardTheme = Themes.PREMIUM_DARK

    private var geometryDirty = true
    private var keyRects: List<KeyRect> = emptyList()

    private var pressedKey: Key? = null
    private var pressedRect: RectF? = null
    private var pressedIsShortcut = false
    private var popup: Popup? = null

    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f.dp(context)
    }
    private var keyShader: Shader? = null
    private var shaderHeight = 0f

    private val handler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var repeatingBackspace = false

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (!repeatingBackspace) return
            fire(BACKSPACE_KEY)
            handler.postDelayed(this, REPEAT_INTERVAL_MS)
        }
    }

    private val popupRunnable = Runnable {
        val rect = pressedRect ?: return@Runnable
        val key = pressedKey ?: return@Runnable
        if (!key.hasAlternates || !popupsEnabled) return@Runnable
        showPopup(key, rect)
    }

    private val BACKSPACE_KEY = Key("⌫", KeyAction.BACKSPACE, isFunction = true)

    init {
        applyTypefaces()
    }

    private fun applyTypefaces() {
        mainPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        subPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    /** Applies all visual settings; call whenever settings change. */
    fun applySettings(settings: KeyboardSettings) {
        theme = Themes.resolve(settings)
        keyHeightPx = settings.keyHeightDp.dp(context)
        spacingPx = settings.keySpacingDp.dp(context)
        radiusPx = settings.keyCornerRadiusDp.dp(context)
        fontSizePx = settings.keyFontSizeSp.sp(context)
        mainPaint.color = theme.keyText
        subPaint.color = theme.accent
        bgPaint.color = theme.keyboardBackground
        geometryDirty = true
        requestLayout()
        invalidate()
    }

    // ------------------------------------------------------------ measuring

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val rowCount = rows.size.coerceAtLeast(1)
        val height = (edgePaddingPx * 2 + rowCount * keyHeightPx +
            spacingPx * (rowCount + 1) + shadowOffsetPx).toInt()
        setMeasuredDimension(width, View.resolveSize(height, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        geometryDirty = true
    }

    private fun ensureGeometry(width: Int) {
        if (!geometryDirty && keyRects.isNotEmpty()) return
        val newRects = ArrayList<KeyRect>(rows.sumOf { it.size })
        for (row in rows) {
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val usable = width - edgePaddingPx * 2 - spacingPx * (row.size - 1)
            var x = edgePaddingPx
            for (key in row) {
                val keyWidth = usable * (key.weight / totalWeight)
                val rect = RectF(x, 0f, x + keyWidth, 0f)
                newRects.add(KeyRect(key, rect))
                x += keyWidth + spacingPx
            }
        }
        keyRects = newRects
        geometryDirty = false
    }

    // -------------------------------------------------------------- drawing

    override fun onDraw(canvas: Canvas) {
        val w = width
        val h = height
        canvas.drawColor(theme.keyboardBackground)
        ensureGeometry(w)

        // Vertically distribute rows.
        var y = edgePaddingPx + spacingPx
        var index = 0
        for (row in rows) {
            repeat(row.size) {
                val kr = keyRects[index++]
                val rect = kr.rect
                rect.top = y
                rect.bottom = y + keyHeightPx
            }
            y += keyHeightPx + spacingPx
        }

        for (kr in keyRects) {
            drawKey(canvas, kr.key, kr.rect)
        }
        popup?.let { drawPopup(canvas, it) }
    }

    private fun drawKey(canvas: Canvas, key: Key, rect: RectF) {
        val isPressed = key === pressedKey && popup == null
        val isCtrlToggle = key.action == KeyAction.CTRL_TOGGLE
        val ctrlOn = isCtrlToggle && ctrlActive
        val isShortcut = key.action == KeyAction.SHORTCUT

        val drawRect = RectF(rect)
        if (isPressed) {
            val inset = keyHeightPx * 0.06f
            drawRect.inset(inset, inset)
        }

        // Soft shadow (skipped for pressed keys: they "sink").
        if (!isPressed) {
            shadowPaint.color = theme.shadowColor
            val shadow = RectF(drawRect)
            shadow.offset(0f, shadowOffsetPx)
            canvas.drawRoundRect(shadow, radiusPx, radiusPx, shadowPaint)
        }

        when {
            ctrlOn -> {
                keyPaint.shader = null
                keyPaint.color = theme.accent
            }
            isPressed -> {
                keyPaint.shader = null
                keyPaint.color = if (isCtrlToggle) theme.accent else theme.keyBackgroundPressed
            }
            key.isFunction -> {
                keyPaint.shader = null
                keyPaint.color = theme.functionKeyBackground
            }
            else -> {
                keyPaint.shader = gradientFor(keyHeightPx)
                keyPaint.color = theme.keyBackground
            }
        }
        canvas.drawRoundRect(drawRect, radiusPx, radiusPx, keyPaint)

        // Accent outline on pressed keys for a crisp highlight.
        if (isPressed) {
            strokePaint.color = theme.accent
            canvas.drawRoundRect(drawRect, radiusPx, radiusPx, strokePaint)
        }

        // Text.
        val textColor = when {
            ctrlOn -> theme.accentText
            isPressed && isCtrlToggle -> theme.accentText
            key.isFunction -> theme.functionKeyText
            else -> theme.keyText
        }
        mainPaint.color = textColor

        if (isShortcut && key.subLabel != null) {
            val labelSize = fontSizePx * 0.72f
            mainPaint.textSize = fitSize(key, drawRect.width(), labelSize)
            subPaint.textSize = fontSizePx * 0.5f
            val cx = drawRect.centerX()
            val midY = drawRect.centerY()
            val mainBaseline = midY - fontSizePx * 0.18f
            val subBaseline = midY + fontSizePx * 0.62f
            canvas.drawText(key.label, cx, mainBaseline, mainPaint)
            subPaint.color = theme.accent
            canvas.drawText(key.subLabel!!, cx, subBaseline, subPaint)
        } else {
            val size = if (key.isFunction && key.label.length > 1) {
                fontSizePx * 0.78f
            } else {
                fontSizePx
            }
            mainPaint.textSize = fitSize(key, drawRect.width(), size)
            val baseline = drawRect.centerY() - (mainPaint.descent() + mainPaint.ascent()) / 2f
            canvas.drawText(key.label, drawRect.centerX(), baseline, mainPaint)
        }
    }

    private fun fitSize(key: Key, keyWidth: Float, desired: Float): Float {
        mainPaint.textSize = desired
        val text = key.label
        val measured = mainPaint.measureText(text)
        if (measured <= keyWidth * 0.88f) return desired
        return desired * (keyWidth * 0.88f / measured)
    }

    private fun gradientFor(height: Float): Shader {
        val cached = keyShader
        if (cached != null && shaderHeight == height) return cached
        val top = lighten(theme.keyBackground, 1.09f)
        val bottom = theme.keyBackground
        val g = LinearGradient(
            0f, 0f, 0f, height,
            top, bottom, Shader.TileMode.CLAMP
        )
        keyShader = g
        shaderHeight = height
        return g
    }

    private fun lighten(color: Int, factor: Float): Int {
        fun ch(shift: Int): Int = min(255, (((color shr shift) and 0xFF) * factor).toInt())
        return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    // ---------------------------------------------------------------- popup

    private fun showPopup(source: Key, anchor: RectF) {
        val alts = source.alternates
        if (alts.isEmpty()) return
        val count = alts.size
        val itemWidth = maxOf(anchor.width(), (width - spacingPx * (count + 1)) / count)
        val popupWidth = itemWidth * count + spacingPx * (count + 1)
        var left = anchor.centerX() - popupWidth / 2f
        left = left.coerceIn(0f, width - popupWidth)
        val top = (anchor.top - popupHeightPx - spacingPx).coerceAtLeast(spacingPx)
        val keys = alts.map { Key(it, KeyAction.TEXT, text = it) }
        val rects = ArrayList<RectF>(count)
        var x = left + spacingPx
        repeat(count) {
            rects.add(RectF(x, top, x + itemWidth, top + popupHeightPx))
            x += itemWidth + spacingPx
        }
        popup = Popup(source, keys, rects, RectF(left, top, left + popupWidth, top + popupHeightPx))
        invalidate()
    }

    private fun drawPopup(canvas: Canvas, popup: Popup) {
        shadowPaint.color = theme.shadowColor
        val shadow = RectF(popup.bounds)
        shadow.offset(0f, shadowOffsetPx)
        canvas.drawRoundRect(shadow, radiusPx, radiusPx, shadowPaint)
        keyPaint.shader = null
        keyPaint.color = theme.functionKeyBackground
        canvas.drawRoundRect(popup.bounds, radiusPx, radiusPx, keyPaint)

        popup.rects.forEachIndexed { i, r ->
            val selected = popup.selectedIndex == i
            keyPaint.color = if (selected) theme.accent else theme.keyBackground
            canvas.drawRoundRect(r, radiusPx, radiusPx, keyPaint)
            mainPaint.color = if (selected) theme.accentText else theme.keyText
            mainPaint.textSize = fontSizePx * 0.9f
            val baseline = r.centerY() - (mainPaint.descent() + mainPaint.ascent()) / 2f
            canvas.drawText(popup.keys[i].text, r.centerX(), baseline, mainPaint)
        }
    }

    // ---------------------------------------------------------------- touch

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                val kr = rectAt(x, y) ?: return false
                cancelPending()
                pressedKey = kr.key
                pressedRect = kr.rect
                pressedIsShortcut = kr.key.action == KeyAction.SHORTCUT
                when {
                    kr.key.hasAlternates && popupsEnabled -> {
                        handler.postDelayed(popupRunnable, LONG_PRESS_MS)
                    }
                    kr.key.action == KeyAction.BACKSPACE -> {
                        fire(kr.key)
                        repeatingBackspace = true
                        handler.postDelayed(repeatRunnable, REPEAT_DELAY_MS)
                    }
                    else -> fire(kr.key)
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                popup?.let {
                    it.selectedIndex = popupIndexAt(it, x, y)
                    invalidate()
                    return true
                }
                val rect = pressedRect
                if (rect != null &&
                    (abs(x - downX) > touchSlop || abs(y - downY) > touchSlop)
                ) {
                    cancelPress()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val p = popup
                if (p != null && event.actionMasked == MotionEvent.ACTION_UP) {
                    val idx = p.selectedIndex
                    if (idx != null) fire(p.keys[idx])
                    popup = null
                } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                    // Keys with alternates commit on UP so a long-press can open
                    // the popup instead of typing the primary character.
                    val key = pressedKey
                    if (key != null && key.hasAlternates && popupsEnabled && !repeatingBackspace) {
                        fire(key)
                    }
                }
                cancelPress()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun popupIndexAt(popup: Popup, x: Float, y: Float): Int? {
        popup.rects.forEachIndexed { i, r ->
            if (r.contains(x, y)) return i
        }
        return null
    }

    private fun rectAt(x: Float, y: Float): KeyRect? {
        ensureGeometry(width)
        keyRects.forEachIndexed { _, kr ->
            if (kr.rect.contains(x, y)) return kr
        }
        return null
    }

    private fun fire(key: Key) {
        listener?.onKey(key)
    }

    private fun cancelPress() {
        cancelPending()
        pressedKey = null
        pressedRect = null
        pressedIsShortcut = false
        repeatingBackspace = false
        popup = null
        invalidate()
    }

    private fun cancelPending() {
        handler.removeCallbacks(popupRunnable)
        handler.removeCallbacks(repeatRunnable)
    }

    override fun onDetachedFromWindow() {
        cancelPending()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val LONG_PRESS_MS = 350L
        private const val REPEAT_DELAY_MS = 400L
        private const val REPEAT_INTERVAL_MS = 55L
    }
}
