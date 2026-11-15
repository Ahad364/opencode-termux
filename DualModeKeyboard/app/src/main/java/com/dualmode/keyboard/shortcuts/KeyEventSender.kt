package com.dualmode.keyboard.shortcuts

import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.InputConnection

/**
 * Dispatches real Ctrl key-event combinations (Ctrl+Z, Ctrl+Y …) through the
 * current InputConnection.
 *
 * Honest limitation: an Android keyboard cannot force apps to honor desktop
 * shortcuts. Apps that implement KeyEvent handling with the CTRL meta state
 * (Termux, Google Docs, some editors and office apps) will react; others will
 * ignore the events. The caller must present this as "sent — depends on app"
 * rather than claiming the action succeeded.
 */
object KeyEventSender {

    /**
     * Sends a key press/release pair with the CTRL meta state.
     * @return true if the events were delivered to the InputConnection.
     *         Note: delivery does NOT guarantee the app acted on them.
     */
    fun sendCtrlCombo(ic: InputConnection?, id: ShortcutId): Boolean {
        val codeName = id.keyCodeName ?: return false
        return sendCtrlKeyCode(ic, codeName)
    }

    /**
     * Sends Ctrl + the key named by [keyCodeName] (e.g. "KEYCODE_TAB") through
     * the current InputConnection. Used for shortcuts and generic Ctrl combos.
     */
    fun sendCtrlKeyCode(ic: InputConnection?, keyCodeName: String): Boolean =
        sendKeyCode(
            ic,
            keyCodeName,
            KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        )

    /**
     * Sends a key press/release pair named by [keyCodeName] with the given
     * meta state (0 = no modifier). Used for arrows, ESC, F-keys and Alt/Fn
     * combos on the coding keyboard.
     * @return true if the events were delivered to the InputConnection.
     *         Note: delivery does NOT guarantee the app acted on them.
     */
    fun sendKeyCode(ic: InputConnection?, keyCodeName: String, meta: Int = 0): Boolean {
        if (ic == null) return false
        val keyCode = KeyEvent.keyCodeFromString(keyCodeName)
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) return false

        val downTime = SystemClock.uptimeMillis()
        val down = KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0, meta)
        val up = KeyEvent(downTime, downTime + 10, KeyEvent.ACTION_UP, keyCode, 0, meta)
        val downAccepted = ic.sendKeyEvent(down)
        val upAccepted = ic.sendKeyEvent(up)
        return downAccepted && upAccepted
    }
}
