/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.core;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.codingkeys.keyboard.editing.CursorNavigationManager;
import com.codingkeys.keyboard.editing.TextSelectionManager;
import com.codingkeys.keyboard.editing.UndoRedoManager;
import com.codingkeys.keyboard.layout.KeyboardDefinitions;

/**
 * Routes every key code from the keyboard view to the manager responsible
 * for it. All editor interaction goes through InputConnectionManager so
 * null/unsupported cases are handled in exactly one place.
 *
 * Never logs typed text - only key codes.
 */
public class KeyboardEventDispatcher {

    private static final String TAG = "KeyboardEventDispatcher";

    private final InputMethodService ime;
    private final InputConnectionManager icm;
    private final ModifierKeyManager modifiers;
    private final TextSelectionManager selection;
    private final CursorNavigationManager navigation;
    private final UndoRedoManager undoRedo;

    public KeyboardEventDispatcher(InputMethodService ime, InputConnectionManager icm,
                                   ModifierKeyManager modifiers,
                                   TextSelectionManager selection,
                                   CursorNavigationManager navigation,
                                   UndoRedoManager undoRedo) {
        this.ime = ime;
        this.icm = icm;
        this.modifiers = modifiers;
        this.selection = selection;
        this.navigation = navigation;
        this.undoRedo = undoRedo;
    }

    /**
     * Handle a key press identified by its primary code.
     * Returns true when the code was handled as a control key; false means
     * the caller should treat it as text input.
     */
    public boolean handleKey(int primaryCode) {
        switch (primaryCode) {
            case KeyboardDefinitions.CODE_SELECT_ALL:
                selection.selectAll();
                return true;
            case KeyboardDefinitions.CODE_CUT:
                selection.cut();
                return true;
            case KeyboardDefinitions.CODE_COPY:
                selection.copy();
                return true;
            case KeyboardDefinitions.CODE_PASTE:
                selection.paste();
                return true;
            case KeyboardDefinitions.CODE_UNDO:
                undoRedo.undo();
                return true;
            case KeyboardDefinitions.CODE_REDO:
                undoRedo.redo();
                return true;

            case KeyboardDefinitions.CODE_CTRL:
                modifiers.toggleCtrl();
                return true;
            case KeyboardDefinitions.CODE_ALT:
                modifiers.toggleAlt();
                return true;
            case KeyboardDefinitions.CODE_SHIFT:
                modifiers.toggleShift();
                return true;

            default:
                return false;
        }
    }

    /**
     * Handle navigation/whitespace/action keys. Returns true when handled.
     */
    public boolean handleNavigationKey(int primaryCode) {
        switch (primaryCode) {
            case KeyboardDefinitions.CODE_ARROW_LEFT:
                if (modifiers.isShift()) navigation.extendSelectionLeft();
                else navigation.moveLeft();
                return true;
            case KeyboardDefinitions.CODE_ARROW_RIGHT:
                if (modifiers.isShift()) navigation.extendSelectionRight();
                else navigation.moveRight();
                return true;
            case KeyboardDefinitions.CODE_ARROW_UP:
                if (modifiers.isShift()) navigation.extendSelectionUp();
                else navigation.moveUp();
                return true;
            case KeyboardDefinitions.CODE_ARROW_DOWN:
                if (modifiers.isShift()) navigation.extendSelectionDown();
                else navigation.moveDown();
                return true;
            case KeyboardDefinitions.CODE_HOME:
                navigation.moveHome();
                return true;
            case KeyboardDefinitions.CODE_END:
                navigation.moveEnd();
                return true;
            case KeyboardDefinitions.CODE_PAGE_UP:
                navigation.pageUp();
                return true;
            case KeyboardDefinitions.CODE_PAGE_DOWN:
                navigation.pageDown();
                return true;
            case KeyboardDefinitions.CODE_FORWARD_DELETE:
                navigation.deleteForward();
                return true;
            case KeyboardDefinitions.CODE_TAB:
                sendKeyWithModifiers(KeyEvent.KEYCODE_TAB);
                return true;
            case KeyboardDefinitions.CODE_ESCAPE:
                sendKeyWithModifiers(KeyEvent.KEYCODE_ESCAPE);
                return true;
            case KeyboardDefinitions.CODE_BACKSPACE:
                handleBackspace();
                return true;
            case KeyboardDefinitions.CODE_ENTER:
                handleEnter();
                return true;
            default:
                return false;
        }
    }

    /**
     * Commit a plain character, consuming modifiers first. Returns false when
     * there is no input connection.
     */
    public boolean commitCharacter(char code, boolean shiftWanted) {
        if (!icm.hasConnection()) return false;
        modifiers.consumeBeforeKey(icm.getConnection());
        char out = shiftWanted ? Character.toUpperCase(code) : code;
        recordAndCommit(String.valueOf(out));
        return true;
    }

    /** Commit arbitrary text (custom multi-char output), recording undo ops. */
    public boolean commitText(CharSequence text) {
        if (!icm.hasConnection()) return false;
        modifiers.consumeBeforeKey(icm.getConnection());
        recordAndCommit(text);
        return true;
    }

    /**
     * Record the change we are about to make so the local undo fallback can
     * invert it. Reads ONLY the current selection - never the whole document.
     */
    private void recordAndCommit(CharSequence newText) {
        int[] sel = icm.getSelectionOffsets();
        CharSequence oldText = icm.getSelectedText();
        boolean committed = icm.commitText(newText);
        if (committed && undoRedo != null && sel != null) {
            int start = Math.max(0, sel[0]);
            undoRedo.recordCommit(start,
                    oldText == null ? "" : oldText.toString(),
                    newText);
        }
    }

    private void handleBackspace() {
        if (!icm.hasConnection()) return;
        int[] sel = icm.getSelectionOffsets();
        int meta = modifiers.consumeBeforeKey(icm.getConnection());

        // When a selection exists, prefer deleting exactly the selection
        // (bounded, no document scan) so undo can invert it precisely.
        if (sel != null && sel[1] > sel[0] && sel[1] - sel[0] <= UndoRedoManager.MAX_OP_TEXT_LENGTH) {
            CharSequence selected = icm.getSelectedText();
            boolean ok = icm.commitText("");
            if (ok && undoRedo != null) {
                undoRedo.recordCommit(sel[0], selected == null ? "" : selected.toString(), "");
            }
            return;
        }

        // Plain backspace: editor key event (native repeat behavior), with a
        // bounded record of the single character removed for local undo.
        CharSequence before = icm.getTextBeforeCursor(1);
        boolean sent = icm.sendKeyEvent(
                new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, meta));
        if (sent && undoRedo != null && before != null && before.length() == 1 && sel != null) {
            undoRedo.recordBackwardDelete(Math.max(0, sel[0] - 1), before.toString());
        }
    }

    private void handleEnter() {
        if (!icm.hasConnection()) return;
        int meta = modifiers.consumeBeforeKey(icm.getConnection());
        EditorInfo info = ime.getCurrentInputEditorInfo();

        // Single-line fields with an IME action trigger that action directly
        // (Search/Send/Go/Next); multiline fields receive a real newline.
        if (info != null) {
            int actionId = info.actionId != 0 ? info.actionId :
                    (info.imeOptions & EditorInfo.IME_MASK_ACTION);
            boolean noEnterAction =
                    (info.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
            boolean multiline = (info.inputType & EditorInfo.TYPE_CLASS_TEXT) != 0
                    && (info.inputType & EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
            if (!multiline && !noEnterAction && actionId != EditorInfo.IME_ACTION_NONE) {
                icm.performEditorAction(actionId);
                return;
            }
        }
        icm.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, meta));
    }

    private void sendKeyWithModifiers(int keyCode) {
        if (!icm.hasConnection()) return;
        int meta = modifiers.consumeBeforeKey(icm.getConnection());
        icm.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, meta));
    }

    /** Guarded logging - codes only, never text. */
    public static void logKey(int code) {
        Log.d(TAG, "key code=" + code);
    }
}
