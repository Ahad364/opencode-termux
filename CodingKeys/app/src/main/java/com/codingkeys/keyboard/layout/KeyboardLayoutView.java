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

    public KeyboardLayoutView(Context context, UiTheme uiTheme) {
        super(context);
        this.uiTheme = uiTheme;
        panelPaint.setColor(uiTheme.backgroundColor);
        setWillNotDraw(false);
    }

    public void setFixedHeight(int heightPx) {
        this.fixedHeight = heightPx;
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

        setMeasuredDimension(availableWidth, (int) (availableHeight * keyboardSize));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && uiTheme.gradientEndColor != null) {
            uiTheme.applyPanelGradient(panelPaint, r - l, b - t);
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.layout(l, t, r, b);
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
