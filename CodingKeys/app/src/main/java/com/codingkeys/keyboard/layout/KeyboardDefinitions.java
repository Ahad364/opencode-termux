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

import android.content.Context;

import com.codingkeys.keyboard.R;

import java.util.List;

/**
 * Central key-code table and dynamic keyboard page definitions.
 *
 * Key codes follow the CodeBoard scheme (proven to work across editors):
 * - Standard Android keycodes for ESC(27), TAB(9), SHIFT(16), CTRL(17), ALT(57), SPACE(32)
 * - Android DEL(-5) / ENTER(-4) conventions
 * - Editing actions routed through InputConnection context menu actions (53737..53742)
 * - Custom codes 5000+ for navigation keys
 */
public class KeyboardDefinitions {

    // ---- Editing actions (editor-side, via performContextMenuAction) ----
    public static final int CODE_SELECT_ALL = 53737;
    public static final int CODE_CUT = 53738;
    public static final int CODE_COPY = 53739;
    public static final int CODE_PASTE = 53740;
    public static final int CODE_UNDO = 53741;
    public static final int CODE_REDO = 53742;

    // ---- Standard keys ----
    public static final int CODE_ESCAPE = 27;
    public static final int CODE_TAB = 9;
    public static final int CODE_SHIFT = 16;      // KEYCODE_SHIFT_LEFT
    public static final int CODE_CTRL = 17;       // KEYCODE_CTRL_LEFT
    public static final int CODE_ALT = 57;        // KEYCODE_ALT_LEFT
    public static final int CODE_SPACE = 32;
    public static final int CODE_BACKSPACE = -5;  // KEYCODE_DEL
    public static final int CODE_ENTER = -4;      // KEYCODE_ENTER
    public static final int CODE_IME_SWITCH = -13;

    // ---- Custom navigation codes ----
    public static final int CODE_ARROW_LEFT = 5000;
    public static final int CODE_ARROW_DOWN = 5001;
    public static final int CODE_ARROW_UP = 5002;
    public static final int CODE_ARROW_RIGHT = 5003;
    public static final int CODE_HOME = 5004;
    public static final int CODE_END = 5005;
    public static final int CODE_FORWARD_DELETE = 5006;
    public static final int CODE_PAGE_UP = 5007;
    public static final int CODE_PAGE_DOWN = 5008;

    // ---- Page switching ----
    public static final int PAGE_ABC = 0;
    public static final int PAGE_SYM = 1;
    public static final int PAGE_PROG = 2;
    public static final int PAGE_NAV = 3;
    public static final int CODE_PAGE_ABC = -20;
    public static final int CODE_PAGE_SYM = -21;
    public static final int CODE_PAGE_PROG = -22;
    public static final int CODE_PAGE_NAV = -23;

    private KeyboardDefinitions() {
    }

    // ------------------------------------------------------------------
    // Toolbar rows (top row, configurable)
    // ------------------------------------------------------------------

    /** Editing toolbar: Esc Tab Ctrl | Select All Cut Copy Paste. */
    public static void addEditingToolbarRow(Context context, KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey("Esc", CODE_ESCAPE).withRole(KeyInfo.Role.SPECIAL)
                .addTabKey()
                .addCtrlKey()
                .addKey(context.getDrawable(R.drawable.ic_select_all), CODE_SELECT_ALL).withRole(KeyInfo.Role.SPECIAL)
                .addKey(context.getDrawable(R.drawable.ic_cut), CODE_CUT).withRole(KeyInfo.Role.SPECIAL)
                .addKey(context.getDrawable(R.drawable.ic_copy), CODE_COPY).withRole(KeyInfo.Role.SPECIAL)
                .addKey(context.getDrawable(R.drawable.ic_paste), CODE_PASTE).withRole(KeyInfo.Role.SPECIAL);
    }

    /** Navigation toolbar: Esc Tab Alt | arrows + forward delete. */
    public static void addNavigationToolbarRow(Context context, KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey("Esc", CODE_ESCAPE).withRole(KeyInfo.Role.SPECIAL)
                .addTabKey()
                .addAltKey();
        addArrowKeys(context, keyboard);
    }

    private static void addArrowKeys(Context context, KeyboardLayoutBuilder keyboard) {
        keyboard.addKey(context.getDrawable(R.drawable.ic_arrow_left), CODE_ARROW_LEFT).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_arrow_down), CODE_ARROW_DOWN).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_arrow_up), CODE_ARROW_UP).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_arrow_right), CODE_ARROW_RIGHT).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_forward_delete), CODE_FORWARD_DELETE).withRole(KeyInfo.Role.SPECIAL).asRepeatable();
    }

    // ------------------------------------------------------------------
    // Bottom row: page switchers + custom chars + space + enter
    // ------------------------------------------------------------------

    public static void addBottomRow(Context context, KeyboardLayoutBuilder keyboard,
                                    int currentPage, String bottomChars) {
        keyboard.newRow()
                .addKey("Abc", CODE_PAGE_ABC).withRole(
                        currentPage == PAGE_ABC ? KeyInfo.Role.MODIFIER : KeyInfo.Role.SPECIAL)
                .addKey("#+=", CODE_PAGE_SYM).withRole(
                        currentPage == PAGE_SYM ? KeyInfo.Role.MODIFIER : KeyInfo.Role.SPECIAL)
                .addKey("</>", CODE_PAGE_PROG).withRole(
                        currentPage == PAGE_PROG ? KeyInfo.Role.MODIFIER : KeyInfo.Role.SPECIAL)
                .addKey("Nav", CODE_PAGE_NAV).withRole(
                        currentPage == PAGE_NAV ? KeyInfo.Role.MODIFIER : KeyInfo.Role.SPECIAL);

        char[] chars = bottomChars == null ? new char[0] : bottomChars.toCharArray();
        for (int i = 0; i < (chars.length + 1) / 2; i++) {
            keyboard.addKey(chars[i]).withSize(0.7f);
        }
        keyboard.addKey(context.getDrawable(R.drawable.ic_space_bar), CODE_SPACE).withSize(2.2f);
        for (int i = (chars.length + 1) / 2; i < chars.length; i++) {
            keyboard.addKey(chars[i]).withSize(0.7f);
        }
        keyboard.addEnterKey();
    }

    // ------------------------------------------------------------------
    // Alphabet layouts
    // ------------------------------------------------------------------

    public static void addNumberRow(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('`')
                .addKey('1')
                .addKey('2')
                .addKey('3')
                .addKey('4')
                .addKey('5')
                .addKey('6')
                .addKey('7')
                .addKey('8')
                .addKey('9')
                .addKey('0')
                .addKey('-')
                .addKey('=');
    }

    public static void addQwertyRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('q').onShiftUppercase()
                .addKey('w').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('y').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('o').onShiftUppercase()
                .addKey('p').onShiftUppercase()
                .newRow()
                .addKey('a').onShiftUppercase().withSize(1.5f)
                .addKey('s').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('l').onShiftUppercase().withSize(1.5f)
                .newRow()
                .addShiftKey()
                .addKey('z').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .addBackspaceKey();
    }

    public static void addQwertzRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('q').onShiftUppercase()
                .addKey('w').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('z').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('o').onShiftUppercase()
                .addKey('p').onShiftUppercase()
                .newRow()
                .addKey('a').onShiftUppercase().withSize(1.5f)
                .addKey('s').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('l').onShiftUppercase().withSize(1.5f)
                .newRow()
                .addShiftKey()
                .addKey('y').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .addBackspaceKey();
    }

    public static void addAzertyRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('a').onShiftUppercase()
                .addKey('z').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('y').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('o').onShiftUppercase()
                .addKey('p').onShiftUppercase()
                .newRow()
                .addKey('q').onShiftUppercase()
                .addKey('s').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('l').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .newRow()
                .addShiftKey()
                .addKey('w').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addBackspaceKey();
    }

    public static void addDvorakRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('!').onShiftShow("1")
                .addKey('p').onShiftUppercase()
                .addKey('y').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('l').onShiftUppercase()
                .newRow()
                .addKey('a').onShiftUppercase().withSize(1.5f)
                .addKey('o').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addKey('s').onShiftUppercase().withSize(1.5f)
                .newRow()
                .addShiftKey()
                .addKey('q').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .addKey('w').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('z').onShiftUppercase()
                .addBackspaceKey();
    }

    // ------------------------------------------------------------------
    // Symbol / programming / navigation pages
    // ------------------------------------------------------------------

    /** Symbol page rows (each char = one key). */
    public static void addSymbolRows(KeyboardLayoutBuilder keyboard, List<String> rows) {
        for (String row : rows) {
            keyboard.newRow();
            if (row == null) continue;
            for (char c : row.toCharArray()) {
                if (c == ' ') continue; // space is reserved for the space bar
                keyboard.addKey(c);
            }
        }
    }

    /** Navigation page rows. */
    public static void addNavRows(Context context, KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey("Home", CODE_HOME).withRole(KeyInfo.Role.SPECIAL)
                .addKey("End", CODE_END).withRole(KeyInfo.Role.SPECIAL)
                .addKey("PgUp", CODE_PAGE_UP).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey("PgDn", CODE_PAGE_DOWN).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey("Del", CODE_FORWARD_DELETE).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .newRow()
                .addAltKey().withSize(2.0f)
                .addCtrlKey().withSize(2.0f)
                .addShiftKey().withSize(2.0f)
                .addTabKey().withSize(2.0f)
                .newRow()
                .addKey(context.getDrawable(R.drawable.ic_arrow_left), CODE_ARROW_LEFT).withSize(2.5f).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_arrow_down), CODE_ARROW_DOWN).withSize(2.5f).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_arrow_up), CODE_ARROW_UP).withSize(2.5f).withRole(KeyInfo.Role.SPECIAL).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_arrow_right), CODE_ARROW_RIGHT).withSize(2.5f).withRole(KeyInfo.Role.SPECIAL).asRepeatable();
    }
}
