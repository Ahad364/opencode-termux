package com.dualmode.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Understands the focused editor: which layout to show, whether the field is
 * sensitive, and how the Enter key should behave.
 */
object EditorHandler {

    enum class FieldType { TEXT, NUMBER, PHONE, EMAIL, PASSWORD, URI, DATETIME }

    data class EditorProfile(
        val type: FieldType,
        val multiline: Boolean,
        val imeAction: Int
    )

    fun analyze(info: EditorInfo?): EditorProfile {
        if (info == null) return EditorProfile(FieldType.TEXT, multiline = false, imeAction = 0)
        val inputType = info.inputType
        val cls = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        val type = when {
            cls == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD -> FieldType.PASSWORD
            cls == InputType.TYPE_CLASS_TEXT &&
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD -> FieldType.PASSWORD
            cls == InputType.TYPE_CLASS_TEXT &&
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> FieldType.PASSWORD
            cls == InputType.TYPE_CLASS_TEXT &&
                variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> FieldType.EMAIL
            cls == InputType.TYPE_CLASS_TEXT &&
                variation == InputType.TYPE_TEXT_VARIATION_URI -> FieldType.URI
            cls == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_NORMAL -> FieldType.NUMBER
            cls == InputType.TYPE_CLASS_PHONE -> FieldType.PHONE
            cls == InputType.TYPE_CLASS_DATETIME -> FieldType.DATETIME
            else -> FieldType.TEXT
        }
        val multiline = cls == InputType.TYPE_CLASS_TEXT &&
            (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        // Only treat concrete editor actions as actions; IME_NULL/UNSPECIFIED/NONE
        // mean the editor has no meaningful action to perform.
        val action = info.actionId
        val knownAction = action in intArrayOf(
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_PREVIOUS,
            EditorInfo.IME_ACTION_DONE
        )
        return EditorProfile(
            type,
            multiline,
            imeAction = if (knownAction) action else EditorInfo.IME_ACTION_UNSPECIFIED
        )
    }

    fun initialPage(profile: EditorProfile): KeyboardLayouts.Page =
        when (profile.type) {
            FieldType.NUMBER, FieldType.PHONE, FieldType.DATETIME ->
                KeyboardLayouts.Page.NUMBERS
            else -> KeyboardLayouts.Page.LETTERS
        }

    /**
     * Enter key behavior: trigger the editor's action when it declares one
     * (Go/Search/Send/Done…), otherwise insert a newline for multiline fields
     * or send a real Enter key event.
     */
    fun handleEnter(profile: EditorProfile, ic: InputConnection?) {
        if (ic == null) return
        val hasAction = profile.imeAction != EditorInfo.IME_ACTION_UNSPECIFIED &&
            profile.imeAction != EditorInfo.IME_ACTION_NONE
        if (!profile.multiline && hasAction) {
            ic.performEditorAction(profile.imeAction)
            return
        }
        if (profile.multiline) {
            ic.commitText("\n", 1)
        } else {
            sendEnter(ic)
        }
    }

    private fun sendEnter(ic: InputConnection) {
        val now = System.currentTimeMillis()
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0))
        ic.sendKeyEvent(KeyEvent(now + 5, now + 5, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0))
    }
}
