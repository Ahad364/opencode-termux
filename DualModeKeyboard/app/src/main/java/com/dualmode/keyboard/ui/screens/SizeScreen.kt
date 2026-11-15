package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.KeyboardPreview
import com.dualmode.keyboard.ui.components.SliderRow

@Composable
fun SizeScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()

    ScreenScaffold(stringResource(R.string.size_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SliderRow(
                label = "Keyboard height",
                value = settings.keyHeightDp,
                valueText = "${settings.keyHeightDp.toInt()} dp/key",
                valueRange = 40f..74f,
                onValueChangeFinished = { viewModel.setKeyHeight(it) }
            )
            SliderRow(
                label = "Key corner radius",
                value = settings.keyCornerRadiusDp,
                valueText = "${settings.keyCornerRadiusDp.toInt()} dp",
                valueRange = 4f..24f,
                onValueChangeFinished = { viewModel.setCornerRadius(it) }
            )
            SliderRow(
                label = "Key spacing",
                value = settings.keySpacingDp,
                valueText = "${settings.keySpacingDp.toInt()} dp",
                valueRange = 2f..10f,
                onValueChangeFinished = { viewModel.setKeySpacing(it) }
            )
            SliderRow(
                label = "Key font size",
                value = settings.keyFontSizeSp,
                valueText = "${settings.keyFontSizeSp.toInt()} sp",
                valueRange = 12f..26f,
                onValueChangeFinished = { viewModel.setFontSize(it) }
            )
            KeyboardPreview(settings)
        }
    }
}
