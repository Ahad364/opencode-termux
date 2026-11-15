package com.dualmode.keyboard.ime

import com.dualmode.keyboard.data.KeyboardMode
import com.dualmode.keyboard.data.Language
import com.dualmode.keyboard.keyboard.KeyboardLayouts

/**
 * Mutable UI state of the keyboard while it is open. The IME service owns one
 * instance and rebuilds the current view whenever [onChanged] fires.
 */
class KeyboardState {

    enum class ShiftState { OFF, ON, LOCKED }

    var mode: KeyboardMode = KeyboardMode.NORMAL
    var page: KeyboardLayouts.Page = KeyboardLayouts.Page.LETTERS
    var language: Language = Language.ENGLISH
    var shift: ShiftState = ShiftState.OFF
    var ctrlActive: Boolean = false

    /** Password/sensitive editors get no long-press popups and no clipboard extras. */
    var disablePopups: Boolean = false

    var onChanged: (() -> Unit)? = null

    var isShifted: Boolean get() = shift != ShiftState.OFF

    /** The page currently displayed (SHORTCUTS = the Ctrl shortcut keyboard). */
    fun currentPage(): KeyboardLayouts.Page = page

    /** Shift latches back to OFF after typing one character (unless locked). */
    fun consumeShiftAfterText() {
        if (shift == ShiftState.ON) {
            shift = ShiftState.OFF
            onChanged?.invoke()
        }
    }

    fun toggleShift() {
        shift = when (shift) {
            ShiftState.OFF -> ShiftState.ON
            ShiftState.ON -> ShiftState.LOCKED
            ShiftState.LOCKED -> ShiftState.OFF
        }
        onChanged?.invoke()
    }

    fun setPage(p: KeyboardLayouts.Page) {
        page = p
        shift = ShiftState.OFF
        onChanged?.invoke()
    }

    fun toggleLanguage() {
        language = if (language == Language.ENGLISH) Language.BENGALI else Language.ENGLISH
        page = KeyboardLayouts.Page.LETTERS
        shift = ShiftState.OFF
        onChanged?.invoke()
    }

    fun toggleCtrl() {
        ctrlActive = !ctrlActive
        onChanged?.invoke()
    }

    fun reset() {
        page = KeyboardLayouts.Page.LETTERS
        shift = ShiftState.OFF
        ctrlActive = false
        onChanged?.invoke()
    }
}
