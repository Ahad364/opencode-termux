package com.dualmode.keyboard.shortcuts

import android.view.inputmethod.InputConnection

/**
 * Routes shortcut keys to the right handler and prevents accidental repeated
 * execution of the same shortcut within a short window.
 */
class ShortcutExecutor(private val clipboard: ClipboardHandler) {

    data class Result(val success: Boolean, val message: String)

    private val lastExecuted = mutableMapOf<ShortcutId, Long>()

    fun execute(ic: InputConnection?, id: ShortcutId): Result {
        val now = android.os.SystemClock.uptimeMillis()
        val last = lastExecuted[id] ?: 0L
        if (now - last < DEBOUNCE_MS) {
            return Result(false, "Slow down — ${id.description} just ran")
        }
        lastExecuted[id] = now

        return when (id) {
            // Direct InputConnection implementations (work in normal editors).
            ShortcutId.COPY -> clipboard.copy(ic).let { Result(it.success, it.message) }
            ShortcutId.CUT -> clipboard.cut(ic).let { Result(it.success, it.message) }
            ShortcutId.PASTE -> clipboard.paste(ic).let { Result(it.success, it.message) }
            ShortcutId.SELECT_ALL -> clipboard.selectAll(ic).let { Result(it.success, it.message) }

            // Key-event based: effect depends on the focused app.
            ShortcutId.UNDO,
            ShortcutId.REDO,
            ShortcutId.BOLD,
            ShortcutId.ITALIC,
            ShortcutId.CENTER,
            ShortcutId.SAVE,
            ShortcutId.PRINT,
            ShortcutId.OPEN,
            ShortcutId.NEW_DOC,
            ShortcutId.FIND -> {
                val delivered = KeyEventSender.sendCtrlCombo(ic, id)
                if (delivered) {
                    Result(
                        success = true,
                        message = "${id.comboLabel} sent — works in apps that support it"
                    )
                } else {
                    Result(false, "No text field focused")
                }
            }
        }
    }

    companion object {
        private const val DEBOUNCE_MS = 350L
    }
}
