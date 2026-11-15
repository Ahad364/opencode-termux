package com.dualmode.keyboard.ime

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator
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
        state.ctrlActive = false
        state.shift = KeyboardState.ShiftState.OFF
        state.language = settings.defaultLanguage
        state.page = EditorHandler.initialPage(profile)
        controller?.refresh()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

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
                } else {
                    ic?.commitText(key.text, 1)
                    state.consumeShiftAfterText()
                }
            }
            KeyAction.SPACE -> {
                ic?.commitText(" ", 1)
                state.consumeShiftAfterText()
                consumeCtrl()
            }
            KeyAction.BACKSPACE -> {
                backspace(ic)
                consumeCtrl()
            }
            KeyAction.ENTER -> {
                EditorHandler.handleEnter(profile, ic)
                consumeCtrl()
            }
            KeyAction.SHIFT -> state.toggleShift()
            KeyAction.OPEN_NUMBERS -> state.openPage(KbPage.NUMBERS)
            KeyAction.OPEN_SYMBOLS -> state.openPage(KbPage.SYMBOLS)
            KeyAction.OPEN_EMOJI -> state.openPage(KbPage.EMOJI)
            KeyAction.BACK_TO_LETTERS -> state.openPage(KbPage.LETTERS)
            KeyAction.SWITCH_SYMBOL_PAGE -> state.openPage(KbPage.SYMBOLS)
            KeyAction.LANGUAGE_SWITCH -> state.toggleLanguage()
            KeyAction.CTRL_TOGGLE -> state.toggleCtrl()
            // Reserved actions kept for API stability; no layout emits them
            // since the dedicated shortcut keyboard was removed.
            KeyAction.MODE_SWITCH, KeyAction.SHORTCUT -> {}
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
        else -> if (ch.isLetter()) "KEYCODE_${ch.uppercaseChar()}" else null
    }

    /** One-shot semantics: Ctrl deactivates after being applied to a key. */
    private fun consumeCtrl() {
        if (state.ctrlActive) state.toggleCtrl()
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
