package com.dualmode.keyboard.ime

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.dualmode.keyboard.data.KeyboardMode
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.data.SettingsDefaults
import com.dualmode.keyboard.keyboard.KeyboardLayouts
import com.dualmode.keyboard.keyboard.KeyboardView
import com.dualmode.keyboard.utils.dp

/**
 * Owns the keyboard view hierarchy inside the IME window and handles mode
 * switching (Normal ↔ Ctrl shortcut) with a smooth slide transition that
 * keeps the editor focus untouched.
 */
class KeyboardController(
    private val context: android.content.Context,
    val state: KeyboardState,
    private val viewListener: KeyboardView.Listener,
    private val onModePersisted: (KeyboardMode) -> Unit
) {

    var settings: KeyboardSettings = SettingsDefaults.SETTINGS
        set(value) {
            field = value
            currentView?.let { configure(it) }
        }

    private var container: FrameLayout? = null
    private var currentView: KeyboardView? = null

    /** Builds and returns the root container with the initial mode shown. */
    fun buildRoot(): View {
        val frame = FrameLayout(context)
        container = frame
        show(state.mode, animate = false)
        return frame
    }

    fun show(mode: KeyboardMode, animate: Boolean) {
        val frame = container ?: return
        frame.removeAllViews()
        val view = KeyboardView(context)
        configure(view)
        frame.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        if (animate && settings.animationsEnabled) {
            view.translationY = 80f.dp(context)
            view.animate()
                .translationY(0f)
                .setDuration(170)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        currentView = view
    }

    /** Re-renders the current view from state (called on every state change). */
    fun refresh() {
        currentView?.let { configure(it) }
    }

    fun switchMode() {
        val next = if (state.mode == KeyboardMode.NORMAL) KeyboardMode.SHORTCUT
        else KeyboardMode.NORMAL
        state.mode = next
        state.ctrlActive = false
        state.page = if (next == KeyboardMode.SHORTCUT) {
            KeyboardLayouts.Page.SHORTCUTS
        } else {
            KeyboardLayouts.Page.LETTERS
        }
        state.shift = KeyboardState.ShiftState.OFF
        onModePersisted(next)
        show(next, animate = settings.animationsEnabled)
    }

    private fun configure(view: KeyboardView) {
        view.applySettings(settings)
        view.popupsEnabled = !state.disablePopups
        view.ctrlActive = state.ctrlActive
        view.listener = viewListener
        view.rows = KeyboardLayouts.forState(
            page = state.currentPage(),
            language = state.language,
            shifted = state.isShifted,
            ctrlActive = state.ctrlActive
        )
    }
}
