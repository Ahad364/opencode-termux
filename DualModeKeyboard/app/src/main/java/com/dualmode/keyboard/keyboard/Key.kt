package com.dualmode.keyboard.keyboard

import com.dualmode.keyboard.shortcuts.ShortcutId

/** What happens when the user presses a key. */
enum class KeyAction {
    TEXT,
    BACKSPACE,
    ENTER,
    SPACE,
    SHIFT,
    OPEN_NUMBERS,
    OPEN_SYMBOLS,
    OPEN_EMOJI,
    BACK_TO_LETTERS,
    SWITCH_SYMBOL_PAGE,
    LANGUAGE_SWITCH,
    MODE_SWITCH,
    CTRL_TOGGLE,
    SHORTCUT
}

/**
 * A single key on the keyboard. [label] is what is drawn; [text] is what is
 * committed when [action] is [KeyAction.TEXT].
 */
data class Key(
    val label: String,
    val action: KeyAction = KeyAction.TEXT,
    val text: String = label,
    /** Relative width within its row. Rows are normalized, so any scale works. */
    val weight: Float = 1f,
    /** Function keys get the darker background and accent-free styling. */
    val isFunction: Boolean = false,
    /** Characters shown in a long-press popup. */
    val alternates: List<String> = emptyList(),
    /** For [KeyAction.SHORTCUT] keys. */
    val shortcut: ShortcutId? = null,
    /** Small secondary label drawn under [label] (used on shortcut keys). */
    val subLabel: String? = null
) {
    val hasAlternates: Boolean get() = alternates.isNotEmpty()
}
