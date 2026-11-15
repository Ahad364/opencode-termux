package com.dualmode.keyboard.shortcuts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputConnection

/**
 * Direct clipboard/selection operations performed through the InputConnection.
 *
 * Strategy: the EDITOR performs every action itself via its standard
 * context-menu actions (android.R.id.copy/cut/paste/selectAll) — the same
 * reliable mechanism as Select All — with a direct InputConnection fallback
 * for editors that do not handle those actions.
 *
 * Privacy: on the native paste path the editor reads the clipboard itself;
 * the fallback path reads the clipboard ONLY when the user explicitly taps
 * the Paste key. The keyboard never reads the clipboard in the background
 * and never transmits anything.
 */
class ClipboardHandler(private val context: Context) {

    data class Result(val success: Boolean, val message: String)

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
        as? ClipboardManager

    fun copy(ic: InputConnection?): Result {
        if (ic == null) return Result(false, "No text field focused")
        val selected = ic.getSelectedText(0)?.toString()
        when {
            // Selection visible to the keyboard: the editor performs its own
            // Copy (most compatible path); direct clipboard write as fallback.
            !selected.isNullOrEmpty() -> {
                return if (ic.performContextMenuAction(android.R.id.copy)) {
                    Result(true, "Copied selection via the editor")
                } else {
                    setClip(selected)
                    Result(true, "Copied ${selected.length} characters")
                }
            }
            // The editor exposes selection state and nothing is selected.
            selected != null ->
                return Result(false, "Select text first (tap Ctrl+A to select all)")
        }
        // The editor hides its selection from the keyboard: hand Copy to the
        // editor and report honestly that it decided the outcome.
        return if (ic.performContextMenuAction(android.R.id.copy)) {
            Result(true, "Copy performed by the editor")
        } else {
            Result(false, "Select text first (tap Ctrl+A to select all)")
        }
    }

    fun cut(ic: InputConnection?): Result {
        if (ic == null) return Result(false, "No text field focused")
        val selected = ic.getSelectedText(0)?.toString()
        when {
            !selected.isNullOrEmpty() -> {
                return if (ic.performContextMenuAction(android.R.id.cut)) {
                    Result(true, "Cut selection via the editor")
                } else {
                    setClip(selected)
                    ic.commitText("", 1)
                    Result(true, "Cut ${selected.length} characters")
                }
            }
            selected != null ->
                return Result(false, "Select text first (tap Ctrl+A to select all)")
        }
        return if (ic.performContextMenuAction(android.R.id.cut)) {
            Result(true, "Cut performed by the editor")
        } else {
            Result(false, "Select text first (tap Ctrl+A to select all)")
        }
    }

    fun paste(ic: InputConnection?): Result {
        if (ic == null) return Result(false, "No text field focused")
        // Check emptiness first so an empty clipboard is reported honestly —
        // the editor's menu action would silently no-op. Read happens only
        // because the user explicitly tapped Paste.
        val text = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
        if (text.isNullOrEmpty()) {
            return Result(false, "Clipboard is empty or has no text")
        }
        // The editor's own Paste (keeps its formatting/behavior); direct
        // insertion as fallback.
        return if (ic.performContextMenuAction(android.R.id.paste)) {
            Result(true, "Pasted via the editor")
        } else {
            ic.commitText(text, 1)
            Result(true, "Pasted ${text.length} characters")
        }
    }

    /**
     * Select all: the EDITOR performs the selection itself — the keyboard
     * never extracts the document into its own memory, so there is no
     * character or line limit, no matter how long the text, code or document
     * is. Two mechanisms are tried, in order:
     *
     * 1. The standard "Select all" context-menu action, honored by
     *    EditText-based editors. Its boolean result is a direct signal of
     *    whether the editor handled it (no false success reports).
     * 2. A real Ctrl+A key event with the CTRL meta state, honored by most
     *    other editors. Delivery is confirmed; the actual effect depends on
     *    the focused app and is reported honestly as such.
     *
     * The sticky Ctrl state is reset by the IME service after execution.
     */
    fun selectAll(ic: InputConnection?): Result {
        if (ic == null) return Result(false, "No text field focused")
        if (ic.performContextMenuAction(android.R.id.selectAll)) {
            return Result(true, "Selected all editor content")
        }
        return if (KeyEventSender.sendCtrlKeyCode(ic, "KEYCODE_A")) {
            Result(true, "Ctrl+A sent — editor selects all supported content")
        } else {
            Result(false, "Editor did not accept the select-all command")
        }
    }

    private fun setClip(text: String) {
        clipboard?.setPrimaryClip(ClipData.newPlainText("text", text))
    }
}
