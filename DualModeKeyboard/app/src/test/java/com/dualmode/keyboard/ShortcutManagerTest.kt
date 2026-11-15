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
    fun `shortcut count matches spec`() {
        // 4 clipboard + 5 editing + 5 document shortcuts = 14
        assertEquals(14, ShortcutId.entries.size)
    }
}
