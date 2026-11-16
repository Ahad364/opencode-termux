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
package com.codingkeys.keyboard.layout;

import android.graphics.drawable.Drawable;

/**
 * Blueprint describing how to build a single key.
 */
public class KeyInfo {

    /** Visual role used by the theme engine to pick colors. */
    public enum Role {
        /** Regular character/number key. */
        CHARACTER,
        /** Special key (tab, arrows, editing actions...). */
        SPECIAL,
        /** Modifier key (shift, ctrl, alt...). */
        MODIFIER,
        /** Action key (enter). */
        ACTION
    }

    /** Key press code sent when pressing the key (see KeyboardDefinitions). */
    public int code;

    /** Label shown on the keyboard. */
    public String label;

    /** Size relative to other keys in the same row. */
    public float size;

    /** Key can be held to repeat. */
    public boolean isRepeatable;

    /** This key is a modifier (Shift/Ctrl/Alt) with a latching state. */
    public boolean isModifier;

    /** When key is pressed output this text (used for multi-char labels). */
    public String outputText;

    /** When shift modifier is pressed, show this label instead. */
    public String onShiftLabel;

    /** When control modifier is pressed, show this label instead. */
    public String onCtrlLabel;

    /** When alt modifier is pressed, show this label instead. */
    public String onAltLabel;

    /** Drawable shown instead of a text label. */
    public Drawable icon;

    /** Visual role of this key. */
    public Role role = Role.CHARACTER;
}
