package com.dualmode.keyboard.shortcuts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputConnection

/**
 * Direct clipboard/selection operations performed through the InputConnection.
 *
 * Privacy: the clipboard is read ONLY when the user explicitly taps the Paste
 * key, and only the text of the focused editor is touched. The keyboard never
 * reads the clipboard in the background and never transmits anything.
 */
class ClipboardHandler(private val context: Context) {

    data class Result(val success: Boolean, val message: String)

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
        as? ClipboardManager

    fun copy(ic: InputConnection?): Result {
        val selected = ic?.getSelectedText(0)?.toString()
        if (selected.isNullOrEmpty()) {
            return Result(false, "Select text first, then tap Copy")
        }
        setClip(selected)
        return Result(true, "Copied ${selected.length} characters")
    }

    fun cut(ic: InputConnection?): Result {
        val selected = ic?.getSelectedText(0)?.toString()
        if (selected.isNullOrEmpty()) {
            return Result(false, "Select text first, then tap Cut")
        }
        setClip(selected)
        ic.commitText("", 1)
        return Result(true, "Cut ${selected.length} characters")
    }

    fun paste(ic: InputConnection?): Result {
        if (ic == null) return Result(false, "No text field focused")
        val text = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
        if (text.isNullOrEmpty()) {
            return Result(false, "Clipboard is empty or has no text")
        }
        ic.commitText(text, 1)
        return Result(true, "Pasted ${text.length} characters")
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
