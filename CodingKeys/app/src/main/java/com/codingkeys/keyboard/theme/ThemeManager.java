/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.theme;

import com.codingkeys.keyboard.core.KeyboardPreferences;

/**
 * Resolves the active theme, including the user-configurable custom theme.
 * Theme changes apply on the next keyboard (re)creation - no reinstall needed.
 */
public class ThemeManager {

    /** Index of the custom theme slot in the built-in list. */
    public static final int CUSTOM_THEME_INDEX = 11;

    private ThemeManager() {
    }

    /** Resolve the ThemeInfo for the configured theme index. */
    public static ThemeInfo resolve(KeyboardPreferences prefs) {
        int index = prefs.getThemeIndex();
        ThemeInfo info = ThemeDefinitions.byIndex(index);
        if (index == CUSTOM_THEME_INDEX) {
            applyCustomColors(info, prefs);
        }
        // Runtime metrics from preferences.
        info.size = prefs.getKeyHeightPercent() / 100.0f;
        info.sizeLandscape = prefs.getLandscapeHeightPercent() / 100.0f;
        info.enablePreview = prefs.isPreviewEnabled();
        info.enableBorder = prefs.isBorderEnabled();
        info.fontSizePx = prefs.getFontSizeSp() * prefs.getResources().getDisplayMetrics().scaledDensity;
        return info;
    }

    /** Read the custom theme colors from preferences into the theme object. */
    public static void applyCustomColors(ThemeInfo info, KeyboardPreferences prefs) {
        info.backgroundColor = prefs.getCustomColor(KeyboardPreferences.CUSTOM_COLOR_BACKGROUND, info.backgroundColor);
        info.backgroundColorGradientEnd = null;
        int gradientEnd = prefs.getCustomColor(KeyboardPreferences.CUSTOM_COLOR_GRADIENT_END, 0);
        if (gradientEnd != 0) {
            info.backgroundColorGradientEnd = gradientEnd;
        }
        info.keyBackgroundColor = prefs.getCustomColor(KeyboardPreferences.CUSTOM_COLOR_KEY, info.keyBackgroundColor);
        info.specialKeyBackgroundColor = prefs.getCustomColor(KeyboardPreferences.CUSTOM_COLOR_SPECIAL, info.specialKeyBackgroundColor);
        info.modifierKeyBackgroundColor = prefs.getCustomColor(KeyboardPreferences.CUSTOM_COLOR_MODIFIER, info.modifierKeyBackgroundColor);
        info.actionKeyBackgroundColor = prefs.getCustomColor(KeyboardPreferences.CUSTOM_COLOR_ACTION, info.actionKeyBackgroundColor);
        info.keyTextColor = prefs.getCustomColor(KeyboardPreferences.CUSTOM_COLOR_TEXT, info.keyTextColor);
        info.buttonBorderRadiusPx = prefs.getKeyCornerRadiusPx();
    }
}
