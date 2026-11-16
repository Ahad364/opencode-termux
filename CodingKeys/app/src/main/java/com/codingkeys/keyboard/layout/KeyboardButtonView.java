/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard's KeyboardButtonView (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.codingkeys.keyboard.theme.UiTheme;

/**
 * A single rounded key. Draws its own body and content; handles press,
 * release, long press and key repeat. Repeat runs on the main looper so key
 * handling always happens on the same thread as the IME.
 */
public class KeyboardButtonView extends View {

    public interface KeyEventListener {
        void onPress(int code);

        void onKey(int code);

        void onText(CharSequence text);

        void onRelease(int code);

        void onLongPress(int code);
    }

    private static final int REPEAT_INITIAL_DELAY_MS = 400;
    private static final int REPEAT_INTERVAL_MS = 50;

    private final Key key;
    private final KeyEventListener listener;
    private final UiTheme uiTheme;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable repeatRunnable;
    private final boolean repeatEnabled;

    private String currentLabel;
    private boolean isPressed = false;
    private boolean longPressFired = false;
    private int repeatDelayMs = REPEAT_INITIAL_DELAY_MS;
    private int repeatIntervalMs = REPEAT_INTERVAL_MS;

    public KeyboardButtonView(Context context, Key key, KeyEventListener listener,
                              UiTheme uiTheme, boolean repeatEnabled) {
        super(context);
        this.key = key;
        this.listener = listener;
        this.uiTheme = uiTheme;
        this.currentLabel = key.info.label;
        this.repeatEnabled = repeatEnabled;
        setFocusable(false);
        setClickable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription(key.info.icon != null ? key.info.label : key.info.label);
        repeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPressed) return;
                submitKeyEvent();
                handler.postDelayed(this, KeyboardButtonView.this.repeatIntervalMs);
            }
        };
    }

    public void setRepeatTiming(int delayMs, int intervalMs) {
        this.repeatDelayMs = delayMs;
        this.repeatIntervalMs = intervalMs;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onPress();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onRelease();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        Box box = key.box;
        int w = r - l;
        int h = b - t;
        int left = (int) (l + w * box.getLeft());
        int right = (int) (l + w * box.getRight());
        int top = (int) (t + h * box.getTop());
        int bottom = (int) (t + h * box.getBottom());
        super.layout(left, top, right, bottom);
    }

    @Override
    public void draw(Canvas canvas) {
        drawButtonBody(canvas);
        drawButtonContent(canvas);
        super.draw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        autoReleaseIfPressed();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != VISIBLE) {
            autoReleaseIfPressed();
        }
    }

    private Paint paintForRole(boolean pressed) {
        switch (key.info.role) {
            case MODIFIER: return uiTheme.modifierKeyPaint;
            case ACTION:   return uiTheme.actionKeyPaint;
            case SPECIAL:  return pressed ? uiTheme.pressedKeyPaint : uiTheme.specialKeyPaint;
            default:       return pressed ? uiTheme.pressedKeyPaint : uiTheme.characterKeyPaint;
        }
    }

    private void drawButtonContent(Canvas canvas) {
        if (currentLabel != null && !currentLabel.isEmpty()) {
            float x = getWidth() / 2f;
            float y = getHeight() / 2f + uiTheme.fontHeight / 3f;
            canvas.drawText(currentLabel, x, y, uiTheme.keyTextPaint);
        }
        if (key.info.icon != null) {
            Drawable d = key.info.icon;
            d.setTint(uiTheme.keyTextPaint.getColor());
            int padding = (int) (uiTheme.buttonBodyPadding * 2);
            int top, left, squareSize;
            if (getWidth() > getHeight()) {
                top = 2 * padding;
                squareSize = (getHeight() / 2) - top;
                left = (getWidth() / 2) - squareSize;
            } else {
                left = 2 * padding;
                squareSize = getWidth() / 2 - left;
                top = getHeight() / 2 - squareSize;
            }
            if (squareSize > 0) {
                int right = left + (squareSize * 2);
                int bottom = top + (squareSize * 2);
                d.setBounds(left, top, right, bottom);
                d.draw(canvas);
            }
        }
    }

    private void drawButtonBody(Canvas canvas) {
        float left = uiTheme.buttonBodyPadding;
        float top = uiTheme.buttonBodyPadding;
        float right = getWidth() - uiTheme.buttonBodyPadding;
        float bottom = getHeight() - uiTheme.buttonBodyPadding;
        float radius = uiTheme.buttonBodyBorderRadius;
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, paintForRole(isPressed));
    }

    private void onPress() {
        isPressed = true;
        longPressFired = false;
        if (key.info.code != 0) {
            listener.onPress(key.info.code);
            handler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
            // Fire immediately on touch-down (matches the reference keyboard:
            // instant response, and repeatable keys start right away).
            submitKeyEvent();
        }
        if (key.info.isRepeatable && repeatEnabled) {
            handler.postDelayed(repeatRunnable, repeatDelayMs);
        }
        invalidate();
    }

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPressed || longPressFired) return;
            longPressFired = true;
            listener.onLongPress(key.info.code);
        }
    };

    private void onRelease() {
        isPressed = false;
        handler.removeCallbacks(repeatRunnable);
        handler.removeCallbacks(longPressRunnable);
        if (key.info.code != 0) {
            listener.onRelease(key.info.code);
        }
        // Output-text keys (code == 0) already fired onText on press; do not
        // fire again here.
        animateRelease();
        invalidate();
    }

    private void submitKeyEvent() {
        if (key.info.code != 0) {
            listener.onKey(key.info.code);
        }
        if (key.info.outputText != null && key.info.code == 0) {
            listener.onText(key.info.outputText);
        }
    }

    private void autoReleaseIfPressed() {
        if (isPressed) {
            isPressed = false;
            handler.removeCallbacks(repeatRunnable);
            handler.removeCallbacks(longPressRunnable);
            if (key.info.code != 0) {
                listener.onRelease(key.info.code);
            }
            invalidate();
        }
    }

    private void animateRelease() {
        if (uiTheme.enablePreview) {
            this.animate().translationY(0.0f).setDuration(100);
            this.animate().scaleX(1.0f).setDuration(100);
            this.animate().scaleY(1.0f).setDuration(100);
        } else {
            this.setAlpha(1.0f);
        }
    }

    public void applyShiftModifier(boolean shiftPressed) {
        if (key.info.onShiftLabel != null) {
            String nextLabel = shiftPressed ? key.info.onShiftLabel : key.info.label;
            setCurrentLabel(nextLabel);
        }
    }

    public void applyCtrlModifier(boolean ctrlPressed) {
        if (key.info.onCtrlLabel != null) {
            String nextLabel = ctrlPressed ? key.info.onCtrlLabel : key.info.label;
            setCurrentLabel(nextLabel);
        }
    }

    public void applyAltModifier(boolean altPressed) {
        if (key.info.onAltLabel != null) {
            String nextLabel = altPressed ? key.info.onAltLabel : key.info.label;
            setCurrentLabel(nextLabel);
        }
    }

    private void setCurrentLabel(String nextLabel) {
        if (nextLabel != null && !nextLabel.equals(currentLabel)) {
            currentLabel = nextLabel;
            invalidate();
        }
    }
}
