/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard's KeyboardLayoutView (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.layout;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.codingkeys.keyboard.theme.UiTheme;

/**
 * The keyboard panel: background painting, size calculation and modifier
 * highlighting across all key buttons.
 */
public class KeyboardLayoutView extends ViewGroup {

    private final UiTheme uiTheme;
    private final Paint panelPaint = new Paint();
    /** When > 0, the view measures to this fixed height (in-app previews). */
    private int fixedHeight = -1;
    /**
     * System navigation bar insets. In the default case the IME window is
     * laid out above the navigation bar by the system and these stay 0.
     * On edge-to-edge configurations (Android 15+, OEM translucent bars)
     * the window extends behind the bar, so the keys must be lifted above
     * it while the background continues behind it.
     */
    private int navLeftInset = 0;
    private int navRightInset = 0;
    private int navBottomInset = 0;

    public KeyboardLayoutView(Context context, UiTheme uiTheme) {
        super(context);
        this.uiTheme = uiTheme;
        panelPaint.setColor(uiTheme.backgroundColor);
        setWillNotDraw(false);
    }

    public void setFixedHeight(int heightPx) {
        this.fixedHeight = heightPx;
    }

    /**
     * Apply the system navigation bar insets so keyboard keys are never
     * drawn behind the Back / Home / Recents buttons.
     *
     * - Bottom inset: keys end above the navigation bar; the keyboard
     *   background keeps painting the strip behind the bar, so the bar
     *   area matches the keyboard.
     * - Left/right insets: cover tablets and foldables that place the
     *   3-button navigation bar on the side in landscape.
     *
     * Insets arrive from the WindowInsets listener the IME attaches; when
     * the system already fits the IME window above the bar, they are 0 and
     * the layout is unchanged.
     */
    public void setNavigationBarInsets(int left, int right, int bottom) {
        if (left != navLeftInset || right != navRightInset || bottom != navBottomInset) {
            navLeftInset = Math.max(0, left);
            navRightInset = Math.max(0, right);
            navBottomInset = Math.max(0, bottom);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (fixedHeight > 0) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), fixedHeight);
            return;
        }
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int availableHeight = metrics.heightPixels;
        int availableWidth = metrics.widthPixels;

        float keyboardSize = availableHeight > availableWidth
                ? uiTheme.portraitSize
                : uiTheme.landscapeSize;

        // Total height = requested key area + navigation bar strip (when the
        // window is drawn edge-to-edge). The key area itself keeps its
        // configured fraction of the screen, so keys are never squeezed or
        // cut off by the system bar below.
        setMeasuredDimension(availableWidth,
                (int) (availableHeight * keyboardSize) + navBottomInset);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && uiTheme.gradientEndColor != null) {
            uiTheme.applyPanelGradient(panelPaint, r - l, b - t);
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            // Keys are confined to the safe area above the navigation bar
            // (and beside a side navigation bar on tablets). The background
            // painted in onDraw still covers the full view, so the strip
            // behind the system buttons matches the keyboard.
            child.layout(l + navLeftInset, t, r - navRightInset, b - navBottomInset);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), panelPaint);
        if (uiTheme.enableBorder) {
            canvas.drawRect(0, 0, getWidth() - 1, getHeight() - 1, uiTheme.panelBorderPaint);
        }
        super.onDraw(canvas);
    }

    public void applyShiftModifier(boolean shiftPressed) {
        for (KeyboardButtonView button : getKeyboardButtons()) {
            button.applyShiftModifier(shiftPressed);
        }
    }

    public void applyCtrlModifier(boolean ctrlPressed) {
        for (KeyboardButtonView button : getKeyboardButtons()) {
            button.applyCtrlModifier(ctrlPressed);
        }
    }

    public void applyAltModifier(boolean altPressed) {
        for (KeyboardButtonView button : getKeyboardButtons()) {
            button.applyAltModifier(altPressed);
        }
    }

    private Collection<KeyboardButtonView> getKeyboardButtons() {
        int childCount = this.getChildCount();
        ArrayList<KeyboardButtonView> list = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof KeyboardButtonView) {
                list.add((KeyboardButtonView) child);
            }
        }
        return list;
    }
}
