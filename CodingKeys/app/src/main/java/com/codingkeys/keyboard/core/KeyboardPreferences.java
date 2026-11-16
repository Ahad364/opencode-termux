/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard's KeyboardPreferences (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.codingkeys.keyboard.layout.SymbolLayoutManager;

/**
 * All persistent keyboard settings, stored locally in SharedPreferences.
 * Nothing leaves the device.
 */
public class KeyboardPreferences {

    public static final String SETTINGS_FILE = "coding_keys_prefs";

    // Custom theme color keys (values are ARGB ints; 0 = "unset" for gradient end).
    public static final String CUSTOM_COLOR_BACKGROUND = "custom_color_background";
    public static final String CUSTOM_COLOR_GRADIENT_END = "custom_color_gradient_end";
    public static final String CUSTOM_COLOR_KEY = "custom_color_key";
    public static final String CUSTOM_COLOR_SPECIAL = "custom_color_special";
    public static final String CUSTOM_COLOR_MODIFIER = "custom_color_modifier";
    public static final String CUSTOM_COLOR_ACTION = "custom_color_action";
    public static final String CUSTOM_COLOR_TEXT = "custom_color_text";

    // Toolbar modes.
    public static final int TOOLBAR_EDITING = 0;
    public static final int TOOLBAR_NAVIGATION = 1;
    public static final int TOOLBAR_HIDDEN = 2;

    private static final String FIRST_START = "first_start";
    private static final String THEME_INDEX = "theme_index";
    private static final String LAYOUT_INDEX = "layout_index";
    private static final String ENABLE_PREVIEW = "preview";
    private static final String ENABLE_BORDER = "border";
    private static final String ENABLE_SOUND = "sound";
    private static final String ENABLE_VIBRATE = "vibrate";
    private static final String ENABLE_KEY_REPEAT = "key_repeat";
    private static final String REPEAT_DELAY_MS = "repeat_delay_ms";
    private static final String REPEAT_RATE_MS = "repeat_rate_ms";
    private static final String STICKY_MODIFIERS = "sticky_modifiers";
    private static final String TOOLBAR_MODE = "toolbar_mode";
    private static final String KEY_HEIGHT_PCT = "key_height_pct";
    private static final String LANDSCAPE_HEIGHT_PCT = "landscape_height_pct";
    private static final String FONT_SIZE_SP = "font_size_sp";
    private static final String KEY_CORNER_RADIUS_DP = "key_corner_radius_dp";
    private static final String KEY_GAP_DP = "key_gap_dp";
    private static final String SYMBOL_ROWS = "symbol_rows";
    private static final String PROG_ROWS = "prog_rows";
    private static final String BOTTOM_CHARS = "bottom_chars";

    private final SharedPreferences preferences;
    private final Resources resources;

    public KeyboardPreferences(Context context) {
        this.preferences = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        this.resources = context.getResources();
    }

    public Resources getResources() {
        return resources;
    }

    // ---- first start ----

    public boolean isFirstStart() {
        return read(FIRST_START, true);
    }

    public void setFirstStart(boolean value) {
        write(FIRST_START, value);
    }

    // ---- theme & layout ----

    public int getThemeIndex() {
        return read(THEME_INDEX, 0);
    }

    public void setThemeIndex(int value) {
        write(THEME_INDEX, value);
    }

    public int getLayoutIndex() {
        return read(LAYOUT_INDEX, 0);
    }

    public void setLayoutIndex(int value) {
        write(LAYOUT_INDEX, value);
    }

    // ---- typing ----

    public boolean isSoundEnabled() {
        return read(ENABLE_SOUND, true);
    }

    public void setSoundEnabled(boolean value) {
        write(ENABLE_SOUND, value);
    }

    public boolean isVibrateEnabled() {
        return read(ENABLE_VIBRATE, true);
    }

    public void setVibrateEnabled(boolean value) {
        write(ENABLE_VIBRATE, value);
    }

    public boolean isKeyRepeatEnabled() {
        return read(ENABLE_KEY_REPEAT, true);
    }

    public void setKeyRepeatEnabled(boolean value) {
        write(ENABLE_KEY_REPEAT, value);
    }

    public int getRepeatDelayMs() {
        return read(REPEAT_DELAY_MS, 400);
    }

    public void setRepeatDelayMs(int value) {
        write(REPEAT_DELAY_MS, clamp(value, 100, 2000));
    }

    public int getRepeatRateMs() {
        return read(REPEAT_RATE_MS, 50);
    }

    public void setRepeatRateMs(int value) {
        write(REPEAT_RATE_MS, clamp(value, 20, 500));
    }

    /** Sticky = modifier stays on until pressed again; single-shot = clears after one key. */
    public boolean isStickyModifiers() {
        return read(STICKY_MODIFIERS, false);
    }

    public void setStickyModifiers(boolean value) {
        write(STICKY_MODIFIERS, value);
    }

    // ---- appearance ----

    public boolean isPreviewEnabled() {
        return read(ENABLE_PREVIEW, false);
    }

    public void setPreviewEnabled(boolean value) {
        write(ENABLE_PREVIEW, value);
    }

    public boolean isBorderEnabled() {
        return read(ENABLE_BORDER, true);
    }

    public void setBorderEnabled(boolean value) {
        write(ENABLE_BORDER, value);
    }

    /** Keyboard height percent of screen height (portrait). 20..60. */
    public int getKeyHeightPercent() {
        return read(KEY_HEIGHT_PCT, 32);
    }

    public void setKeyHeightPercent(int value) {
        write(KEY_HEIGHT_PCT, clamp(value, 20, 60));
    }

    /** Keyboard height percent of screen height (landscape). 30..80. */
    public int getLandscapeHeightPercent() {
        return read(LANDSCAPE_HEIGHT_PCT, 45);
    }

    public void setLandscapeHeightPercent(int value) {
        write(LANDSCAPE_HEIGHT_PCT, clamp(value, 30, 80));
    }

    public int getFontSizeSp() {
        return read(FONT_SIZE_SP, 18);
    }

    public void setFontSizeSp(int value) {
        write(FONT_SIZE_SP, clamp(value, 10, 28));
    }

    public int getKeyCornerRadiusDp() {
        return read(KEY_CORNER_RADIUS_DP, 10);
    }

    public void setKeyCornerRadiusDp(int value) {
        write(KEY_CORNER_RADIUS_DP, clamp(value, 0, 24));
    }

    public float getKeyCornerRadiusPx() {
        return dpToPx(getKeyCornerRadiusDp());
    }

    public int getKeyGapDp() {
        return read(KEY_GAP_DP, 2);
    }

    public void setKeyGapDp(int value) {
        write(KEY_GAP_DP, clamp(value, 0, 8));
    }

    public float getKeyGapPx() {
        return dpToPx(getKeyGapDp());
    }

    /** Toolbar row content: 0=editing, 1=navigation, 2=hidden. */
    public int getToolbarMode() {
        return read(TOOLBAR_MODE, TOOLBAR_EDITING);
    }

    public void setToolbarMode(int value) {
        write(TOOLBAR_MODE, clamp(value, TOOLBAR_EDITING, TOOLBAR_HIDDEN));
    }

    // ---- custom symbols ----

    public String getSymbolRows() {
        return read(SYMBOL_ROWS, SymbolLayoutManager.DEFAULT_SYMBOL_ROWS);
    }

    public void setSymbolRows(String value) {
        write(SYMBOL_ROWS, value);
    }

    public String getProgRows() {
        return read(PROG_ROWS, SymbolLayoutManager.DEFAULT_PROG_ROWS);
    }

    public void setProgRows(String value) {
        write(PROG_ROWS, value);
    }

    public String getBottomChars() {
        return read(BOTTOM_CHARS, SymbolLayoutManager.DEFAULT_BOTTOM_CHARS);
    }

    public void setBottomChars(String value) {
        write(BOTTOM_CHARS, value);
    }

    // ---- custom theme colors ----

    public int getCustomColor(String key, int defaultValue) {
        return read(key, defaultValue);
    }

    public void setCustomColor(String key, int value) {
        write(key, value);
    }

    public void resetAll() {
        preferences.edit().clear().apply();
    }

    // ---- helpers ----

    private float dpToPx(float dp) {
        return dp * resources.getDisplayMetrics().density;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean read(String key, boolean defaultValue) {
        return preferences.getInt(key, defaultValue ? 1 : 0) != 0;
    }

    private void write(String key, boolean value) {
        write(key, value ? 1 : 0);
    }

    private int read(String key, int defaultValue) {
        try {
            return preferences.getInt(key, defaultValue);
        } catch (ClassCastException e) {
            // Preference was written with an incompatible type; fall back to default.
            preferences.edit().remove(key).apply();
            return defaultValue;
        }
    }

    private void write(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    private String read(String key, String defaultValue) {
        try {
            return preferences.getString(key, defaultValue);
        } catch (ClassCastException e) {
            preferences.edit().remove(key).apply();
            return defaultValue;
        }
    }

    private void write(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }
}
