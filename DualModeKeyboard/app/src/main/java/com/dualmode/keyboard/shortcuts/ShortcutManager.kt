package com.dualmode.keyboard.shortcuts

/**
 * All supported Ctrl shortcuts. Each shortcut maps to a physical Android
 * KeyEvent so apps that implement desktop-style Ctrl handling receive it.
 *
 * IMPORTANT: Android keyboards cannot force every app to implement these
 * shortcuts. Clipboard operations (copy/cut/paste/select-all) are implemented
 * directly through the InputConnection and work in ordinary text editors;
 * the remaining shortcuts are dispatched as Ctrl key events and their effect
 * depends entirely on the focused application (e.g. Termux, Google Docs,
 * some office and code editors). See PRIVACY.md and TESTING.md.
 */
enum class ShortcutId(val comboLabel: String, val description: String, val keyCodeName: String?) {
    COPY("Ctrl+C", "Copy", "KEYCODE_C"),
    PASTE("Ctrl+V", "Paste", "KEYCODE_V"),
    CUT("Ctrl+X", "Cut", "KEYCODE_X"),
    SELECT_ALL("Ctrl+A", "Select all", "KEYCODE_A"),

    UNDO("Ctrl+Z", "Undo", "KEYCODE_Z"),
    REDO("Ctrl+Y", "Redo", "KEYCODE_Y"),
    BOLD("Ctrl+B", "Bold", "KEYCODE_B"),
    ITALIC("Ctrl+I", "Italic", "KEYCODE_I"),
    CENTER("Ctrl+E", "Center align", "KEYCODE_E"),

    SAVE("Ctrl+S", "Save", "KEYCODE_S"),
    PRINT("Ctrl+P", "Print", "KEYCODE_P"),
    OPEN("Ctrl+O", "Open document", "KEYCODE_O"),
    NEW_DOC("Ctrl+N", "New document", "KEYCODE_N"),
    FIND("Ctrl+F", "Find", "KEYCODE_F");

    companion object {
        fun fromKeyCodeName(name: String?): ShortcutId? =
            entries.firstOrNull { it.keyCodeName == name }
    }
}
