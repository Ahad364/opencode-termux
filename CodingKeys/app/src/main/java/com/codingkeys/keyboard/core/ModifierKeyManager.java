/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard's modifier logic (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.core;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

/**
 * Manages SHIFT / CTRL / ALT modifier states.
 *
 * Strategy proven in the reference project: the physical meta keys are
 * emulated with KeyEvent ACTION_DOWN/UP on the input connection so editors
 * that implement shift+arrow selection and ctrl shortcuts natively keep
 * working, while the visible key state is tracked here.
 */
public class ModifierKeyManager {

    /** Updates modifier highlight state on the keyboard view. */
    public interface OnModifierChangedListener {
        void onModifierStateChanged();
    }

    private final InputMethodService ime;
    private boolean sticky;

    private boolean shift = false;
    private boolean shiftLock = false;
    private boolean ctrl = false;
    private boolean alt = false;

    private OnModifierChangedListener listener;

    public ModifierKeyManager(InputMethodService ime) {
        this.ime = ime;
    }

    public void setOnModifierChangedListener(OnModifierChangedListener listener) {
        this.listener = listener;
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    public boolean isShift() {
        return shift;
    }

    public boolean isShiftLock() {
        return shiftLock;
    }

    public boolean isCtrl() {
        return ctrl;
    }

    public boolean isAlt() {
        return alt;
    }

    /** Meta mask for the currently held modifiers (used for synthesized key events). */
    public int currentMetaState() {
        int meta = 0;
        if (shift) meta |= KeyEvent.META_SHIFT_LEFT_ON | KeyEvent.META_SHIFT_ON;
        if (ctrl) meta |= KeyEvent.META_CTRL_LEFT_ON | KeyEvent.META_CTRL_ON;
        if (alt) meta |= KeyEvent.META_ALT_LEFT_ON | KeyEvent.META_ALT_ON;
        return meta;
    }

    /** Toggle the shift modifier. A tap while shift-locked releases the lock. */
    public void toggleShift() {
        InputConnection ic = ime.getCurrentInputConnection();
        if (shiftLock) {
            // Tap while locked: release lock and shift together.
            shiftLock = false;
            releaseShift(ic);
            notifyChanged();
            return;
        }
        if (!shift) {
            shift = true;
            sendMeta(ic, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.ACTION_DOWN);
        } else {
            releaseShift(ic);
        }
        notifyChanged();
    }

    /** Long-press toggles caps-style shift lock. */
    public void toggleShiftLock() {
        InputConnection ic = ime.getCurrentInputConnection();
        shiftLock = !shiftLock;
        if (shiftLock) {
            shift = true;
            sendMeta(ic, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.ACTION_DOWN);
        } else {
            shift = false;
            sendMeta(ic, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.ACTION_UP);
        }
        notifyChanged();
    }

    /** Toggle the ctrl modifier. */
    public void toggleCtrl() {
        InputConnection ic = ime.getCurrentInputConnection();
        ctrl = !ctrl;
        sendMeta(ic, KeyEvent.KEYCODE_CTRL_LEFT, ctrl ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP);
        notifyChanged();
    }

    /** Toggle the alt modifier. */
    public void toggleAlt() {
        InputConnection ic = ime.getCurrentInputConnection();
        alt = !alt;
        sendMeta(ic, KeyEvent.KEYCODE_ALT_LEFT, alt ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP);
        notifyChanged();
    }

    /**
     * Consume one-shot modifiers before a regular key press.
     * Returns the meta mask that should be applied to the event.
     */
    public int consumeBeforeKey(InputConnection ic) {
        int meta = currentMetaState();
        if (shift && !shiftLock) {
            releaseShift(ic);
        }
        if (!sticky) {
            if (ctrl) {
                ctrl = false;
                sendMeta(ic, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.ACTION_UP);
            }
            if (alt) {
                alt = false;
                sendMeta(ic, KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.ACTION_UP);
            }
        }
        notifyChanged();
        return meta;
    }

    /** Release all held modifiers (input restart / editor change). */
    public void releaseAll() {
        InputConnection ic = ime.getCurrentInputConnection();
        if (shift || shiftLock) {
            releaseShift(ic);
            shiftLock = false;
        }
        if (ctrl) {
            ctrl = false;
            sendMeta(ic, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.ACTION_UP);
        }
        if (alt) {
            alt = false;
            sendMeta(ic, KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.ACTION_UP);
        }
        notifyChanged();
    }

    private void releaseShift(InputConnection ic) {
        shift = false;
        sendMeta(ic, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.ACTION_UP);
    }

    private void sendMeta(InputConnection ic, int keyCode, int action) {
        if (ic == null) return;
        try {
            ic.sendKeyEvent(new KeyEvent(0, 0, action, keyCode, 0));
        } catch (Exception e) {
            android.util.Log.w("ModifierKeyManager", "sendMeta failed", e);
        }
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onModifierStateChanged();
        }
    }
}
