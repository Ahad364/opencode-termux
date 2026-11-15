package com.dualmode.keyboard.shortcuts

import android.view.inputmethod.InputConnection

/**
 * Formatting and document shortcuts (Bold, Italic, Center, Save, Print, Open,
 * New, Find). Android offers no IME API to style text or trigger document
 * commands in arbitrary apps, so these are sent as real Ctrl key events and
 * honored only by apps that implement them.
 */
object FormattingHandler {

    fun bold(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.BOLD)

    fun italic(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.ITALIC)

    fun centerAlign(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.CENTER)

    fun save(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.SAVE)

    fun print(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.PRINT)

    fun open(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.OPEN)

    fun newDocument(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.NEW_DOC)

    fun find(ic: InputConnection?): Boolean = KeyEventSender.sendCtrlCombo(ic, ShortcutId.FIND)
}
