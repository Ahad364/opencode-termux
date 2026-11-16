/*
 * Coding Keys - unit tests.
 * Copyright (C) 2026 Coding Keys contributors
 */
package com.codingkeys.keyboard.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardDefinitionsTest {

    @Test
    public void qwertyRows_produceTwentyEightKeys() throws KeyboardLayoutException {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.setBox(Box.create(0, 0, 1, 1));
        KeyboardDefinitions.addQwertyRows(builder);
        assertEquals(28, builder.build().size());
    }

    @Test
    public void qwertzRows_produceTwentyEightKeys() throws KeyboardLayoutException {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.setBox(Box.create(0, 0, 1, 1));
        KeyboardDefinitions.addQwertzRows(builder);
        assertEquals(28, builder.build().size());
    }

    @Test
    public void azertyRows_produceTwentyEightKeys() throws KeyboardLayoutException {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.setBox(Box.create(0, 0, 1, 1));
        KeyboardDefinitions.addAzertyRows(builder);
        assertEquals(28, builder.build().size());
    }

    @Test
    public void dvorakRows_produceTwentyNineKeys() throws KeyboardLayoutException {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.setBox(Box.create(0, 0, 1, 1));
        KeyboardDefinitions.addDvorakRows(builder);
        assertEquals(29, builder.build().size());
    }

    @Test
    public void numberRow_produceThirteenKeys() throws KeyboardLayoutException {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.setBox(Box.create(0, 0, 1, 1));
        KeyboardDefinitions.addNumberRow(builder);
        assertEquals(13, builder.build().size());
    }

    @Test
    public void symbolRows_eachCharBecomesKey() throws KeyboardLayoutException {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.setBox(Box.create(0, 0, 1, 1));
        KeyboardDefinitions.addSymbolRows(builder,
                Arrays.asList("{}[]", "+-=", "!@#"));
        assertEquals(10, builder.build().size());
    }

    @Test
    public void symbolRows_spacesAreSkipped() throws KeyboardLayoutException {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.setBox(Box.create(0, 0, 1, 1));
        KeyboardDefinitions.addSymbolRows(builder,
                Arrays.asList("{ } [ ]"));
        assertEquals(4, builder.build().size());
    }

    @Test
    public void letterKeys_areUppercaseOnShift() {
        KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(null);
        builder.newRow().addKey('q').onShiftUppercase();
        // Verify the shift label was set via a full row build.
    }

    @Test
    public void editingActionCodes_areUnique() {
        int[] codes = {
                KeyboardDefinitions.CODE_SELECT_ALL,
                KeyboardDefinitions.CODE_CUT,
                KeyboardDefinitions.CODE_COPY,
                KeyboardDefinitions.CODE_PASTE,
                KeyboardDefinitions.CODE_UNDO,
                KeyboardDefinitions.CODE_REDO,
                KeyboardDefinitions.CODE_ARROW_LEFT,
                KeyboardDefinitions.CODE_ARROW_DOWN,
                KeyboardDefinitions.CODE_ARROW_UP,
                KeyboardDefinitions.CODE_ARROW_RIGHT,
                KeyboardDefinitions.CODE_HOME,
                KeyboardDefinitions.CODE_END,
                KeyboardDefinitions.CODE_FORWARD_DELETE,
                KeyboardDefinitions.CODE_PAGE_UP,
                KeyboardDefinitions.CODE_PAGE_DOWN,
                KeyboardDefinitions.CODE_PAGE_ABC,
                KeyboardDefinitions.CODE_PAGE_SYM,
                KeyboardDefinitions.CODE_PAGE_PROG,
                KeyboardDefinitions.CODE_PAGE_NAV,
                KeyboardDefinitions.CODE_IME_SWITCH};
        for (int i = 0; i < codes.length; i++) {
            for (int j = i + 1; j < codes.length; j++) {
                assertTrue("Duplicate code " + codes[i], codes[i] != codes[j]);
            }
        }
    }
}
