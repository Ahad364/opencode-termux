package com.dualmode.keyboard

import com.dualmode.keyboard.shortcuts.ShortcutId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortcutManagerTest {

    @Test
    fun `all shortcuts have key code names`() {
        ShortcutId.entries.forEach { id ->
            assertEquals(
                "Shortcut ${id.name} must declare its KeyEvent name",
                true,
                id.keyCodeName != null && id.keyCodeName.startsWith("KEYCODE_")
            )
        }
    }

    @Test
    fun `fromKeyCodeName round trips`() {
        ShortcutId.entries.forEach { id ->
            assertEquals(id, ShortcutId.fromKeyCodeName(id.keyCodeName))
        }
        assertNull(ShortcutId.fromKeyCodeName("KEYCODE_NOPE"))
        assertNull(ShortcutId.fromKeyCodeName(null))
    }

    @Test
    fun `fromChar maps typed letters to shortcuts`() {
        // Clipboard shortcuts
        assertEquals(ShortcutId.COPY, ShortcutId.fromChar('c'))
        assertEquals(ShortcutId.COPY, ShortcutId.fromChar('C'))
        assertEquals(ShortcutId.PASTE, ShortcutId.fromChar('V'))
        assertEquals(ShortcutId.CUT, ShortcutId.fromChar('x'))
        assertEquals(ShortcutId.SELECT_ALL, ShortcutId.fromChar('a'))
        // Editing + document shortcuts
        assertEquals(ShortcutId.UNDO, ShortcutId.fromChar('z'))
        assertEquals(ShortcutId.REDO, ShortcutId.fromChar('y'))
        assertEquals(ShortcutId.BOLD, ShortcutId.fromChar('b'))
        assertEquals(ShortcutId.ITALIC, ShortcutId.fromChar('i'))
        assertEquals(ShortcutId.SAVE, ShortcutId.fromChar('s'))
        assertEquals(ShortcutId.FIND, ShortcutId.fromChar('f'))
        // Letters without a shortcut (and non-letters) map to nothing
        assertNull(ShortcutId.fromChar('q'))
        assertNull(ShortcutId.fromChar('1'))
        assertNull(ShortcutId.fromChar(' '))
    }

    @Test
    fun `shortcut count matches spec`() {
        // 4 clipboard + 5 editing + 5 document shortcuts = 14
        assertEquals(14, ShortcutId.entries.size)
    }
}
