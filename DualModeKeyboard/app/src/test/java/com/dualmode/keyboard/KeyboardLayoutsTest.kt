package com.dualmode.keyboard

import com.dualmode.keyboard.data.Language
import com.dualmode.keyboard.keyboard.KeyAction
import com.dualmode.keyboard.keyboard.KeyboardLayouts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutsTest {

    @Test
    fun `english layout has toolbar plus four rows`() {
        val rows = KeyboardLayouts.forState(
            KeyboardLayouts.Page.LETTERS, Language.ENGLISH, shifted = false, ctrlActive = false
        )
        assertEquals(5, rows.size)
    }

    @Test
    fun `english layout contains qwerty keys and space`() {
        val rows = KeyboardLayouts.english(shifted = false)
        val allKeys = rows.flatten()
        assertTrue(allKeys.any { it.label == "q" })
        assertTrue(allKeys.any { it.label == "p" })
        assertTrue(allKeys.any { it.label == "m" })
        assertTrue(allKeys.any { it.action == KeyAction.SPACE })
        assertTrue(allKeys.any { it.action == KeyAction.BACKSPACE })
        assertTrue(allKeys.any { it.action == KeyAction.ENTER })
    }

    @Test
    fun `pc style toolbar has sticky ctrl tab and code toggle on every page`() {
        val pages = listOf(
            KeyboardLayouts.english(false),
            KeyboardLayouts.numbers(Language.ENGLISH),
            KeyboardLayouts.symbols(),
            KeyboardLayouts.emoji(),
            KeyboardLayouts.bengali(false),
            KeyboardLayouts.coding(Language.ENGLISH)
        )
        pages.forEach { rows ->
            val keys = rows.flatten()
            assertTrue(
                "every page must expose the sticky Ctrl key",
                keys.any { it.action == KeyAction.CTRL_TOGGLE }
            )
            assertTrue(
                "every page must expose a Tab key",
                keys.any { it.label == "Tab" && it.text == "\t" }
            )
            assertTrue(
                "every page must expose the Coding Keys toggle",
                keys.any { it.action == KeyAction.CODE_TOGGLE }
            )
        }
    }

    @Test
    fun `separate shortcut mode is removed`() {
        // The dedicated shortcut keyboard no longer exists: no page may
        // contain dedicated shortcut keys or a mode-switch key.
        val all = KeyboardLayouts.english(false) +
            KeyboardLayouts.numbers(Language.ENGLISH) +
            KeyboardLayouts.symbols() +
            KeyboardLayouts.emoji() +
            KeyboardLayouts.bengali(false)
        assertTrue(all.flatten().none { it.action == KeyAction.MODE_SWITCH })
        assertTrue(all.flatten().none { it.action == KeyAction.SHORTCUT })
    }

    @Test
    fun `shifted english layout is uppercase`() {
        val rows = KeyboardLayouts.english(shifted = true)
        val labels = rows.flatten().map { it.label }
        assertTrue(labels.contains("Q"))
        assertTrue(labels.contains("Z"))
        assertTrue(!labels.contains("q"))
    }

    @Test
    fun `number page has digits`() {
        val rows = KeyboardLayouts.numbers(Language.ENGLISH)
        val labels = rows.flatten().map { it.label }
        listOf("1", "2", "9", "0", "@", "#", "%").forEach {
            assertTrue("missing $it", labels.contains(it))
        }
    }

    @Test
    fun `bengali number page uses bengali digits`() {
        val rows = KeyboardLayouts.numbers(Language.BENGALI)
        val labels = rows.flatten().map { it.label }
        assertTrue(labels.contains("১"))
        assertTrue(labels.contains("৯"))
        assertTrue(labels.contains("০"))
    }

    @Test
    fun `bengali layout has consonants vowels and vowel signs`() {
        val rows = KeyboardLayouts.bengali(shifted = false)
        val labels = rows.flatten().map { it.label }
        listOf("ক", "খ", "ট", "ম", "অ", "আ", "ঔ", "া", "ী", "ৌ", "।").forEach {
            assertTrue("missing bengali char $it", labels.contains(it))
        }
    }

    @Test
    fun `shifted bengali shows extra consonants and bengali digits`() {
        val shifted = KeyboardLayouts.bengali(shifted = true)
        val labels = shifted.flatten().map { it.label }
        assertTrue(labels.contains("শ"))
        assertTrue(labels.contains("ক্ষ"))
        assertTrue(labels.contains("ড়"))
        // Shift + vowel row becomes Bengali digits.
        assertTrue(labels.contains("৫"))
    }

    @Test
    fun `letters remain typed as plain text on the normal keyboard`() {
        // With no dedicated shortcut page, letters are ordinary keys; the
        // Ctrl combination logic lives in the IME service, not the layout.
        val keys = KeyboardLayouts.english(false).flatten()
        assertTrue(keys.none { it.shortcut != null })
        assertTrue(keys.none { it.subLabel != null })
    }

    @Test
    fun `coding layout has modifiers arrows shift and escape`() {
        val keys = KeyboardLayouts.coding(Language.ENGLISH).flatten()
        assertTrue(keys.any { it.label == "ESC" && it.action == KeyAction.ESC })
        assertTrue(keys.any { it.label == "Ctrl" && it.action == KeyAction.CTRL_TOGGLE })
        assertTrue(keys.any { it.label == "Alt" && it.action == KeyAction.ALT_TOGGLE })
        assertTrue(keys.any { it.label == "Fn" && it.action == KeyAction.FN_TOGGLE })
        // CodeBoard-style: shift enables arrow-key text selection.
        assertTrue(keys.any { it.action == KeyAction.SHIFT })
        assertTrue(keys.any { it.action == KeyAction.ARROW_LEFT })
        assertTrue(keys.any { it.action == KeyAction.ARROW_UP })
        assertTrue(keys.any { it.action == KeyAction.ARROW_DOWN })
        assertTrue(keys.any { it.action == KeyAction.ARROW_RIGHT })
    }

    @Test
    fun `coding tab sends a real tab key event`() {
        // CodeBoard parity: Tab is a KEYCODE_TAB key event on the coding
        // keyboard (indent handling), not a committed tab character.
        val keys = KeyboardLayouts.coding(Language.ENGLISH).flatten()
        assertTrue(keys.any { it.label == "Tab" && it.action == KeyAction.TAB })
    }

    @Test
    fun `coding layout has all programming symbols`() {
        val labels = KeyboardLayouts.coding(Language.ENGLISH).flatten().map { it.label }
        // Keys shown directly or via long-press alternates on the number row.
        val alternates = KeyboardLayouts.coding(Language.ENGLISH).flatten().flatMap { it.alternates }
        val available = labels + alternates
        "{ } [ ] ( ) < > ; : ' \" = + - * / _ \\ | & ! ? # @ $ % ^ ~"
            .split(" ")
            .forEach { s ->
                assertTrue("coding layout missing symbol $s", available.contains(s))
            }
    }

    @Test
    fun `coding layout has pc style number row with shifted alternates`() {
        val digitLabels = setOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val digits = KeyboardLayouts.coding(Language.ENGLISH)
            .flatten()
            .filter { it.label in digitLabels }
        assertEquals(10, digits.size)
        val one = digits.first { it.label == "1" }
        assertEquals(listOf("!"), one.alternates)
        val two = digits.first { it.label == "2" }
        assertEquals(listOf("@"), two.alternates)
    }

    @Test
    fun `coding layout exposes direct editing shortcuts`() {
        val keys = KeyboardLayouts.coding(Language.ENGLISH).flatten()
        val ids = keys.mapNotNull { it.shortcut }
        listOf(
            com.dualmode.keyboard.shortcuts.ShortcutId.CUT,
            com.dualmode.keyboard.shortcuts.ShortcutId.COPY,
            com.dualmode.keyboard.shortcuts.ShortcutId.PASTE,
            com.dualmode.keyboard.shortcuts.ShortcutId.SELECT_ALL,
            com.dualmode.keyboard.shortcuts.ShortcutId.UNDO,
            com.dualmode.keyboard.shortcuts.ShortcutId.REDO
        ).forEach { id ->
            assertTrue("coding layout missing shortcut $id", ids.contains(id))
        }
        // No legacy mode-switch keys anywhere.
        assertTrue(keys.none { it.action == KeyAction.MODE_SWITCH })
        // Every shortcut key shows its combo as sub label.
        val copy = keys.first { it.shortcut == com.dualmode.keyboard.shortcuts.ShortcutId.COPY }
        assertEquals("Ctrl+C", copy.subLabel)
    }

    @Test
    fun `coding layout keeps space backspace and enter`() {
        val keys = KeyboardLayouts.coding(Language.ENGLISH).flatten()
        assertTrue(keys.any { it.action == KeyAction.SPACE })
        assertTrue(keys.any { it.action == KeyAction.BACKSPACE })
        assertTrue(keys.any { it.action == KeyAction.ENTER })
    }
}
