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

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;

/**
 * Thin, null-safe wrapper around the system clipboard.
 *
 * Privacy note: clipboard content is only read at the moment the user taps
 * Paste, and never stored or transmitted.
 */
public class ClipboardManager {

    public enum Result { SUCCESS, NO_CONNECTION, EMPTY_CLIPBOARD, FAILED }

    private final Context context;
    private final android.content.ClipboardManager systemClipboard;

    public ClipboardManager(Context context) {
        this.context = context;
        this.systemClipboard = (android.content.ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    /** Copy text to the clipboard. */
    public Result setClip(CharSequence text) {
        if (systemClipboard == null) return Result.FAILED;
        if (text == null || text.length() == 0) return Result.EMPTY_CLIPBOARD;
        try {
            systemClipboard.setPrimaryClip(ClipData.newPlainText("Coding Keys", text));
            return Result.SUCCESS;
        } catch (Exception e) {
            return Result.FAILED;
        }
    }

    /** Read the current clipboard text, or null when unavailable. */
    public CharSequence getClip() {
        if (systemClipboard == null) return null;
        try {
            ClipDescription desc = systemClipboard.getPrimaryClipDescription();
            if (desc == null || !desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                return null;
            }
            ClipData clip = systemClipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) return null;
            return clip.getItemAt(0).coerceToText(context);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasClip() {
        CharSequence text = getClip();
        return text != null && text.length() > 0;
    }
}
