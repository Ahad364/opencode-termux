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
package com.codingkeys.keyboard.theme;

/**
 * Data for one keyboard theme. All colors are ARGB ints.
 */
public class ThemeInfo {

    public String name;

    /** Keyboard panel background (flat color, or gradient start below). */
    public int backgroundColor;

    /** Optional gradient end for the keyboard panel background (null = flat). */
    public Integer backgroundColorGradientEnd;

    /** Background of regular character keys. */
    public int keyBackgroundColor;

    /** Background of special keys (tab, arrows, editing actions). */
    public int specialKeyBackgroundColor;

    /** Background of modifier keys (shift/ctrl/alt) and active page keys. */
    public int modifierKeyBackgroundColor;

    /** Background of the enter/action key. */
    public int actionKeyBackgroundColor;

    /** Text and icon color on keys. */
    public int keyTextColor;

    /** Draw a subtle border around the keyboard panel. */
    public boolean enableBorder = true;

    /** Key corner radius in px. */
    public float buttonBorderRadiusPx = 10.0f;

    /** Key body padding in px. */
    public float buttonBodyPaddingPx = 5.0f;

    // ---- Runtime metrics, filled in by the IME from user preferences ----
    /** Keyboard height as a fraction of screen height (portrait). */
    public float size = 0.32f;
    /** Keyboard height as a fraction of screen height (landscape). */
    public float sizeLandscape = 0.45f;
    /** Show the pop-up preview animation on key press. */
    public boolean enablePreview = false;
    /** Key label font size in px. */
    public float fontSizePx = 36f;

    public ThemeInfo() {
    }

    public ThemeInfo(String name) {
        this.name = name;
    }

    /** Chainable copy helper used by the theme definitions. */
    public ThemeInfo copy() {
        ThemeInfo t = new ThemeInfo(this.name);
        t.backgroundColor = this.backgroundColor;
        t.backgroundColorGradientEnd = this.backgroundColorGradientEnd;
        t.keyBackgroundColor = this.keyBackgroundColor;
        t.specialKeyBackgroundColor = this.specialKeyBackgroundColor;
        t.modifierKeyBackgroundColor = this.modifierKeyBackgroundColor;
        t.actionKeyBackgroundColor = this.actionKeyBackgroundColor;
        t.keyTextColor = this.keyTextColor;
        t.enableBorder = this.enableBorder;
        t.buttonBorderRadiusPx = this.buttonBorderRadiusPx;
        t.buttonBodyPaddingPx = this.buttonBodyPaddingPx;
        return t;
    }
}
