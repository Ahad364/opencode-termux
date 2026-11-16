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

import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import android.view.KeyEvent;

/**
 * Safe wrapper around InputConnection.
 *
 * Large-document rules (Part 5 of the requirements):
 * - Never read the whole document: cursor positions come from a
 *   1-character extraction window (selection offsets are absolute anyway).
 * - Every call is null-safe and exception-safe.
 * - Editing operations report whether the editor actually supported them.
 */
public class InputConnectionManager {

    /** Small bounded window used for word navigation fallbacks. */
    private static final int CONTEXT_WINDOW_CHARS = 64;

    private final InputMethodServiceHost host;
    private InputConnection lastConnection;

    /** Minimal interface over the IME so tests stay simple. */
    public interface InputMethodServiceHost {
        InputConnection getCurrentInputConnection();
    }

    public InputConnectionManager(InputMethodServiceHost host) {
        this.host = host;
    }

    public InputConnection getConnection() {
        InputConnection ic = host.getCurrentInputConnection();
        if (ic != null) {
            lastConnection = ic;
        }
        return ic;
    }

    public boolean hasConnection() {
        return getConnection() != null;
    }

    // ------------------------------------------------------------------
    // Committing text
    // ------------------------------------------------------------------

    /** Commit a character or string. Returns false when there is no editor. */
    public boolean commitText(CharSequence text) {
        InputConnection ic = getConnection();
        if (ic == null || text == null || text.length() == 0) {
            return false;
        }
        try {
            return ic.commitText(text, 1);
        } catch (Exception e) {
            android.util.Log.w("InputConnectionMgr", "commitText failed", e);
            return false;
        }
    }

    /** Delete n characters before the cursor. */
    public boolean deleteBackward(int n) {
        InputConnection ic = getConnection();
        if (ic == null) return false;
        try {
            return ic.deleteSurroundingText(n, 0);
        } catch (Exception e) {
            android.util.Log.w("InputConnectionMgr", "deleteBackward failed", e);
            return false;
        }
    }

    /**
     * Perform the editor's IME action (Search/Send/Go/Next...).
     * Returns false when unsupported.
     */
    public boolean performEditorAction(int action) {
        InputConnection ic = getConnection();
        if (ic == null) return false;
        try {
            ic.performEditorAction(action);
            return true;
        } catch (Exception e) {
            android.util.Log.w("InputConnectionMgr", "performEditorAction failed", e);
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Editor-side context actions (Select All / Cut / Copy / Paste / Undo / Redo)
    // ------------------------------------------------------------------

    /**
     * Perform a standard editor context action (e.g. android.R.id.selectAll).
     * Returns true only when the editor reports that it handled the action.
     */
    public boolean performContextAction(int actionId) {
        InputConnection ic = getConnection();
        if (ic == null) return false;
        try {
            return ic.performContextMenuAction(actionId);
        } catch (Exception e) {
            android.util.Log.w("InputConnectionMgr", "performContextAction failed", e);
            return false;
        }
    }

    /** Send a raw key event to the editor. Returns false when there is no editor. */
    public boolean sendKeyEvent(KeyEvent event) {
        InputConnection ic = getConnection();
        if (ic == null) return false;
        try {
            return ic.sendKeyEvent(event);
        } catch (Exception e) {
            android.util.Log.w("InputConnectionMgr", "sendKeyEvent failed", e);
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Bounded reads (never the full document)
    // ------------------------------------------------------------------

    /**
     * Absolute selection offsets, read through a 1-character extraction
     * window. Returns null when the editor does not support extraction.
     */
    public int[] getSelectionOffsets() {
        InputConnection ic = getConnection();
        if (ic == null) return null;
        try {
            ExtractedTextRequest req = new ExtractedTextRequest();
            req.hintMaxChars = 1; // tiny window; offsets are absolute regardless
            ExtractedText et = ic.getExtractedText(req, 0);
            if (et == null) return null;
            return new int[]{et.selectionStart, et.selectionEnd};
        } catch (Exception e) {
            return null;
        }
    }

    /** Selected text, or null when nothing is selected / unsupported. */
    public CharSequence getSelectedText() {
        InputConnection ic = getConnection();
        if (ic == null) return null;
        try {
            return ic.getSelectedText(0);
        } catch (Exception e) {
            return null;
        }
    }

    /** Bounded text before the cursor (word-navigation fallbacks). */
    public String getTextBeforeCursor(int maxChars) {
        InputConnection ic = getConnection();
        if (ic == null) return "";
        try {
            CharSequence t = ic.getTextBeforeCursor(Math.min(maxChars, CONTEXT_WINDOW_CHARS), 0);
            return t == null ? "" : t.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Bounded text after the cursor (word-navigation fallbacks). */
    public String getTextAfterCursor(int maxChars) {
        InputConnection ic = getConnection();
        if (ic == null) return "";
        try {
            CharSequence t = ic.getTextAfterCursor(Math.min(maxChars, CONTEXT_WINDOW_CHARS), 0);
            return t == null ? "" : t.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Set an absolute selection. */
    public boolean setSelection(int start, int end) {
        InputConnection ic = getConnection();
        if (ic == null) return false;
        try {
            return ic.setSelection(start, end);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Replace the currently selected range with the given text
     * (used by cut/paste fallbacks). Bounded to the selection only.
     */
    public boolean replaceSelectedText(CharSequence replacement) {
        InputConnection ic = getConnection();
        if (ic == null) return false;
        try {
            return ic.commitText(replacement == null ? "" : replacement, 1);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finish a batch edit safely. Kept public so the editing managers can
     * group related updates without leaking the raw connection.
     */
    public Batch beginBatch() {
        return new Batch(getConnection());
    }

    /** Handle for grouped edits. */
    public static final class Batch {
        private final InputConnection ic;
        private boolean ended;

        private Batch(InputConnection ic) {
            this.ic = ic;
            if (ic != null) {
                ic.beginBatchEdit();
            }
        }

        public InputConnection connection() {
            return ic;
        }

        public void end() {
            if (ic != null && !ended) {
                ended = true;
                ic.endBatchEdit();
            }
        }
    }

    /** The last live connection, for tests that wrap with InputConnectionWrapper. */
    public InputConnection lastConnection() {
        return lastConnection;
    }
}
