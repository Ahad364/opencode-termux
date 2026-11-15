package com.dualmode.keyboard

import com.dualmode.keyboard.ime.KeyboardState
import com.dualmode.keyboard.keyboard.KeyboardLayouts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardStateTest {

    @Test
    fun `shift cycles off on locked`() {
        val state = KeyboardState()
        assertFalse(state.isShifted)
        state.toggleShift()
        assertTrue(state.isShifted)
        state.toggleShift()
        assertTrue(state.isShifted) // locked
        state.toggleShift()
        assertFalse(state.isShifted)
    }

    @Test
    fun `shift resets after one typed character unless locked`() {
        val state = KeyboardState()
        state.toggleShift() // ON
        state.consumeShiftAfterText()
        assertFalse(state.isShifted)

        state.toggleShift()
        state.toggleShift() // LOCKED
        state.consumeShiftAfterText()
        assertTrue(state.isShifted) // caps lock persists
    }

    @Test
    fun `language toggle switches and returns to letters`() {
        val state = KeyboardState()
        state.openPage(KeyboardLayouts.Page.NUMBERS)
        state.toggleLanguage()
        assertEquals(KeyboardLayouts.Page.LETTERS, state.page)
        assertFalse(state.isShifted)
    }

    @Test
    fun `ctrl toggle flips state`() {
        val state = KeyboardState()
        assertFalse(state.ctrlActive)
        state.toggleCtrl()
        assertTrue(state.ctrlActive)
        state.toggleCtrl()
        assertFalse(state.ctrlActive)
    }

    @Test
    fun `alt and fn toggles flip state and reset clears them`() {
        val state = KeyboardState()
        assertFalse(state.altActive)
        assertFalse(state.fnActive)
        state.toggleAlt()
        state.toggleFn()
        assertTrue(state.altActive)
        assertTrue(state.fnActive)
        state.toggleAlt()
        state.toggleFn()
        assertFalse(state.altActive)
        assertFalse(state.fnActive)

        state.toggleAlt()
        state.toggleFn()
        state.reset()
        assertFalse(state.altActive)
        assertFalse(state.fnActive)
    }

    @Test
    fun `code page toggle round trips`() {
        val state = KeyboardState()
        state.openPage(KeyboardLayouts.Page.CODE)
        assertEquals(KeyboardLayouts.Page.CODE, state.page)
        state.openPage(KeyboardLayouts.Page.LETTERS)
        assertEquals(KeyboardLayouts.Page.LETTERS, state.page)
    }

    @Test
    fun `page changes reset shift`() {
        val state = KeyboardState()
        state.toggleShift()
        state.openPage(KeyboardLayouts.Page.SYMBOLS)
        assertFalse(state.isShifted)
        assertEquals(KeyboardLayouts.Page.SYMBOLS, state.page)
    }
}
