package com.dualmode.keyboard.shortcuts

import android.view.inputmethod.InputConnection

/**
 * Text editing shortcuts. Undo/Redo cannot be implemented generically through
 * InputConnection (Android has no IME-side undo API), so these are dispatched
 * as Ctrl+Z / Ctrl+Y key events for apps that support them.
 */
object EditingHandler {

    fun undo(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.UNDO)

    fun redo(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.REDO)
}
