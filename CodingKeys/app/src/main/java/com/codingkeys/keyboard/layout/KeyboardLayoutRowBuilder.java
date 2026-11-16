/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.layout;

import java.util.ArrayList;

/**
 * Builds the keys of one row into concrete boxes.
 * Derived from CodeBoard's KeyboardLayoutRowBuilder.
 */
public class KeyboardLayoutRowBuilder {

    private Box box; // the dimensions of a row
    private final ArrayList<KeyInfo> keys = new ArrayList<>();
    private float gap = 0;

    public ArrayList<Key> build() throws KeyboardLayoutException {
        if (keys.size() == 0) {
            throw new KeyboardLayoutException("Row cannot be built without any keys");
        }
        if (box == null) {
            box = Box.create(0, 0, 0, 0);
        }
        float availableWidth = box.width - gap * (keys.size() - 1);
        float availableHeight = box.height;
        if (availableWidth <= 0) {
            throw new KeyboardLayoutException("Not enough space to fit keys in row");
        }
        float totalRequestedSize = 0;
        for (KeyInfo info : keys) {
            totalRequestedSize += info.size;
        }
        float cursorX = box.x;
        float cursorY = box.y;
        ArrayList<Key> result = new ArrayList<>();
        for (KeyInfo info : keys) {
            float width = availableWidth / totalRequestedSize * info.size;
            float height = availableHeight;
            Box keyBox = Box.create(cursorX, cursorY, width, height);
            cursorX += keyBox.width + gap;
            Key key = new Key();
            key.box = keyBox;
            key.info = info;
            result.add(key);
        }
        return result;
    }

    public KeyboardLayoutRowBuilder addKey(KeyInfo key) {
        keys.add(key);
        return this;
    }

    public KeyboardLayoutRowBuilder setBox(Box size) {
        this.box = size;
        return this;
    }

    public KeyboardLayoutRowBuilder setGap(float size) {
        this.gap = size;
        return this;
    }
}
