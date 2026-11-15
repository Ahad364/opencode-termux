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
        if (ic == null) return false
        val codeName = id.keyCodeName ?: return false
        val keyCode = KeyEvent.keyCodeFromString(codeName)
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) return false

        val downTime = SystemClock.uptimeMillis()
        val meta = KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        val down = KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0, meta)
        val upTime = downTime + 10
        val up = KeyEvent(downTime, upTime, KeyEvent.ACTION_UP, keyCode, 0, meta)
        val downAccepted = ic.sendKeyEvent(down)
        val upAccepted = ic.sendKeyEvent(up)
        return downAccepted && upAccepted
    }
}
