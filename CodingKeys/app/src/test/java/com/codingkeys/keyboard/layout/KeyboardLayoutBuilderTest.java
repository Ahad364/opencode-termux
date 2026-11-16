/*
 * Coding Keys - unit tests.
 * Derived from CodeBoard's builder tests (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 */
package com.codingkeys.keyboard.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

public class KeyboardLayoutBuilderTest {

    @Test
    public void build_returnsCorrectNumberOfKeys() throws KeyboardLayoutException {
        ArrayList<Key> keyboard = builder().setBox(Box.create(0, 0, 100, 100))
                .newRow().addKey('a').addKey('b')
                .newRow().addKey('c').addKey('d').build();
        assertEquals(4, keyboard.size());
    }

    @Test
    public void build_normalizedBox_keepsAllKeysInsideBounds() throws KeyboardLayoutException {
        ArrayList<Key> keyboard = builder().setBox(Box.create(0, 0, 1, 1))
                .newRow().addKey('q').addKey('w').withSize(1.5f)
                .newRow().addKey('a').build();
        for (Key key : keyboard) {
            assertTrue(key.box.getLeft() >= 0f);
            assertTrue(key.box.getRight() <= 1.0001f);
            assertTrue(key.box.getTop() >= 0f);
            assertTrue(key.box.getBottom() <= 1.0001f);
        }
    }

    @Test
    public void build_sameSizeKeys_getEqualWidths() throws KeyboardLayoutException {
        ArrayList<Key> keyboard = builder().setBox(Box.create(0, 0, 100, 10))
                .newRow().addKey('a').addKey('b').addKey('c').addKey('d').build();
        float width = keyboard.get(0).box.width;
        for (Key key : keyboard) {
            assertEquals(width, key.box.width, 0.001f);
        }
    }

    @Test
    public void build_largerKey_getsLargerWidth() throws KeyboardLayoutException {
        ArrayList<Key> keyboard = builder().setBox(Box.create(0, 0, 100, 10))
                .newRow().addKey('a').withSize(2.0f).addKey('b').withSize(1.0f).build();
        assertEquals(keyboard.get(0).box.width, keyboard.get(1).box.width * 2f, 0.001f);
    }

    @Test
    public void build_twoRows_getHalfHeightEach() throws KeyboardLayoutException {
        ArrayList<Key> keyboard = builder().setBox(Box.create(0, 0, 100, 100))
                .newRow().addKey('a')
                .newRow().addKey('b').build();
        assertEquals(50f, keyboard.get(0).box.height, 0.001f);
        assertEquals(50f, keyboard.get(1).box.height, 0.001f);
    }

    @Test(expected = KeyboardLayoutException.class)
    public void build_emptyRow_throws() throws KeyboardLayoutException {
        builder().setBox(Box.create(0, 0, 100, 100))
                .newRow()
                .build();
    }

    private KeyboardLayoutBuilder builder() {
        // Context is only needed for drawable keys; pure math tests pass null.
        return new KeyboardLayoutBuilder(null);
    }
}
