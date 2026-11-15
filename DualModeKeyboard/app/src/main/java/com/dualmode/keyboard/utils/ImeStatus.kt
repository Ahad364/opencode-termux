package com.dualmode.keyboard.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import com.dualmode.keyboard.ime.KeyboardService

/**
 * Checks the real system state: whether this IME is enabled and whether it is
 * the currently selected default keyboard. The app cannot change these itself
 * — the user must do it in Android Settings; these helpers only read state.
 */
object ImeStatus {

    private fun component(context: Context) =
        ComponentName(context, KeyboardService::class.java)

    fun isEnabled(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        return imm?.enabledInputMethodList
            ?.any { it.packageName == context.packageName } == true
    }

    fun isDefault(context: Context): Boolean {
        val current = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: return false
        val component = component(context)
        // IME ids can be stored as either "pkg/.Class" or the fully flattened
        // component name, so match on the package prefix + service class name.
        val serviceClass = KeyboardService::class.java.name
        return current.startsWith("${context.packageName}/") &&
            current.endsWith(serviceClass.substringAfterLast('.'))
    }

    fun openImeSettings(context: Context) {
        context.startActivity(
            android.content.Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun showImePicker(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }
}
