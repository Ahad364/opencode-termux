package com.dualmode.keyboard.keyboard

import com.dualmode.keyboard.data.Language
import com.dualmode.keyboard.shortcuts.ShortcutId

/**
 * All keyboard layouts, built as pure data so they are easy to test and cheap
 * to (re)create. Pages: letters (EN/BN), numbers, symbols, emoji and the
 * dedicated coding keyboard.
 *
 * PC-style compact design: a sticky one-shot Ctrl modifier and a Tab key live
 * directly in the toolbar of every page, so Ctrl+letter shortcuts (Copy,
 * Paste, Undo, Save…) work from the normal typing keyboard. The toolbar also
 * carries the "</>" Coding Keys toggle for the dedicated coding keyboard.
 */
object KeyboardLayouts {

    private val SHIFT_ICON = "⇧"
    private val BACKSPACE_ICON = "⌫"
    private val ENTER_ICON = "⏎"

    /** Main entry: builds the rows for the given state. */
    fun forState(
        page: Page,
        language: Language,
        shifted: Boolean,
        ctrlActive: Boolean
    ): List<List<Key>> = when (page) {
        Page.LETTERS -> when (language) {
            Language.ENGLISH -> english(shifted)
            Language.BENGALI -> bengali(shifted)
        }
        Page.NUMBERS -> numbers(language)
        Page.SYMBOLS -> symbols()
        Page.EMOJI -> emoji()
        Page.CODE -> coding(language)
    }

    // ---------------------------------------------------------------- toolbar

    private fun toolbar(language: Language): List<Key> = listOf(
        Key("Ctrl", KeyAction.CTRL_TOGGLE, isFunction = true, weight = 1.2f),
        Key("</>", KeyAction.CODE_TOGGLE, isFunction = true, weight = 1f),
        Key("Tab", KeyAction.TEXT, text = "\t", isFunction = true, weight = 1f),
        Key("😀", KeyAction.OPEN_EMOJI, isFunction = true, weight = 0.8f),
        Key(
            if (language == Language.ENGLISH) "EN" else "বাং",
            KeyAction.LANGUAGE_SWITCH,
            isFunction = true,
            weight = 0.8f
        ),
        Key("123", KeyAction.OPEN_NUMBERS, isFunction = true, weight = 0.8f)
    )

    // ------------------------------------------------------------ coding keys

    private fun sym(c: Char): Key = Key(label = c.toString(), weight = 0.85f)

    /** PC-style number row: long-press shows the shifted symbol (1 → !…). */
    private fun digit(d: Char, shifted: Char): Key = Key(
        label = d.toString(),
        alternates = listOf(shifted.toString())
    )

    private fun shortcutKey(id: ShortcutId, label: String, weight: Float = 1f): Key = Key(
        label = label,
        action = KeyAction.SHORTCUT,
        shortcut = id,
        subLabel = id.comboLabel,
        weight = weight
    )

    /**
     * The dedicated coding keyboard: modifier keys (ESC/Ctrl/Alt/Fn), cursor
     * arrows (Fn+arrow jumps Home/End/PageUp/PageDown), all common programming
     * symbols, a PC-style number row and direct clipboard/editing shortcuts.
     */
    fun coding(language: Language): List<List<Key>> {
        val row0 = toolbar(language)
        val row1 = listOf(
            Key("ESC", KeyAction.ESC, isFunction = true, weight = 1.2f),
            Key("Ctrl", KeyAction.CTRL_TOGGLE, isFunction = true, weight = 1.2f),
            Key("Alt", KeyAction.ALT_TOGGLE, isFunction = true, weight = 1.1f),
            Key("Fn", KeyAction.FN_TOGGLE, isFunction = true, weight = 1.1f),
            Key(SHIFT_ICON, KeyAction.SHIFT, isFunction = true, weight = 1f),
            Key("←", KeyAction.ARROW_LEFT, isFunction = true, weight = 0.9f),
            Key("↑", KeyAction.ARROW_UP, isFunction = true, weight = 0.9f),
            Key("↓", KeyAction.ARROW_DOWN, isFunction = true, weight = 0.9f),
            Key("→", KeyAction.ARROW_RIGHT, isFunction = true, weight = 0.9f)
        )
        val row2 = listOf(
            Key("Tab", KeyAction.TAB, isFunction = true, weight = 1.4f),
            sym('{'), sym('}'), sym('['), sym(']'), sym('('), sym(')'),
            sym('<'), sym('>'),
            Key(BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1.4f)
        )
        val row3 = listOf(
            sym('\\'), sym('|'), sym(';'), sym(':'), sym('\''), sym('"'),
            sym('='), sym('+'), sym('/'),
            Key(ENTER_ICON, KeyAction.ENTER, isFunction = true, weight = 1.3f)
        )
        val row4 = listOf(
            digit('1', '!'), digit('2', '@'), digit('3', '#'), digit('4', '$'),
            digit('5', '%'), digit('6', '^'), digit('7', '&'), digit('8', '*'),
            digit('9', '('), digit('0', ')')
        )
        val row5 = listOf(
            sym('~'), sym('_'), sym('-'), sym('?'),
            Key("space", KeyAction.SPACE, text = " ", weight = 1.5f),
            shortcutKey(ShortcutId.CUT, "Cut", 0.9f),
            shortcutKey(ShortcutId.COPY, "Copy", 0.9f),
            shortcutKey(ShortcutId.PASTE, "Paste", 1f),
            shortcutKey(ShortcutId.SELECT_ALL, "All", 0.9f),
            shortcutKey(ShortcutId.UNDO, "Undo", 0.9f),
            shortcutKey(ShortcutId.REDO, "Redo", 0.9f)
        )
        return listOf(row0, row1, row2, row3, row4, row5)
    }

    // --------------------------------------------------------------- english

    fun english(shifted: Boolean): List<List<Key>> {
        val shift = if (shifted) {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { it.toString() }
        } else {
            "abcdefghijklmnopqrstuvwxyz".map { it.toString() }
        }
        val casing: (String) -> String = { if (shifted) it.uppercase() else it }

        fun letter(c: Char, alts: List<String> = emptyList()) = Key(
            label = casing(c.toString()),
            alternates = if (shifted) alts.map { it.uppercase() } else alts
        )

        val row1 = "qwertyuiop".map { letter(it) }
        val row2 = "asdfghjkl".map { letter(it) }
        val row3 = listOf(
            Key(SHIFT_ICON, KeyAction.SHIFT, isFunction = true, weight = 1.4f),
            letter('z'), letter('x'), letter('c', listOf("ç")), letter('v'),
            letter('b'), letter('n', listOf("ñ")), letter('m'),
            Key(BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1.4f)
        )
        val row4 = listOf(
            Key(",", KeyAction.TEXT, text = ",", weight = 1.1f, alternates = listOf("!", "?", ";", ":", "-")),
            Key("space", KeyAction.SPACE, text = " ", weight = 4.4f),
            Key(".", KeyAction.TEXT, text = ".", weight = 1.1f, alternates = listOf(":", "…", "'", "\"")),
            Key(ENTER_ICON, KeyAction.ENTER, isFunction = true, weight = 1.8f)
        )
        return listOf(toolbar(Language.ENGLISH), row1, row2, row3, row4)
    }

    // --------------------------------------------------------------- numbers

    fun numbers(language: Language): List<List<Key>> {
        val digits = if (language == Language.BENGALI) {
            listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
        } else {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        }
        val row1 = digits.map { Key(it) }
        val row2 = "@ # $ % & - + ( ) /".split(" ").map { Key(it) }
        val row3 = listOf(
            Key("=\\<", KeyAction.OPEN_SYMBOLS, isFunction = true, weight = 1.5f),
            Key("*"), Key("\""), Key("'"), Key(":"), Key(";"), Key("!"), Key("?"),
            Key(BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1.5f)
        )
        val row4 = listOf(
            Key("ABC", KeyAction.BACK_TO_LETTERS, isFunction = true, weight = 1.5f),
            Key(",", weight = 1f),
            Key("_", weight = 1f),
            Key("space", KeyAction.SPACE, text = " ", weight = 3.4f),
            Key(".", weight = 1f),
            Key(ENTER_ICON, KeyAction.ENTER, isFunction = true, weight = 1.6f)
        )
        return listOf(toolbar(language), row1, row2, row3, row4)
    }

    // --------------------------------------------------------------- symbols

    fun symbols(): List<List<Key>> {
        val row1 = "~ ` | • √ π ÷ × ¶ ∆".split(" ").map { Key(it) }
        val row2 = "£ € ¥ ^ ° = { } \\".split(" ").map { Key(it) }
        val row3 = listOf(
            Key("123", KeyAction.OPEN_NUMBERS, isFunction = true, weight = 1.5f),
            Key("%"), Key("©"), Key("®"), Key("™"), Key("✓"), Key("["), Key("]"),
            Key(BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1.5f)
        )
        val row4 = listOf(
            Key("ABC", KeyAction.BACK_TO_LETTERS, isFunction = true, weight = 1.5f),
            Key(",", weight = 1f),
            Key("_", weight = 1f),
            Key("space", KeyAction.SPACE, text = " ", weight = 3.4f),
            Key(".", weight = 1f),
            Key(ENTER_ICON, KeyAction.ENTER, isFunction = true, weight = 1.6f)
        )
        return listOf(toolbar(Language.ENGLISH), row1, row2, row3, row4)
    }

    // ----------------------------------------------------------------- emoji

    private val EMOJI_ROWS = listOf(
        "😀 😃 😄 😁 😆 😅 😂 🙂 🙃 😉",
        "😊 😍 🥰 😘 😜 🤔 🤗 🤩 😎 🤝",
        "👍 👎 👏 🙏 💪 🔥 ✨ ❤️ 🎉 😢"
    )

    fun emoji(): List<List<Key>> {
        val rows = EMOJI_ROWS.map { row ->
            row.split(" ").map { Key(it) } + Key(
                BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1f
            )
        }
        val row4 = listOf(
            Key("ABC", KeyAction.BACK_TO_LETTERS, isFunction = true, weight = 1.6f),
            Key("123", KeyAction.OPEN_NUMBERS, isFunction = true, weight = 1.6f),
            Key("space", KeyAction.SPACE, text = " ", weight = 3.8f),
            Key(ENTER_ICON, KeyAction.ENTER, isFunction = true, weight = 1.6f)
        )
        return listOf(toolbar(Language.ENGLISH), rows[0], rows[1], rows[2], row4)
    }

    // --------------------------------------------------------------- bengali

    /**
     * Direct Bengali character layout (vowels, consonants, vowel signs,
     * Bengali digits and punctuation). This is a character-for-character
     * layout, not a phonetic transliteration method.
     */
    fun bengali(shifted: Boolean): List<List<Key>> {
        val row1 = "ক খ গ ঘ ঙ চ ছ জ ঝ ঞ".split(" ").map { Key(it) }
        val row2 = "ট ঠ ড ঢ ণ ত থ দ ধ ন".split(" ").map { Key(it) }
        val row3 = if (shifted) {
            listOf(
                Key(SHIFT_ICON, KeyAction.SHIFT, isFunction = true, weight = 1.4f),
                Key("শ"), Key("ষ"), Key("স"), Key("হ"), Key("ক্ষ"), Key("য়"),
                Key("ৎ"), Key("ড়"), Key("ঢ়"),
                Key(BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1.4f)
            )
        } else {
            listOf(
                Key(SHIFT_ICON, KeyAction.SHIFT, isFunction = true, weight = 1.4f),
                Key("প"), Key("ফ"), Key("ব"), Key("ভ"), Key("ম"), Key("য"),
                Key("র"), Key("ল"),
                Key(BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1.4f)
            )
        }
        val row4 = if (shifted) {
            // Shift + vowels row = Bengali digits.
            "১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯ ০".split(" ").map { Key(it) }
        } else {
            "অ আ ই ঈ উ ঊ এ ঐ ও ঔ".split(" ").map { Key(it) }
        }
        val row5 = if (shifted) {
            "! ? ; : - \" ( ) / ।".split(" ").map { Key(it) }
        } else {
            ", া ি ী ু ূ ে ৈ ো ৌ".split(" ").map { Key(it) }
        }
        val row6 = listOf(
            Key("ABC", KeyAction.BACK_TO_LETTERS, isFunction = true, weight = 1.5f),
            Key("।", weight = 1f),
            Key("ং", weight = 1f),
            Key("space", KeyAction.SPACE, text = " ", weight = 3.4f),
            Key(BACKSPACE_ICON, KeyAction.BACKSPACE, isFunction = true, weight = 1.3f),
            Key(ENTER_ICON, KeyAction.ENTER, isFunction = true, weight = 1.3f)
        )
        return listOf(toolbar(Language.BENGALI), row1, row2, row3, row4, row5, row6)
    }

    /** Pages the keyboard can be in. */
    enum class Page { LETTERS, NUMBERS, SYMBOLS, EMOJI, CODE }
}
