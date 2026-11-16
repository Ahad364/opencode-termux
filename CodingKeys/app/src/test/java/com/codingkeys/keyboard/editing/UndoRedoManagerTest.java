/*
 * Coding Keys - unit tests.
 * Copyright (C) 2026 Coding Keys contributors
 */
package com.codingkeys.keyboard.editing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.inputmethod.InputConnection;

import com.codingkeys.keyboard.core.InputConnectionManager;

import org.junit.Test;

/**
 * Tests the bounded local undo fallback with a fake InputConnection.
 * The fake reports false for performContextMenuAction so the manager uses
 * its local operation stack (the same path editors without native undo take).
 */
public class UndoRedoManagerTest {

    private static class FakeInputConnection implements InputConnection {
        CharSequence lastCommitted = null;
        int lastSelStart = -1;
        int lastSelEnd = -1;
        int deletedBackward = 0;
        int deletedForward = 0;

        @Override public CharSequence getTextBeforeCursor(int n, int flags) { return ""; }
        @Override public CharSequence getTextAfterCursor(int n, int flags) { return ""; }
        @Override public CharSequence getSelectedText(int flags) { return null; }
        @Override public int getCursorCapsMode(int reqModes) { return 0; }
        @Override public android.view.inputmethod.ExtractedText getExtractedText(
                android.view.inputmethod.ExtractedTextRequest request, int flags) { return null; }
        @Override public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            deletedBackward += beforeLength;
            deletedForward += afterLength;
            return true;
        }
        @Override public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
            return true;
        }
        @Override public boolean setComposingText(CharSequence text, int newCursorPosition) { return true; }
        @Override public boolean setComposingRegion(int start, int end) { return true; }
        @Override public boolean finishComposingText() { return true; }
        @Override public boolean commitText(CharSequence text, int newCursorPosition) {
            lastCommitted = text;
            return true;
        }
        @Override public boolean commitCorrection(android.view.inputmethod.CorrectionInfo correctionInfo) {
            return true;
        }
        @Override public boolean commitCompletion(android.view.inputmethod.CompletionInfo completion) {
            return true;
        }
        @Override public android.os.Handler getHandler() { return null; }
        @Override public boolean setSelection(int start, int end) {
            lastSelStart = start;
            lastSelEnd = end;
            return true;
        }
        @Override public boolean performEditorAction(int actionCode) { return true; }
        @Override public boolean performContextMenuAction(int id) { return false; } // no native undo
        @Override public boolean beginBatchEdit() { return true; }
        @Override public boolean endBatchEdit() { return true; }
        @Override public boolean sendKeyEvent(android.view.KeyEvent event) { return true; }
        @Override public boolean clearMetaKeyStates(int states) { return true; }
        @Override public boolean reportFullscreenMode(boolean enabled) { return true; }
        @Override public boolean performPrivateCommand(String action, android.os.Bundle data) {
            return true;
        }
        @Override public boolean requestCursorUpdates(int cursorUpdateMode) { return true; }
        @Override public boolean commitContent(
                android.view.inputmethod.InputContentInfo inputContentInfo, int flags, android.os.Bundle opts) {
            return true;
        }
        @Override public void closeConnection() { }
    }

    private InputConnectionManager manager(FakeInputConnection ic) {
        return new InputConnectionManager(() -> ic);
    }

    @Test
    public void undo_restoresCommittedText() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        undoRedo.recordCommit(4, "abc", "x");
        // Simulated typing replaced "abc" with "x" at offset 4.

        UndoRedoManager.Result result = undoRedo.undo();
        assertEquals(UndoRedoManager.Result.SUCCESS, result);
        assertEquals("abc", fake.lastCommitted.toString());
        assertEquals(4, fake.lastSelStart);
        assertEquals(5, fake.lastSelEnd);
    }

    @Test
    public void redo_reappliesCommittedText() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        undoRedo.recordCommit(4, "abc", "x");
        undoRedo.undo();

        UndoRedoManager.Result result = undoRedo.redo();
        assertEquals(UndoRedoManager.Result.SUCCESS, result);
        assertEquals("x", fake.lastCommitted.toString());
        assertEquals(4, fake.lastSelStart);
        assertEquals(7, fake.lastSelEnd);
    }

    @Test
    public void undo_backwardDelete_restoresChar() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        undoRedo.recordBackwardDelete(3, "a");

        UndoRedoManager.Result result = undoRedo.undo();
        assertEquals(UndoRedoManager.Result.SUCCESS, result);
        assertEquals("a", fake.lastCommitted.toString());
    }

    @Test
    public void redo_backwardDelete_deletesCharAgain() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        undoRedo.recordBackwardDelete(3, "a");
        undoRedo.undo();

        UndoRedoManager.Result result = undoRedo.redo();
        assertEquals(UndoRedoManager.Result.SUCCESS, result);
        assertEquals(1, fake.deletedBackward);
    }

    @Test
    public void undo_forwardDelete_restoresChar() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        undoRedo.recordForwardDelete(3, "b");

        UndoRedoManager.Result result = undoRedo.undo();
        assertEquals(UndoRedoManager.Result.SUCCESS, result);
        assertEquals("b", fake.lastCommitted.toString());
    }

    @Test
    public void emptyStack_reportsNothingToUndo() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        UndoRedoManager.Result result = undoRedo.undo();
        // The fake editor does not support native undo either.
        assertEquals(UndoRedoManager.Result.UNSUPPORTED, result);
    }

    @Test
    public void oversizedOps_areNotRecorded() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        char[] big = new char[UndoRedoManager.MAX_OP_TEXT_LENGTH + 1];
        java.util.Arrays.fill(big, 'a');
        undoRedo.recordCommit(0, new String(big), "x");

        UndoRedoManager.Result result = undoRedo.undo();
        // Nothing recorded locally and no native support -> unsupported.
        assertEquals(UndoRedoManager.Result.UNSUPPORTED, result);
    }

    @Test
    public void newCommit_clearsRedoStack() {
        FakeInputConnection fake = new FakeInputConnection();
        UndoRedoManager undoRedo = new UndoRedoManager(manager(fake));
        undoRedo.recordCommit(0, "a", "b");
        undoRedo.undo();
        undoRedo.recordCommit(1, "", "c");
        UndoRedoManager.Result result = undoRedo.redo();
        assertEquals(UndoRedoManager.Result.UNSUPPORTED, result);
    }
}
