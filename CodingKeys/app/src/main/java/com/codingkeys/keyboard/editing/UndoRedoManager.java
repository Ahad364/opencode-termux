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

import android.view.inputmethod.InputConnection;

import com.codingkeys.keyboard.core.InputConnectionManager;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Undo/Redo support.
 *
 * Strategy (honest about platform limits):
 * 1. Editors that implement the standard context actions
 *    (android.R.id.undo / android.R.id.redo) are used first. Their result
 *    boolean decides whether the operation really succeeded.
 * 2. Editors that do NOT support them get a bounded local fallback: Coding
 *    Keys records the text operations it performed itself (commits and
 *    forward/backward deletes it issues) and inverts them on demand.
 *    - The stack is capped (see MAX_OPS) and only records small bounded
 *      payloads, so memory stays constant for large documents.
 *    - Operations the editor performed on its own (external edits) are NOT
 *      recorded; undo then only covers keyboard-initiated changes. This is
 *      an honest limitation of IME-side undo on Android.
 */
public class UndoRedoManager {

    /** Maximum recorded operations. */
    public static final int MAX_OPS = 100;
    /** Maximum recorded payload size per op (protects against huge selections). */
    public static final int MAX_OP_TEXT_LENGTH = 10_000;

    private final InputConnectionManager icm;

    /** One recorded keyboard-initiated change. */
    static class EditOp {
        int start;         // absolute cursor offset where the op happened
        CharSequence oldText; // text that was there before ("" for pure insert)
        CharSequence newText; // text that is there after

        boolean undo(InputConnection ic) {
            if (newText.length() > 0) {
                ic.setSelection(start, start + newText.length());
                ic.commitText(oldText, 1);
            }
            return true;
        }

        boolean redo(InputConnection ic) {
            if (oldText.length() > 0) {
                ic.setSelection(start, start + oldText.length());
                ic.commitText(newText, 1);
            }
            return true;
        }
    }

    private final Deque<EditOp> undoStack = new ArrayDeque<>();
    private final Deque<EditOp> redoStack = new ArrayDeque<>();

    /** Cached result of the last native undo attempt; null = not probed yet. */
    private Boolean nativeUndoSupported;
    private Boolean nativeRedoSupported;

    public UndoRedoManager(InputConnectionManager icm) {
        this.icm = icm;
    }

    // ------------------------------------------------------------------
    // Recording (called by the dispatcher around its own edits)
    // ------------------------------------------------------------------

    /** Record a text commit the keyboard performed. */
    public void recordCommit(int start, CharSequence oldText, CharSequence newText) {
        if (oldText == null) oldText = "";
        if (newText == null) newText = "";
        if (oldText.length() > MAX_OP_TEXT_LENGTH || newText.length() > MAX_OP_TEXT_LENGTH) {
            return; // too large to keep bounded
        }
        EditOp op = new EditOp();
        op.start = Math.max(0, start);
        op.oldText = oldText.toString();
        op.newText = newText.toString();
        pushUndo(op);
    }

    /** Record a backward delete of {@code deleted} at {@code start}. */
    public void recordBackwardDelete(int start, CharSequence deleted) {
        EditOp op = new EditOp() {
            @Override
            boolean undo(InputConnection ic) {
                ic.setSelection(start, start);
                ic.commitText(deleted, 1);
                return true;
            }

            @Override
            boolean redo(InputConnection ic) {
                ic.setSelection(start + deleted.length(), start + deleted.length());
                ic.deleteSurroundingText(deleted.length(), 0);
                return true;
            }
        };
        op.start = Math.max(0, start);
        op.newText = "";
        op.oldText = deleted.toString();
        pushUndo(op);
    }

    /** Record a forward delete of {@code deleted} at {@code start}. */
    public void recordForwardDelete(int start, CharSequence deleted) {
        EditOp op = new EditOp() {
            @Override
            boolean undo(InputConnection ic) {
                ic.setSelection(start, start);
                ic.commitText(deleted, 1);
                return true;
            }

            @Override
            boolean redo(InputConnection ic) {
                ic.setSelection(start, start);
                ic.deleteSurroundingText(0, deleted.length());
                return true;
            }
        };
        op.start = Math.max(0, start);
        op.newText = "";
        op.oldText = deleted.toString();
        pushUndo(op);
    }

    private void pushUndo(EditOp op) {
        undoStack.push(op);
        if (undoStack.size() > MAX_OPS) {
            ((ArrayDeque<EditOp>) undoStack).pollLast();
        }
        redoStack.clear();
    }

    /** Clear recorded ops (new editor session). */
    public void reset() {
        undoStack.clear();
        redoStack.clear();
        nativeUndoSupported = null;
        nativeRedoSupported = null;
    }

    // ------------------------------------------------------------------
    // Undo / Redo
    // ------------------------------------------------------------------

    /**
     * Perform undo. Returns a result describing what actually happened -
     * never claims success without confirmation.
     */
    public Result undo() {
        // 1. native first
        if (nativeUndoSupported == null || nativeUndoSupported) {
            boolean ok = icm.performContextAction(android.R.id.undo);
            nativeUndoSupported = ok;
            if (ok) return Result.SUCCESS;
        }
        // 2. local stack
        EditOp op = undoStack.pollFirst();
        if (op == null) {
            return nativeUndoSupported != null && !nativeUndoSupported
                    ? Result.UNSUPPORTED : Result.NOTHING_TO_UNDO;
        }
        InputConnection ic = icm.getConnection();
        if (ic == null) {
            undoStack.addFirst(op);
            return Result.NO_CONNECTION;
        }
        try {
            ic.beginBatchEdit();
            op.undo(ic);
            ic.endBatchEdit();
            redoStack.push(op);
            return Result.SUCCESS;
        } catch (Exception e) {
            undoStack.addFirst(op);
            return Result.FAILED;
        }
    }

    public Result redo() {
        if (nativeRedoSupported == null || nativeRedoSupported) {
            boolean ok = icm.performContextAction(android.R.id.redo);
            nativeRedoSupported = ok;
            if (ok) return Result.SUCCESS;
        }
        EditOp op = redoStack.pollFirst();
        if (op == null) {
            return nativeRedoSupported != null && !nativeRedoSupported
                    ? Result.UNSUPPORTED : Result.NOTHING_TO_REDO;
        }
        InputConnection ic = icm.getConnection();
        if (ic == null) {
            redoStack.addFirst(op);
            return Result.NO_CONNECTION;
        }
        try {
            ic.beginBatchEdit();
            op.redo(ic);
            ic.endBatchEdit();
            undoStack.push(op);
            return Result.SUCCESS;
        } catch (Exception e) {
            redoStack.addFirst(op);
            return Result.FAILED;
        }
    }

    public enum Result { SUCCESS, NO_CONNECTION, UNSUPPORTED, NOTHING_TO_UNDO, NOTHING_TO_REDO, FAILED }
}
