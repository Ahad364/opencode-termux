package com.dualmode.keyboard.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dualmode.keyboard.KeyboardApplication
import com.dualmode.keyboard.data.KeyboardMode
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.data.Language
import com.dualmode.keyboard.data.SettingsDefaults
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** MVVM view model for the settings dashboard. All persistence is local. */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository =
        (app as KeyboardApplication).settingsRepository

    val settings: StateFlow<KeyboardSettings> = repository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsDefaults.SETTINGS
    )

    private fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        viewModelScope.launch { repository.update(transform) }
    }

    fun setTheme(id: String) = update { it.copy(themeId = id) }
    fun setAccent(color: Int?) = update { it.copy(accentOverride = color) }
    fun setKeyBackground(color: Int?) = update { it.copy(keyBackgroundOverride = color) }
    fun setKeyText(color: Int?) = update { it.copy(keyTextOverride = color) }
    fun setKeyboardBackground(color: Int?) =
        update { it.copy(keyboardBackgroundOverride = color) }

    fun setKeyHeight(dp: Float) = update { it.copy(keyHeightDp = dp) }
    fun setCornerRadius(dp: Float) = update { it.copy(keyCornerRadiusDp = dp) }
    fun setKeySpacing(dp: Float) = update { it.copy(keySpacingDp = dp) }
    fun setFontSize(sp: Float) = update { it.copy(keyFontSizeSp = sp) }

    fun setSound(enabled: Boolean) = update { it.copy(soundEnabled = enabled) }
    fun setSoundVolume(value: Int) = update { it.copy(soundVolume = value) }
    fun setHaptics(enabled: Boolean) = update { it.copy(hapticsEnabled = enabled) }
    fun setHapticStrength(value: Int) = update { it.copy(hapticStrength = value) }
    fun setAnimations(enabled: Boolean) = update { it.copy(animationsEnabled = enabled) }

    fun setLanguage(language: Language) = update { it.copy(defaultLanguage = language) }
    fun setDefaultMode(mode: KeyboardMode) = update { it.copy(defaultMode = mode) }
}
