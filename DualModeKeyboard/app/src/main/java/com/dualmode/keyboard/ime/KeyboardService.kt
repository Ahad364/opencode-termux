package com.dualmode.keyboard.ime

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Toast
import com.dualmode.keyboard.KeyboardApplication
import com.dualmode.keyboard.data.KeyboardMode
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.data.SettingsDefaults
import com.dualmode.keyboard.keyboard.Key
import com.dualmode.keyboard.keyboard.KeyAction
import com.dualmode.keyboard.keyboard.KeyboardLayouts.Page as KbPage
import com.dualmode.keyboard.keyboard.KeyboardView
import com.dualmode.keyboard.shortcuts.ClipboardHandler
import com.dualmode.keyboard.shortcuts.ShortcutExecutor
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
        state.mode = settings.lastMode
        state.language = settings.defaultLanguage
        state.onChanged = { controller?.refresh() }
        val controller = KeyboardController(
            context = this,
            state = state,
            viewListener = this,
            onModePersisted = { mode ->
                scope.launch { app.settingsRepository.setLastMode(mode) }
            }
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
                ic?.commitText(key.text, 1)
                state.consumeShiftAfterText()
            }
            KeyAction.SPACE -> {
                ic?.commitText(" ", 1)
                state.consumeShiftAfterText()
            }
            KeyAction.BACKSPACE -> backspace(ic)
            KeyAction.ENTER -> EditorHandler.handleEnter(profile, ic)
            KeyAction.SHIFT -> state.toggleShift()
            KeyAction.OPEN_NUMBERS -> state.setPage(KbPage.NUMBERS)
            KeyAction.OPEN_SYMBOLS -> state.setPage(KbPage.SYMBOLS)
            KeyAction.OPEN_EMOJI -> state.setPage(KbPage.EMOJI)
            KeyAction.BACK_TO_LETTERS -> state.setPage(
                if (state.mode == KeyboardMode.SHORTCUT) KbPage.SHORTCUTS
                else KbPage.LETTERS
            )
            KeyAction.SWITCH_SYMBOL_PAGE -> state.setPage(KbPage.SYMBOLS)
            KeyAction.LANGUAGE_SWITCH -> state.toggleLanguage()
            KeyAction.MODE_SWITCH -> controller?.switchMode()
            KeyAction.CTRL_TOGGLE -> state.toggleCtrl()
            KeyAction.SHORTCUT -> key.shortcut?.let { runShortcut(ic, it) }
        }
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

    private fun runShortcut(ic: InputConnection?, id: com.dualmode.keyboard.shortcuts.ShortcutId) {
        val result = shortcutExecutor.execute(ic, id)
        toast(result.message)
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
