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

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.codingkeys.keyboard.R;

import java.util.ArrayList;

/**
 * Fluent builder that turns rows of keys into positioned boxes.
 * Derived from CodeBoard's KeyboardLayoutBuilder.
 */
public class KeyboardLayoutBuilder {

    private final Context context;
    private Box box; // the dimensions of the keyboard
    private final ArrayList<KeyboardLayoutRowBuilder> rows = new ArrayList<>();
    private KeyboardLayoutRowBuilder currentRow = null;
    private KeyInfo currentKey = null;
    private float rowGap = 0; // space between keyboard rows
    private float keyGap = 0; // space between keys (horizontal)
    private float padding = 0;

    public KeyboardLayoutBuilder(Context current) {
        this.context = current;
    }

    public KeyboardLayoutBuilder newRow() {
        currentKey = null;
        currentRow = new KeyboardLayoutRowBuilder();
        rows.add(currentRow);
        return this;
    }

    public KeyboardLayoutBuilder setBox(Box box) {
        this.box = box;
        return this;
    }

    public KeyboardLayoutBuilder setRowGap(float size) {
        this.rowGap = size;
        return this;
    }

    public KeyboardLayoutBuilder setKeyGap(float size) {
        this.keyGap = size;
        return this;
    }

    public KeyboardLayoutBuilder setPadding(float size) {
        this.padding = size;
        return this;
    }

    public KeyboardLayoutBuilder addKey(String label, int code) {
        if (currentRow == null) {
            newRow();
        }
        currentKey = new KeyInfo();
        currentKey.label = label;
        currentKey.code = code;
        currentKey.size = 1.0f;
        currentKey.isRepeatable = false;
        currentRow.addKey(currentKey);
        return this;
    }

    public KeyboardLayoutBuilder addKey(Drawable icon, int code) {
        return this.addKey("", code).withIcon(icon);
    }

    public KeyboardLayoutBuilder addKey(char key) {
        return this.addKey("" + key, (int) key);
    }

    public KeyboardLayoutBuilder addKey(String label) {
        return this.addKey(label, 0).withOutputText(label);
    }

    public KeyboardLayoutBuilder asRepeatable(boolean repeat) {
        currentKey.isRepeatable = repeat;
        return this;
    }

    public KeyboardLayoutBuilder asRepeatable() {
        return this.asRepeatable(true);
    }

    public KeyboardLayoutBuilder withSize(float size) {
        currentKey.size = size;
        return this;
    }

    public KeyboardLayoutBuilder withCode(int code) {
        currentKey.code = code;
        return this;
    }

    public KeyboardLayoutBuilder withIcon(Drawable icon) {
        currentKey.icon = icon;
        return this;
    }

    public KeyboardLayoutBuilder withRole(KeyInfo.Role role) {
        currentKey.role = role;
        return this;
    }

    public KeyboardLayoutBuilder asModifier(boolean isModifier) {
        currentKey.isModifier = isModifier;
        return this;
    }

    public KeyboardLayoutBuilder asModifier() {
        return asModifier(true);
    }

    public KeyboardLayoutBuilder withOutputText(String s) {
        currentKey.outputText = s;
        return this;
    }

    public KeyboardLayoutBuilder onShiftShow(String label) {
        currentKey.onShiftLabel = label;
        return this;
    }

    public KeyboardLayoutBuilder onCtrlShow(String label) {
        currentKey.onCtrlLabel = label;
        return this;
    }

    public KeyboardLayoutBuilder onAltShow(String label) {
        currentKey.onAltLabel = label;
        return this;
    }

    public KeyboardLayoutBuilder onShiftUppercase() {
        return onShiftShow(currentKey.label.toUpperCase());
    }

    public ArrayList<Key> build() throws KeyboardLayoutException {
        float availableWidth = box.width - 2 * padding;
        float availableHeight = box.height - (rows.size() - 1) * rowGap - 2 * padding;
        float cursorX = box.x + padding;
        float cursorY = box.y + padding;
        ArrayList<Key> result = new ArrayList<>();
        for (KeyboardLayoutRowBuilder rowBuilder : rows) {
            rowBuilder.setGap(keyGap);
            float width = availableWidth;
            float height = availableHeight / rows.size();
            Box rowBox = Box.create(cursorX, cursorY, width, height);
            rowBuilder.setBox(rowBox);
            cursorY += rowBox.height;
            cursorY += rowGap;
            result.addAll(rowBuilder.build());
        }
        return result;
    }

    public Context getContext() {
        return context;
    }

    // common key definitions (extension methods, mirroring the reference builder)

    public KeyboardLayoutBuilder addTabKey() {
        return addKey("Tab", KeyboardDefinitions.CODE_TAB).withRole(KeyInfo.Role.SPECIAL);
    }

    public KeyboardLayoutBuilder addShiftKey() {
        return addKey("Shift", KeyboardDefinitions.CODE_SHIFT).asModifier()
                .onShiftShow("SHIFT").withSize(1.5f).withRole(KeyInfo.Role.MODIFIER);
    }

    public KeyboardLayoutBuilder addCtrlKey() {
        return addKey("Ctrl", KeyboardDefinitions.CODE_CTRL).asModifier()
                .onCtrlShow("CTRL").withRole(KeyInfo.Role.MODIFIER);
    }

    public KeyboardLayoutBuilder addAltKey() {
        return addKey("Alt", KeyboardDefinitions.CODE_ALT).asModifier()
                .onAltShow("ALT").withRole(KeyInfo.Role.MODIFIER);
    }

    /** Null-safe drawable load so pure-unit tests can build layouts without a Context. */
    private Drawable loadDrawable(int resId) {
        return context == null ? null : context.getDrawable(resId);
    }

    public KeyboardLayoutBuilder addBackspaceKey() {
        return addKey(loadDrawable(R.drawable.ic_backspace), KeyboardDefinitions.CODE_BACKSPACE)
                .asRepeatable().withRole(KeyInfo.Role.SPECIAL);
    }

    public KeyboardLayoutBuilder addEnterKey() {
        return addKey(loadDrawable(R.drawable.ic_keyboard_return), KeyboardDefinitions.CODE_ENTER)
                .withSize(1.5f).withRole(KeyInfo.Role.ACTION);
    }
}
