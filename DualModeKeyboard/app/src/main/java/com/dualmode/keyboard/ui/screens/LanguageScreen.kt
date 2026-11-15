package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.data.Language
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.SectionTitle

@Composable
fun LanguageScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()

    ScreenScaffold(stringResource(R.string.language_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard {
                SectionTitle("Default startup layout")
                Text(
                    "You can also switch languages instantly with the EN / বাং key " +
                        "in the keyboard toolbar while typing.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            LanguageOption(
                title = "English (QWERTY)",
                description = "Standard QWERTY layout with shift, caps lock, " +
                    "long-press alternatives, numbers, symbols and emoji.",
                selected = settings.defaultLanguage == Language.ENGLISH,
                onClick = { viewModel.setLanguage(Language.ENGLISH) }
            )
            LanguageOption(
                title = "বাংলা (Bengali)",
                description = "Direct Bengali character layout: vowels, consonants, " +
                    "vowel signs, Bengali digits and punctuation. Shift gives extra " +
                    "consonants and Bengali numbers. (Character layout — not a " +
                    "phonetic transliteration keyboard.)",
                selected = settings.defaultLanguage == Language.BENGALI,
                onClick = { viewModel.setLanguage(Language.BENGALI) }
            )
        }
    }
}

@Composable
private fun LanguageOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.padding(start = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
