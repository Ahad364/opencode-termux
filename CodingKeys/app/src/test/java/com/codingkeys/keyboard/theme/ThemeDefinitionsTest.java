/*
 * Coding Keys - unit tests.
 * Copyright (C) 2026 Coding Keys contributors
 */
package com.codingkeys.keyboard.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThemeDefinitionsTest {

    @Test
    public void count_isTwelve() {
        assertEquals(12, ThemeDefinitions.count());
    }

    @Test
    public void byIndex_returnsThemeForEachIndex() {
        for (int i = 0; i < 12; i++) {
            ThemeInfo info = ThemeDefinitions.byIndex(i);
            assertNotNull(info);
            assertNotNull(info.name);
            assertTrue(info.name.length() > 0);
        }
    }

    @Test
    public void byIndex_unknownIndex_fallsBackToPremiumBlue() {
        assertEquals("Premium Blue", ThemeDefinitions.byIndex(-5).name);
        assertEquals("Premium Blue", ThemeDefinitions.byIndex(99).name);
    }

    @Test
    public void themes_haveSaneColors() {
        for (int i = 0; i < 12; i++) {
            ThemeInfo info = ThemeDefinitions.byIndex(i);
            assertTrue(info.backgroundColor != 0);
            assertTrue(info.keyBackgroundColor != 0);
            assertTrue(info.keyTextColor != 0);
            assertTrue(info.buttonBorderRadiusPx >= 0f);
            assertTrue(info.buttonBodyPaddingPx >= 0f);
        }
    }

    @Test
    public void customTheme_isSlotEleven() {
        assertEquals("Custom", ThemeDefinitions.byIndex(11).name);
    }

    @Test
    public void copy_preservesAllColors() {
        ThemeInfo original = ThemeDefinitions.PremiumBlue();
        ThemeInfo copy = original.copy();
        assertEquals(original.backgroundColor, copy.backgroundColor);
        assertEquals(original.keyBackgroundColor, copy.keyBackgroundColor);
        assertEquals(original.specialKeyBackgroundColor, copy.specialKeyBackgroundColor);
        assertEquals(original.modifierKeyBackgroundColor, copy.modifierKeyBackgroundColor);
        assertEquals(original.actionKeyBackgroundColor, copy.actionKeyBackgroundColor);
        assertEquals(original.keyTextColor, copy.keyTextColor);
        assertEquals(original.name, copy.name);
    }
}
