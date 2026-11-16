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

import com.codingkeys.keyboard.core.InputConnectionManager;

/**
 * Select All / Cut / Copy / Paste with editor-side preference and safe
 * fallbacks.
 *
 * Strategy (Part 5 of the requirements):
 * 1. Prefer editor-side operations via performContextMenuAction - these work
 *    on documents of any size without ever extracting text.
 * 2. Only when an editor reports it cannot perform an action, fall back to a
 *    bounded operation that reads just the selection (never the whole doc).
 * 3. Never report success for an operation the editor did not confirm.
 */
public class TextSelectionManager {

    public enum Result {
        SUCCESS, NO_CONNECTION, UNSUPPORTED, NO_SELECTION, NO_CLIPBOARD, FAILED
    }

    private final InputConnectionManager icm;
    private final ClipboardManager clipboard;

    public TextSelectionManager(InputConnectionManager icm, ClipboardManager clipboard) {
        this.icm = icm;
        this.clipboard = clipboard;
    }

    // ------------------------------------------------------------------
    // Select All
    // ------------------------------------------------------------------

    /**
     * Select the entire document using the editor's own action.
     * No full-document extraction is performed.
     */
    public Result selectAll() {
        if (!icm.hasConnection()) return Result.NO_CONNECTION;
        boolean ok = icm.performContextAction(android.R.id.selectAll);
        if (ok) return Result.SUCCESS;

        // Fallback: select the visible bounded window if the editor gives us
        // offsets. We do not extract the whole document just to select it.
        int[] sel = icm.getSelectionOffsets();
        if (sel != null) {
            // The editor does not support context select-all and we refuse to
            // read a potentially huge document; report honestly.
            return Result.UNSUPPORTED;
        }
        return Result.NO_CONNECTION;
    }

    // ------------------------------------------------------------------
    // Cut / Copy
    // ------------------------------------------------------------------

    public Result cut() {
        if (!icm.hasConnection()) return Result.NO_CONNECTION;

        // Preferred: editor-side cut (handles huge selections natively).
        if (icm.performContextAction(android.R.id.cut)) {
            return Result.SUCCESS;
        }

        // Fallback: read only the selection, place it on the clipboard, delete it.
        CharSequence selected = icm.getSelectedText();
        if (selected == null || selected.length() == 0) {
            return Result.NO_SELECTION;
        }
        if (clipboard.setClip(selected) != ClipboardManager.Result.SUCCESS) {
            return Result.FAILED;
        }
        if (!icm.replaceSelectedText("")) {
            return Result.FAILED;
        }
        return Result.SUCCESS;
    }

    public Result copy() {
        if (!icm.hasConnection()) return Result.NO_CONNECTION;

        if (icm.performContextAction(android.R.id.copy)) {
            return Result.SUCCESS;
        }

        CharSequence selected = icm.getSelectedText();
        if (selected == null || selected.length() == 0) {
            return Result.NO_SELECTION;
        }
        return clipboard.setClip(selected) == ClipboardManager.Result.SUCCESS
                ? Result.SUCCESS : Result.FAILED;
    }

    // ------------------------------------------------------------------
    // Paste
    // ------------------------------------------------------------------

    public Result paste() {
        if (!icm.hasConnection()) return Result.NO_CONNECTION;

        // Preferred: editor-side paste.
        if (icm.performContextAction(android.R.id.paste)) {
            return Result.SUCCESS;
        }

        // Fallback: commit clipboard text at the cursor (replaces selection).
        CharSequence clip = clipboard.getClip();
        if (clip == null || clip.length() == 0) {
            return Result.NO_CLIPBOARD;
        }
        return icm.replaceSelectedText(clip) ? Result.SUCCESS : Result.FAILED;
    }
}
