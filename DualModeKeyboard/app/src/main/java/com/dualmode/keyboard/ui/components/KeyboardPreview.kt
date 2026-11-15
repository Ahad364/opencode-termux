package com.dualmode.keyboard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.data.Language
import com.dualmode.keyboard.keyboard.KeyboardLayouts
import com.dualmode.keyboard.keyboard.KeyboardView

/**
 * Live interactive preview of the keyboard inside the settings app.
 * It renders the exact same KeyboardView used by the IME with the user's
 * current settings, and commits typed characters into the text field above.
 */
@Composable
fun KeyboardPreview(
    settings: KeyboardSettings,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Try the keyboard below") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                KeyboardView(context).apply {
                    listener = object : KeyboardView.Listener {
                        override fun onKey(key: com.dualmode.keyboard.keyboard.Key) {
                            when (key.action) {
                                com.dualmode.keyboard.keyboard.KeyAction.TEXT ->
                                    text += key.text
                                com.dualmode.keyboard.keyboard.KeyAction.SPACE ->
                                    text += " "
                                com.dualmode.keyboard.keyboard.KeyAction.BACKSPACE -> {
                                    if (text.isNotEmpty()) text = text.dropLast(1)
                                }
                                else -> {}
                            }
                        }
                    }
                }
            },
            update = { view ->
                view.applySettings(settings)
                view.rows = KeyboardLayouts.forState(
                    page = KeyboardLayouts.Page.LETTERS,
                    language = if (settings.defaultLanguage == Language.BENGALI)
                        Language.BENGALI else Language.ENGLISH,
                    shifted = false,
                    ctrlActive = false
                )
            }
        )
        Text(
            "Preview — this is the real keyboard renderer with your current settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
