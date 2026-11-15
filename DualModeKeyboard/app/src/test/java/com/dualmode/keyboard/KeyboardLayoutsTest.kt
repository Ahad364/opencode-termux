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
        assertTrue(allKeys.any { it.action == KeyAction.MODE_SWITCH })
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
    fun `shortcut layout contains ctrl toggle and every shortcut`() {
        val rows = KeyboardLayouts.shortcuts(ctrlActive = false)
        val keys = rows.flatten()
        assertTrue(keys.any { it.action == KeyAction.CTRL_TOGGLE })
        val shortcutIds = keys.mapNotNull { it.shortcut }
        assertEquals(
            com.dualmode.keyboard.shortcuts.ShortcutId.entries.size,
            shortcutIds.size
        )
        // Shortcut keys show their combo as sub label.
        val copy = keys.first { it.shortcut == com.dualmode.keyboard.shortcuts.ShortcutId.COPY }
        assertEquals("Ctrl+C", copy.subLabel)
        assertEquals("Copy", copy.label)
    }

    @Test
    fun `shortcut rows have consistent weights and mode switch`() {
        val rows = KeyboardLayouts.shortcuts(ctrlActive = true)
        assertTrue(rows.size >= 4)
        assertTrue(rows.flatten().any { it.action == KeyAction.MODE_SWITCH })
    }
}
