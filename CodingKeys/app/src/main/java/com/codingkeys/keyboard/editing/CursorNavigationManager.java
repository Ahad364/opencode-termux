/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.editing;

import android.view.KeyEvent;

import com.codingkeys.keyboard.core.InputConnectionManager;
import com.codingkeys.keyboard.core.ModifierKeyManager;

/**
 * Cursor navigation: arrows, Home/End, PgUp/PgDn, forward delete and
 * word-wise movement.
 *
 * Editor key events are preferred (they work with any document size and
 * integrate with the editor's own selection logic). Bounded text-window
 * fallbacks cover editors that ignore synthesized key events.
 */
public class CursorNavigationManager {

    private final InputConnectionManager icm;
    private final ModifierKeyManager modifiers;

    public CursorNavigationManager(InputConnectionManager icm, ModifierKeyManager modifiers) {
        this.icm = icm;
        this.modifiers = modifiers;
    }

    // ------------------------------------------------------------------
    // Arrow keys
    // ------------------------------------------------------------------

    public void moveLeft() {
        sendWithMeta(KeyEvent.KEYCODE_DPAD_LEFT);
    }

    public void moveRight() {
        sendWithMeta(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    public void moveUp() {
        sendWithMeta(KeyEvent.KEYCODE_DPAD_UP);
    }

    public void moveDown() {
        sendWithMeta(KeyEvent.KEYCODE_DPAD_DOWN);
    }

    // ------------------------------------------------------------------
    // Home / End / Page
    // ------------------------------------------------------------------

    public void moveHome() {
        sendWithMeta(KeyEvent.KEYCODE_MOVE_HOME);
    }

    public void moveEnd() {
        sendWithMeta(KeyEvent.KEYCODE_MOVE_END);
    }

    public void pageUp() {
        sendWithMeta(KeyEvent.KEYCODE_PAGE_UP);
    }

    public void pageDown() {
        sendWithMeta(KeyEvent.KEYCODE_PAGE_DOWN);
    }

    /** Forward delete (delete the character after the cursor). */
    public void deleteForward() {
        sendWithMeta(KeyEvent.KEYCODE_FORWARD_DEL);
    }

    // ------------------------------------------------------------------
    // Word-wise movement (CTRL+LEFT / CTRL+RIGHT)
    // ------------------------------------------------------------------

    public void moveWordLeft() {
        int[] before = icm.getSelectionOffsets();
        int meta = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_LEFT, meta));
        int[] after = icm.getSelectionOffsets();
        if (before == null || after == null) {
            return; // cannot verify; trust the editor handled the event
        }
        if (after[0] != before[0] || after[1] != before[1]) {
            return; // editor handled ctrl+arrow natively
        }
        // Fallback for editors that ignore ctrl+arrow events: bounded scan.
        String beforeText = icm.getTextBeforeCursor(64);
        int skip = 0;
        // Skip trailing whitespace then the word characters before it.
        int i = beforeText.length() - 1;
        while (i >= 0 && Character.isWhitespace(beforeText.charAt(i))) { skip++; i--; }
        while (i >= 0 && !Character.isWhitespace(beforeText.charAt(i))) { skip++; i--; }
        for (int s = 0; s < skip; s++) {
            icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_LEFT, 0));
        }
    }

    public void moveWordRight() {
        int[] before = icm.getSelectionOffsets();
        int meta = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, meta));
        int[] after = icm.getSelectionOffsets();
        if (before == null || after == null) {
            return; // cannot verify; trust the editor handled the event
        }
        if (after[0] != before[0] || after[1] != before[1]) {
            return; // editor handled ctrl+arrow natively
        }
        // Bounded fallback scan for editors that ignore ctrl+arrow events.
        String afterText = icm.getTextAfterCursor(64);
        int skip = 0;
        int i = 0;
        while (i < afterText.length() && Character.isWhitespace(afterText.charAt(i))) { skip++; i++; }
        while (i < afterText.length() && !Character.isWhitespace(afterText.charAt(i))) { skip++; i++; }
        for (int s = 0; s < skip; s++) {
            icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, 0));
        }
    }

    // ------------------------------------------------------------------
    // Selection extension (SHIFT + arrows) - plain meta events like the
    // reference project; editors with native selection support handle this.
    // ------------------------------------------------------------------

    public void extendSelectionLeft() {
        int meta = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_LEFT, meta));
    }

    public void extendSelectionRight() {
        int meta = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, meta));
    }

    public void extendSelectionUp() {
        int meta = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_UP, meta));
    }

    public void extendSelectionDown() {
        int meta = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        icm.sendKeyEvent(keyEvent(KeyEvent.KEYCODE_DPAD_DOWN, meta));
    }

    // ------------------------------------------------------------------

    private void sendWithMeta(int keyCode) {
        int meta = modifiers.currentMetaState();
        icm.sendKeyEvent(keyEvent(keyCode, meta));
    }

    private static KeyEvent keyEvent(int keyCode, int meta) {
        return new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, meta);
    }
}
