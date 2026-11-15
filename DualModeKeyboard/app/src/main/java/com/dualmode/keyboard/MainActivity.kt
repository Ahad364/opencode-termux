package com.dualmode.keyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.ui.App

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SettingsViewModel = viewModel()
            AppTheme {
                App(viewModel)
            }
        }
    }
}
