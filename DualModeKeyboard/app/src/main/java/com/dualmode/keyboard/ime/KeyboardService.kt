package com.dualmode.keyboard.ime

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import com.dualmode.keyboard.shortcuts.KeyEventSender
import android.widget.Toast
import com.dualmode.keyboard.KeyboardApplication
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.data.SettingsDefaults
import com.dualmode.keyboard.keyboard.Key
import com.dualmode.keyboard.keyboard.KeyAction
import com.dualmode.keyboard.keyboard.KeyboardLayouts.Page as KbPage
import com.dualmode.keyboard.keyboard.KeyboardView
import com.dualmode.keyboard.shortcuts.ClipboardHandler
import com.dualmode.keyboard.shortcuts.ShortcutExecutor
import com.dualmode.keyboard.shortcuts.ShortcutId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

/**
 * The real Android input method. Registered in AndroidManifest.xml with
 * BIND_INPUT_METHOD and res/xml/method.xml (subtypes en_US and bn_BD).
 *
 * Privacy: this service only ever writes to or reads from the currently
 * focused editor's InputConnection. It never logs, stores, or transmits
 * typed content.
 */
class KeyboardService : InputMethodService(), KeyboardView.Listener {

    private val app: KeyboardApplication
        get() = application as KeyboardApplication

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var controller: KeyboardController? = null
    private val state = KeyboardState()
    private var settings: KeyboardSettings = SettingsDefaults.SETTINGS
    private var profile: EditorHandler.EditorProfile = EditorHandler.analyze(null)

    private lateinit var clipboardHandler: ClipboardHandler
    private lateinit var shortcutExecutor: ShortcutExecutor
    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        clipboardHandler = ClipboardHandler(this)
        shortcutExecutor = ShortcutExecutor(clipboardHandler)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

        // Observe settings; the latest value is available synchronously when
        // the view is created, and live changes re-render the keyboard.
        val settingsFlow = app.settingsRepository.settingsFlow
        scope.launch {
            val live = settingsFlow.stateIn(
                scope, SharingStarted.Eagerly, SettingsDefaults.SETTINGS
            )
            settings = live.value
            live.collect {
                settings = it
                controller?.let { c -> c.settings = it }
            }
        }
    }

    override fun onCreateInputView(): View {
        state.language = settings.defaultLanguage
        state.onChanged = { controller?.refresh() }
        val controller = KeyboardController(
            context = this,
            state = state,
            viewListener = this
        )
        controller.settings = settings
        this.controller = controller
        return controller.buildRoot()
    }

    override fun onStartInputView(editorInfo: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        profile = EditorHandler.analyze(editorInfo)
        state.disablePopups = profile.type == EditorHandler.FieldType.PASSWORD
        releaseHeldModifiers()
        state.shift = KeyboardState.ShiftState.OFF
        state.language = settings.defaultLanguage
        state.page = EditorHandler.initialPage(profile)
        controller?.refresh()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onFinishInputView(finishingInput: Boolean) {
        // Never leave the editor believing Ctrl is still held.
        releaseHeldModifiers()
        super.onFinishInputView(finishingInput)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        releaseHeldModifiers()
    }

    // ------------------------------------------------------- key dispatch

    override fun onKey(key: Key) {
        feedback()
        val ic: InputConnection? = currentInputConnection
        when (key.action) {
            KeyAction.TEXT -> {
                if (state.ctrlActive) {
                    // Sticky one-shot Ctrl: apply the modifier to this key,
                    // then reset. Known letters go through the shortcut
                    // handlers (direct clipboard ops or Ctrl key events).
                    runCtrlCombo(ic, key.text)
                    consumeCtrl()
                } else if (state.altActive) {
                    // Sticky one-shot Alt: send a real Alt key event (apps
                    // decide whether to act on it).
                    runAltCombo(ic, key.text)
                    consumeAlt()
                } else if (state.fnActive) {
                    // Sticky one-shot Fn: digits become F1–F10, anything else
                    // types normally.
                    runFnCombo(ic, key.text)
                    consumeFn()
                } else {
                    ic?.commitText(key.text, 1)
                    state.consumeShiftAfterText()
                }
            }
            KeyAction.SPACE -> {
                ic?.commitText(" ", 1)
                state.consumeShiftAfterText()
                consumeCtrl()
                consumeAlt()
                consumeFn()
            }
            KeyAction.BACKSPACE -> {
                backspace(ic)
                consumeCtrl()
                consumeAlt()
                consumeFn()
            }
            KeyAction.ENTER -> {
                EditorHandler.handleEnter(profile, ic)
                consumeCtrl()
                consumeAlt()
                consumeFn()
            }
            KeyAction.SHIFT -> state.toggleShift()
            KeyAction.TAB -> {
                // CodeBoard-style: real Tab key event (indent in code editors,
                // focus move in apps that use it) with current modifiers.
                KeyEventSender.sendKeyCode(ic, "KEYCODE_TAB", heldMetaState())
                state.consumeShiftAfterText()
                consumeCtrl()
            }
            KeyAction.OPEN_NUMBERS -> state.openPage(KbPage.NUMBERS)
            KeyAction.OPEN_SYMBOLS -> state.openPage(KbPage.SYMBOLS)
            KeyAction.OPEN_EMOJI -> state.openPage(KbPage.EMOJI)
            KeyAction.BACK_TO_LETTERS -> state.openPage(KbPage.LETTERS)
            KeyAction.SWITCH_SYMBOL_PAGE -> state.openPage(KbPage.SYMBOLS)
            KeyAction.LANGUAGE_SWITCH -> state.toggleLanguage()
            KeyAction.CTRL_TOGGLE -> {
                state.toggleCtrl()
                // CodeBoard-style held modifier: the editor physically sees
                // Ctrl pressed while sticky, and released when it resets.
                currentInputConnection?.sendKeyEvent(
                    KeyEvent(
                        if (state.ctrlActive) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_CTRL_LEFT
                    )
                )
            }
            KeyAction.CODE_TOGGLE -> state.openPage(
                if (state.page == KbPage.CODE) KbPage.LETTERS else KbPage.CODE
            )
            KeyAction.ALT_TOGGLE -> state.toggleAlt()
            KeyAction.FN_TOGGLE -> state.toggleFn()
            KeyAction.ESC -> {
                if (!KeyEventSender.sendKeyCode(ic, "KEYCODE_ESCAPE", heldMetaState())) {
                    toast("Editor did not accept ESC")
                }
                state.consumeShiftAfterText()
                consumeCtrl()
            }
            KeyAction.ARROW_LEFT -> arrow(ic, "KEYCODE_DPAD_LEFT", "KEYCODE_MOVE_HOME")
            KeyAction.ARROW_UP -> arrow(ic, "KEYCODE_DPAD_UP", "KEYCODE_PAGE_UP")
            KeyAction.ARROW_DOWN -> arrow(ic, "KEYCODE_DPAD_DOWN", "KEYCODE_PAGE_DOWN")
            KeyAction.ARROW_RIGHT -> arrow(ic, "KEYCODE_DPAD_RIGHT", "KEYCODE_MOVE_END")
            KeyAction.SHORTCUT -> key.shortcut?.let {
                toast(shortcutExecutor.execute(ic, it).message)
                consumeCtrl()
                consumeAlt()
                consumeFn()
            }
            // Reserved action kept for API stability; no layout emits it
            // since the dedicated shortcut keyboard was removed.
            KeyAction.MODE_SWITCH -> {}
        }
    }

    /**
     * Applies Ctrl to a single key press. Letters with a known shortcut
     * (Ctrl+C, Ctrl+V, Ctrl+Z…) run through [ShortcutExecutor], which performs
     * direct InputConnection work for clipboard actions and sends real Ctrl
     * key events for everything else. Unknown letters get a generic Ctrl
     * key event (e.g. Ctrl+D) — apps decide whether to act on it.
     */
    private fun runCtrlCombo(ic: InputConnection?, text: String) {
        val ch = text.firstOrNull() ?: return
        val shortcut = ShortcutId.fromChar(ch)
        if (shortcut != null) {
            toast(shortcutExecutor.execute(ic, shortcut).message)
            return
        }
        val codeName = ctrlKeyCodeName(ch)
        if (codeName != null && KeyEventSender.sendCtrlKeyCode(ic, codeName)) {
            val display = if (ch == '\t') "Tab" else ch.uppercase()
            toast("Ctrl+$display sent — depends on the app")
        }
        // If no meaningful Ctrl combination exists, Ctrl simply resets.
    }

    private fun ctrlKeyCodeName(ch: Char): String? = when (ch) {
        '\t' -> "KEYCODE_TAB"
        ' ' -> "KEYCODE_SPACE"
        ',' -> "KEYCODE_COMMA"
        '.' -> "KEYCODE_PERIOD"
        '-' -> "KEYCODE_MINUS"
        '=' -> "KEYCODE_EQUALS"
        '[' -> "KEYCODE_LEFT_BRACKET"
        ']' -> "KEYCODE_RIGHT_BRACKET"
        '\\' -> "KEYCODE_BACKSLASH"
        ';' -> "KEYCODE_SEMICOLON"
        '\'' -> "KEYCODE_APOSTROPHE"
        '/' -> "KEYCODE_SLASH"
        '@' -> "KEYCODE_AT"
        '`' -> "KEYCODE_GRAVE"
        else -> if (ch.isLetter()) "KEYCODE_${ch.uppercaseChar()}" else null
    }

    /**
     * Sticky one-shot Alt: sends a real Alt key event for the pressed key.
     * Delivery is confirmed; the effect depends on the focused app.
     */
    private fun runAltCombo(ic: InputConnection?, text: String) {
        val ch = text.firstOrNull() ?: return
        val codeName = ctrlKeyCodeName(ch) ?: return
        if (KeyEventSender.sendKeyCode(
                ic, codeName, KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
            )
        ) {
            toast("Alt+$ch sent — depends on the app")
        }
    }

    /**
     * Sticky one-shot Fn: digits become F1–F10 function key events. Any other
     * key types normally (Fn simply resets with the next press).
     */
    private fun runFnCombo(ic: InputConnection?, text: String) {
        val fKey = fnKeyCodeName(text.firstOrNull())
        if (fKey != null) {
            KeyEventSender.sendKeyCode(ic, fKey)
            return
        }
        ic?.commitText(text, 1)
        state.consumeShiftAfterText()
    }

    private fun fnKeyCodeName(ch: Char?): String? = when (ch) {
        '1' -> "KEYCODE_F1"
        '2' -> "KEYCODE_F2"
        '3' -> "KEYCODE_F3"
        '4' -> "KEYCODE_F4"
        '5' -> "KEYCODE_F5"
        '6' -> "KEYCODE_F6"
        '7' -> "KEYCODE_F7"
        '8' -> "KEYCODE_F8"
        '9' -> "KEYCODE_F9"
        '0' -> "KEYCODE_F10"
        else -> null
    }

    /**
     * Current modifier flags for key events (CodeBoard-style meta
     * composition): Shift selects, Ctrl combines — e.g. Shift+arrow extends
     * the selection, Shift+Tab outdents.
     */
    private fun heldMetaState(): Int {
        var meta = 0
        if (state.isShifted) meta = meta or KeyEvent.META_SHIFT_LEFT_ON
        if (state.ctrlActive) meta = meta or KeyEvent.META_CTRL_ON
        return meta
    }

    /**
     * Arrows move the cursor with DPAD key events; while Fn is active they
     * jump Home/End/PageUp/PageDown instead (laptop-style). Shift+arrow
     * extends the selection (CodeBoard behavior); modifiers reset after.
     */
    private fun arrow(ic: InputConnection?, plain: String, withFn: String) {
        val fnHeld = state.fnActive
        if (!KeyEventSender.sendKeyCode(ic, if (fnHeld) withFn else plain, heldMetaState())) {
            toast("No editor to navigate")
        }
        if (fnHeld) consumeFn()
        state.consumeShiftAfterText()
        consumeCtrl()
    }

    /** One-shot semantics: Ctrl deactivates after being applied to a key. */
    private fun consumeCtrl() {
        if (state.ctrlActive) {
            state.toggleCtrl()
            // CodeBoard-style: release the held Ctrl key event in the editor.
            currentInputConnection?.sendKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT)
            )
        }
    }

    /**
     * Releases every held modifier state (CodeBoard releases its modifiers
     * when the keyboard view regenerates or the input changes): the held Ctrl
     * key event is released so no editor is left believing Ctrl is pressed,
     * and all sticky flags reset.
     */
    private fun releaseHeldModifiers() {
        if (state.ctrlActive) {
            currentInputConnection?.sendKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT)
            )
            state.ctrlActive = false
        }
        state.altActive = false
        state.fnActive = false
    }

    private fun consumeAlt() {
        if (state.altActive) state.toggleAlt()
    }

    private fun consumeFn() {
        if (state.fnActive) state.toggleFn()
    }

    private fun backspace(ic: InputConnection?) {
        if (ic == null) return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
        state.consumeShiftAfterText()
    }

    private fun toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    // -------------------------------------------------------- feedback

    private fun feedback() {
        if (settings.soundEnabled) {
            val volume = (settings.soundVolume / 100f).coerceIn(0f, 1f)
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, volume)
        }
        if (settings.hapticsEnabled) {
            vibrator?.let { v ->
                val amplitude = (1 + (settings.hapticStrength * 254 / 100)).coerceIn(1, 255)
                v.vibrate(VibrationEffect.createOneShot(18, amplitude))
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        controller = null
        super.onDestroy()
    }
}
