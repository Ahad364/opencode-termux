/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.theme;

/**
 * The 12 built-in Coding Keys themes.
 */
public final class ThemeDefinitions {

    private ThemeDefinitions() {
    }

    /** Common defaults used as the base for the dark themes. */
    private static ThemeInfo base(String name) {
        ThemeInfo t = new ThemeInfo(name);
        t.keyTextColor = 0xFFF5F7FF;
        t.enableBorder = true;
        t.buttonBorderRadiusPx = 10.0f;
        t.buttonBodyPaddingPx = 5.0f;
        return t;
    }

    /** Theme 1 - default: premium blue. */
    public static ThemeInfo PremiumBlue() {
        ThemeInfo t = base("Premium Blue");
        t.backgroundColor = 0xFF0B1A3A;
        t.backgroundColorGradientEnd = 0xFF14295E;
        t.keyBackgroundColor = 0xFF1B2F5E;
        t.specialKeyBackgroundColor = 0xFF24407F;
        t.modifierKeyBackgroundColor = 0xFF2E4F9E;
        t.actionKeyBackgroundColor = 0xFF3E6BD6;
        return t;
    }

    /** Theme 2 - midnight blue. */
    public static ThemeInfo MidnightBlue() {
        ThemeInfo t = base("Midnight Blue");
        t.backgroundColor = 0xFF060D1F;
        t.keyBackgroundColor = 0xFF0E1B38;
        t.specialKeyBackgroundColor = 0xFF15254B;
        t.modifierKeyBackgroundColor = 0xFF1D3263;
        t.actionKeyBackgroundColor = 0xFF2A4C99;
        return t;
    }

    /** Theme 3 - deep purple. */
    public static ThemeInfo DeepPurple() {
        ThemeInfo t = base("Deep Purple");
        t.backgroundColor = 0xFF1A0B2E;
        t.keyBackgroundColor = 0xFF2A1650;
        t.specialKeyBackgroundColor = 0xFF371E68;
        t.modifierKeyBackgroundColor = 0xFF452781;
        t.actionKeyBackgroundColor = 0xFF6A3DD1;
        return t;
    }

    /** Theme 4 - cyber dark. */
    public static ThemeInfo CyberDark() {
        ThemeInfo t = base("Cyber Dark");
        t.backgroundColor = 0xFF02121B;
        t.keyBackgroundColor = 0xFF062331;
        t.specialKeyBackgroundColor = 0xFF0A3242;
        t.modifierKeyBackgroundColor = 0xFF0E4459;
        t.actionKeyBackgroundColor = 0xFF00B8D4;
        t.keyTextColor = 0xFF7CFFCB;
        return t;
    }

    /** Theme 5 - AMOLED black. */
    public static ThemeInfo AmoledBlack() {
        ThemeInfo t = base("AMOLED Black");
        t.backgroundColor = 0xFF000000;
        t.keyBackgroundColor = 0xFF0A0A0A;
        t.specialKeyBackgroundColor = 0xFF141414;
        t.modifierKeyBackgroundColor = 0xFF1F1F1F;
        t.actionKeyBackgroundColor = 0xFF3D5AFE;
        return t;
    }

    /** Theme 6 - pure black. */
    public static ThemeInfo PureBlack() {
        ThemeInfo t = base("Pure Black");
        t.backgroundColor = 0xFF000000;
        t.keyBackgroundColor = 0xFF000000;
        t.specialKeyBackgroundColor = 0xFF101010;
        t.modifierKeyBackgroundColor = 0xFF202020;
        t.actionKeyBackgroundColor = 0xFF383838;
        t.enableBorder = false;
        return t;
    }

    /** Theme 7 - material dark. */
    public static ThemeInfo MaterialDark() {
        ThemeInfo t = base("Material Dark");
        t.backgroundColor = 0xFF263238;
        t.keyBackgroundColor = 0xFF2E3C43;
        t.specialKeyBackgroundColor = 0xFF37474F;
        t.modifierKeyBackgroundColor = 0xFF455A64;
        t.actionKeyBackgroundColor = 0xFF546E7A;
        return t;
    }

    /** Theme 8 - material light. */
    public static ThemeInfo MaterialLight() {
        ThemeInfo t = base("Material Light");
        t.backgroundColor = 0xFFECEFF1;
        t.keyBackgroundColor = 0xFFF5F5F5;
        t.specialKeyBackgroundColor = 0xFFE0E0E0;
        t.modifierKeyBackgroundColor = 0xFFCFD8DC;
        t.actionKeyBackgroundColor = 0xFF2962FF;
        t.keyTextColor = 0xFF263238;
        return t;
    }

    /** Theme 9 - ocean blue. */
    public static ThemeInfo OceanBlue() {
        ThemeInfo t = base("Ocean Blue");
        t.backgroundColor = 0xFF00243D;
        t.backgroundColorGradientEnd = 0xFF00587C;
        t.keyBackgroundColor = 0xFF003455;
        t.specialKeyBackgroundColor = 0xFF004A70;
        t.modifierKeyBackgroundColor = 0xFF00638D;
        t.actionKeyBackgroundColor = 0xFF00B0FF;
        return t;
    }

    /** Theme 10 - neon purple. */
    public static ThemeInfo NeonPurple() {
        ThemeInfo t = base("Neon Purple");
        t.backgroundColor = 0xFF0D0018;
        t.keyBackgroundColor = 0xFF1A0033;
        t.specialKeyBackgroundColor = 0xFF28004D;
        t.modifierKeyBackgroundColor = 0xFF3A006E;
        t.actionKeyBackgroundColor = 0xFFD500F9;
        t.keyTextColor = 0xFFEACBFF;
        return t;
    }

    /** Theme 11 - premium gradient (navy to purple). */
    public static ThemeInfo PremiumGradient() {
        ThemeInfo t = base("Premium Gradient");
        t.backgroundColor = 0xFF0B1A3A;
        t.backgroundColorGradientEnd = 0xFF3A1B5E;
        t.keyBackgroundColor = 0xFF1B2F5E;
        t.specialKeyBackgroundColor = 0xFF2C2A66;
        t.modifierKeyBackgroundColor = 0xFF3D3583;
        t.actionKeyBackgroundColor = 0xFF7C4DFF;
        return t;
    }

    /** Theme 12 - user-configurable custom theme. */
    public static ThemeInfo Custom() {
        ThemeInfo t = base("Custom");
        t.backgroundColor = 0xFF10131A;
        t.keyBackgroundColor = 0xFF1E2530;
        t.specialKeyBackgroundColor = 0xFF2A3441;
        t.modifierKeyBackgroundColor = 0xFF37465A;
        t.actionKeyBackgroundColor = 0xFF5C6BC0;
        return t;
    }

    public static ThemeInfo byIndex(int index) {
        switch (index) {
            case 1: return MidnightBlue();
            case 2: return DeepPurple();
            case 3: return CyberDark();
            case 4: return AmoledBlack();
            case 5: return PureBlack();
            case 6: return MaterialDark();
            case 7: return MaterialLight();
            case 8: return OceanBlue();
            case 9: return NeonPurple();
            case 10: return PremiumGradient();
            case 11: return Custom();
            case 0:
            default: return PremiumBlue();
        }
    }

    public static int count() {
        return 12;
    }
}
