package com.dualmode.keyboard.themes

import com.dualmode.keyboard.data.KeyboardSettings

/**
 * A keyboard color theme. Colors are ARGB ints so that the same theme can be
 * used both by the Canvas-based IME keyboard view and by Jetpack Compose.
 */
data class KeyboardTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val keyboardBackground: Int,
    val keyBackground: Int,
    val keyBackgroundPressed: Int,
    val functionKeyBackground: Int,
    val keyText: Int,
    val functionKeyText: Int,
    val accent: Int,
    val accentText: Int,
    val shadowColor: Int
)

object Themes {

    const val DEFAULT_THEME_ID = "premium_dark"

    private fun argb(a: Int, r: Int, g: Int, b: Int) = (a shl 24) or (r shl 16) or (g shl 8) or b

    val PREMIUM_DARK = KeyboardTheme(
        id = "premium_dark",
        name = "Premium Dark",
        isDark = true,
        keyboardBackground = argb(255, 20, 24, 36),
        keyBackground = argb(255, 44, 52, 70),
        keyBackgroundPressed = argb(255, 64, 76, 100),
        functionKeyBackground = argb(255, 32, 38, 52),
        keyText = argb(255, 235, 240, 250),
        functionKeyText = argb(255, 170, 182, 205),
        accent = argb(255, 66, 133, 244),
        accentText = argb(255, 255, 255, 255),
        shadowColor = argb(90, 0, 0, 0)
    )

    val PREMIUM_LIGHT = KeyboardTheme(
        id = "premium_light",
        name = "Premium Light",
        isDark = false,
        keyboardBackground = argb(255, 238, 240, 246),
        keyBackground = argb(255, 255, 255, 255),
        keyBackgroundPressed = argb(255, 214, 224, 245),
        functionKeyBackground = argb(255, 216, 220, 232),
        keyText = argb(255, 28, 32, 44),
        functionKeyText = argb(255, 70, 78, 96),
        accent = argb(255, 43, 106, 235),
        accentText = argb(255, 255, 255, 255),
        shadowColor = argb(40, 10, 14, 30)
    )

    val MIDNIGHT_BLUE = KeyboardTheme(
        id = "midnight_blue",
        name = "Midnight Blue",
        isDark = true,
        keyboardBackground = argb(255, 10, 16, 34),
        keyBackground = argb(255, 26, 40, 78),
        keyBackgroundPressed = argb(255, 44, 66, 122),
        functionKeyBackground = argb(255, 18, 28, 58),
        keyText = argb(255, 214, 228, 255),
        functionKeyText = argb(255, 140, 168, 220),
        accent = argb(255, 77, 148, 255),
        accentText = argb(255, 255, 255, 255),
        shadowColor = argb(90, 0, 0, 0)
    )

    val PURPLE_NEON = KeyboardTheme(
        id = "purple_neon",
        name = "Purple Neon",
        isDark = true,
        keyboardBackground = argb(255, 24, 14, 40),
        keyBackground = argb(255, 58, 34, 92),
        keyBackgroundPressed = argb(255, 92, 56, 140),
        functionKeyBackground = argb(255, 40, 24, 66),
        keyText = argb(255, 240, 226, 255),
        functionKeyText = argb(255, 196, 168, 235),
        accent = argb(255, 190, 92, 255),
        accentText = argb(255, 255, 255, 255),
        shadowColor = argb(90, 0, 0, 0)
    )

    val ELECTRIC_BLUE = KeyboardTheme(
        id = "electric_blue",
        name = "Electric Blue",
        isDark = true,
        keyboardBackground = argb(255, 8, 20, 34),
        keyBackground = argb(255, 20, 54, 92),
        keyBackgroundPressed = argb(255, 34, 88, 140),
        functionKeyBackground = argb(255, 14, 38, 64),
        keyText = argb(255, 220, 240, 255),
        functionKeyText = argb(255, 140, 190, 235),
        accent = argb(255, 0, 170, 255),
        accentText = argb(255, 6, 20, 34),
        shadowColor = argb(90, 0, 0, 0)
    )

    val MINIMAL_BLACK = KeyboardTheme(
        id = "minimal_black",
        name = "Minimal Black",
        isDark = true,
        keyboardBackground = argb(255, 8, 8, 10),
        keyBackground = argb(255, 34, 34, 38),
        keyBackgroundPressed = argb(255, 58, 58, 64),
        functionKeyBackground = argb(255, 22, 22, 26),
        keyText = argb(255, 232, 232, 238),
        functionKeyText = argb(255, 150, 150, 158),
        accent = argb(255, 230, 230, 235),
        accentText = argb(255, 10, 10, 12),
        shadowColor = argb(90, 0, 0, 0)
    )

    val ALL = listOf(
        PREMIUM_DARK,
        PREMIUM_LIGHT,
        MIDNIGHT_BLUE,
        PURPLE_NEON,
        ELECTRIC_BLUE,
        MINIMAL_BLACK
    )

    /** Preset swatches offered for per-element color customization. */
    val CUSTOM_COLORS: List<Int> = listOf(
        argb(255, 66, 133, 244),   // blue
        argb(255, 0, 170, 255),    // electric blue
        argb(255, 190, 92, 255),   // purple
        argb(255, 235, 84, 141),   // pink
        argb(255, 255, 152, 0),    // amber
        argb(255, 76, 217, 150),   // green
        argb(255, 41, 44, 52),     // graphite
        argb(255, 255, 255, 255),  // white
        argb(255, 16, 18, 26),     // near black
        argb(255, 58, 66, 88)      // navy gray
    )

    fun byId(id: String): KeyboardTheme = ALL.firstOrNull { it.id == id } ?: PREMIUM_DARK

    /**
     * Resolves the effective theme for the given settings, applying any
     * per-element color overrides chosen by the user.
     */
    fun resolve(settings: KeyboardSettings): KeyboardTheme {
        val base = byId(settings.themeId)
        if (settings.accentOverride == null && settings.keyBackgroundOverride == null &&
            settings.keyTextOverride == null && settings.keyboardBackgroundOverride == null
        ) {
            return base
        }
        return base.copy(
            keyboardBackground = settings.keyboardBackgroundOverride ?: base.keyboardBackground,
            keyBackground = settings.keyBackgroundOverride ?: base.keyBackground,
            keyBackgroundPressed = settings.keyBackgroundOverride?.let { pressed(it) }
                ?: base.keyBackgroundPressed,
            functionKeyBackground = settings.keyBackgroundOverride?.let { dimmed(it) }
                ?: base.functionKeyBackground,
            keyText = settings.keyTextOverride ?: base.keyText,
            accent = settings.accentOverride ?: base.accent
        )
    }

    private fun pressed(color: Int): Int {
        val f = 1.25f
        fun ch(c: Int) = ((c * f).toInt()).coerceAtMost(255)
        val r = ch((color shr 16) and 0xFF)
        val g = ch((color shr 8) and 0xFF)
        val b = ch(color and 0xFF)
        return argb(255, r, g, b)
    }

    private fun dimmed(color: Int): Int {
        val f = 0.72f
        val r = (((color shr 16) and 0xFF) * f).toInt()
        val g = (((color shr 8) and 0xFF) * f).toInt()
        val b = ((color and 0xFF) * f).toInt()
        return argb(255, r, g, b)
    }
}
