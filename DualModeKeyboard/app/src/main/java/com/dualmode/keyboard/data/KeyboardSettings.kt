package com.dualmode.keyboard.data

import com.dualmode.keyboard.themes.Themes

/**
 * Core user-facing settings for the keyboard. Persisted with Jetpack DataStore
 * and shared between the settings dashboard (Compose UI) and the IME service.
 */
data class KeyboardSettings(
    val themeId: String = Themes.DEFAULT_THEME_ID,
    // Optional per-element color overrides (ARGB ints, null = use theme color).
    val accentOverride: Int? = null,
    val keyBackgroundOverride: Int? = null,
    val keyTextOverride: Int? = null,
    val keyboardBackgroundOverride: Int? = null,
    // Dimensions.
    val keyHeightDp: Float = 54f,
    val keyCornerRadiusDp: Float = 14f,
    val keySpacingDp: Float = 4f,
    val keyFontSizeSp: Float = 18f,
    // Feedback.
    val soundEnabled: Boolean = true,
    val soundVolume: Int = 50, // 0..100
    val hapticsEnabled: Boolean = true,
    val hapticStrength: Int = 40, // 0..100
    val animationsEnabled: Boolean = true,
    // Behavior.
    val defaultLanguage: Language = Language.ENGLISH,
    val defaultMode: KeyboardMode = KeyboardMode.NORMAL,
    // Mode the keyboard was last used in. Restored on keyboard open so the
    // user's preferred mode is remembered across sessions.
    val lastMode: KeyboardMode = defaultMode
)

enum class KeyboardMode { NORMAL, SHORTCUT }

enum class Language { ENGLISH, BENGALI }

object SettingsDefaults {
    val SETTINGS = KeyboardSettings()
}
