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

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;

/**
 * Render-time state derived from a ThemeInfo: paints, sizes and metrics.
 * Derived from CodeBoard's UiTheme.
 */
public class UiTheme {

    public Paint keyTextPaint;
    public int backgroundColor;

    /** Optional gradient used by the keyboard panel background. */
    public Integer gradientEndColor;

    public float fontHeight;
    public float buttonBodyPadding = 5.0f;
    public float buttonBodyBorderRadius = 8.0f;
    public boolean enablePreview = false;
    public boolean enableBorder;
    public float portraitSize;
    public float landscapeSize;

    // Per-role key body paints (from the Coding Keys theme engine).
    public Paint characterKeyPaint;
    public Paint specialKeyPaint;
    public Paint modifierKeyPaint;
    public Paint actionKeyPaint;
    public Paint pressedKeyPaint;
    public Paint panelBorderPaint;

    private UiTheme() {
        keyTextPaint = new Paint();
        characterKeyPaint = new Paint();
        specialKeyPaint = new Paint();
        modifierKeyPaint = new Paint();
        actionKeyPaint = new Paint();
        pressedKeyPaint = new Paint();
        panelBorderPaint = new Paint();
        backgroundColor = 0xFF000000;
    }

    public static UiTheme buildFromInfo(ThemeInfo info) {
        UiTheme theme = new UiTheme();
        theme.portraitSize = info.size;
        theme.landscapeSize = info.sizeLandscape;
        theme.enablePreview = info.enablePreview;
        theme.enableBorder = info.enableBorder;
        theme.gradientEndColor = info.backgroundColorGradientEnd;
        theme.buttonBodyBorderRadius = info.buttonBorderRadiusPx;
        theme.buttonBodyPadding = info.buttonBodyPaddingPx;

        // Panel background: subtle darker blend when border is enabled (reference behavior).
        if (info.enableBorder) {
            theme.backgroundColor = blend(info.backgroundColor, Color.BLACK, 0.2f);
        } else {
            theme.backgroundColor = info.backgroundColor;
        }

        theme.characterKeyPaint.setColor(info.keyBackgroundColor);
        theme.specialKeyPaint.setColor(info.specialKeyBackgroundColor);
        theme.modifierKeyPaint.setColor(info.modifierKeyBackgroundColor);
        theme.actionKeyPaint.setColor(info.actionKeyBackgroundColor);

        // Pressed keys lighten the regular key color.
        theme.pressedKeyPaint.setColor(blend(info.keyBackgroundColor, Color.WHITE, 0.25f));

        theme.panelBorderPaint.setColor(blend(info.backgroundColor, Color.WHITE, 0.12f));
        theme.panelBorderPaint.setStyle(Paint.Style.STROKE);
        theme.panelBorderPaint.setStrokeWidth(1.5f);

        theme.keyTextPaint.setColor(info.keyTextColor);
        theme.fontHeight = info.fontSizePx;
        theme.keyTextPaint.setTextSize(theme.fontHeight);
        theme.keyTextPaint.setTextAlign(Paint.Align.CENTER);
        theme.keyTextPaint.setAntiAlias(true);
        theme.keyTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        return theme;
    }

    /** Apply a gradient shader to the panel paint once view height is known. */
    public void applyPanelGradient(Paint panelPaint, int width, int height) {
        if (gradientEndColor == null) {
            panelPaint.setColor(backgroundColor);
            return;
        }
        panelPaint.setShader(new LinearGradient(0, 0, width, height,
                backgroundColor, gradientEndColor, Shader.TileMode.CLAMP));
    }

    private static int blend(int color1, int color2, float ratio) {
        final float inverseR = 1f - ratio;
        float a = (Color.alpha(color1) * inverseR) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseR) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseR) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseR) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }
}
