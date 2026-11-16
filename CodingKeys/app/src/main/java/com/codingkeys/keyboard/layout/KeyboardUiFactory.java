/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard's KeyboardUiFactory (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.layout;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import com.codingkeys.keyboard.theme.UiTheme;

import java.util.Collection;

/**
 * Builds the keyboard view tree from positioned keys.
 */
public class KeyboardUiFactory {

    private final KeyboardButtonView.KeyEventListener inputListener;
    public UiTheme uiTheme;
    public boolean keyRepeatEnabled = true;
    public int repeatDelayMs = 400;
    public int repeatIntervalMs = 50;
    public float keyGapPx = 2.0f;

    public KeyboardUiFactory(KeyboardButtonView.KeyEventListener inputListener) {
        this.inputListener = inputListener;
    }

    public KeyboardLayoutView createKeyboardView(Context context, Collection<Key> keys) {
        UiTheme theme = this.uiTheme;
        KeyboardLayoutView layout = new KeyboardLayoutView(context, theme);

        // Keys are laid out in normalized space (Box 0..1); convert to pixels.
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = layoutHeightPx(context, theme);

        for (Key key : keys) {
            Box box = key.box;
            int left = (int) (box.getLeft() * width);
            int top = (int) (box.getTop() * height);
            int w = (int) (box.width * width);
            int h = (int) (box.height * height);

            KeyboardButtonView view = createKeyView(context, key, theme);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
            params.leftMargin = left;
            params.topMargin = top;
            layout.addView(view, params);
        }
        return layout;
    }

    /** Height the KeyboardLayoutView will measure itself to. */
    public static int layoutHeightPx(Context context, UiTheme theme) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float keyboardSize = metrics.heightPixels > metrics.widthPixels
                ? theme.portraitSize
                : theme.landscapeSize;
        return (int) (metrics.heightPixels * keyboardSize);
    }

    private KeyboardButtonView createKeyView(Context context, Key key, UiTheme theme) {
        KeyboardButtonView view = new KeyboardButtonView(context, key, inputListener, theme,
                keyRepeatEnabled);
        view.setRepeatTiming(repeatDelayMs, repeatIntervalMs);
        return view;
    }

    /** Convenience for in-app previews where layout height is fixed. */
    public static KeyboardLayoutView buildPreview(Context context, Collection<Key> keys,
                                                  UiTheme theme,
                                                  KeyboardButtonView.KeyEventListener listener,
                                                  int heightPx) {
        KeyboardLayoutView layout = new KeyboardLayoutView(context, theme);
        layout.setFixedHeight(heightPx);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        for (Key key : keys) {
            Box box = key.box;
            int left = (int) (box.getLeft() * width);
            int top = (int) (box.getTop() * heightPx);
            int w = (int) (box.width * width);
            int h = (int) (box.height * heightPx);
            KeyboardButtonView view = new KeyboardButtonView(context, key, listener, theme, false);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
            params.leftMargin = left;
            params.topMargin = top;
            layout.addView(view, params);
        }
        return layout;
    }

    /** Listener that does nothing - used for static previews. */
    public static class NoOpListener implements KeyboardButtonView.KeyEventListener {
        @Override public void onPress(int code) { }
        @Override public void onKey(int code) { }
        @Override public void onText(CharSequence text) { }
        @Override public void onRelease(int code) { }
        @Override public void onLongPress(int code) { }
    }
}
