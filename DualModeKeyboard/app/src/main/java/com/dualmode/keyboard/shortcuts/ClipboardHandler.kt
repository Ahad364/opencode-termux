package com.dualmode.keyboard.shortcuts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.ExtractedTextRequest
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

    fun selectAll(ic: InputConnection?): Result {
        if (ic == null) return Result(false, "No text field focused")
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
        val full = extracted?.text?.toString()
            ?: (ic.getTextBeforeCursor(MAX_TEXT, 0)?.toString()
                + ic.getSelectedText(0)?.toString()
                + ic.getTextAfterCursor(MAX_TEXT, 0)?.toString())
        if (full.isNullOrEmpty()) {
            return Result(false, "No text to select")
        }
        ic.setSelection(0, full.length)
        return Result(true, "Selected all ${full.length} characters")
    }

    private fun setClip(text: String) {
        clipboard?.setPrimaryClip(ClipData.newPlainText("text", text))
    }

    companion object {
        private const val MAX_TEXT = 10_000
    }
}
