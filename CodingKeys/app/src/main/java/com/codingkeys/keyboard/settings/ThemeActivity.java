/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codingkeys.keyboard.R;
import com.codingkeys.keyboard.core.KeyboardPreferences;
import com.codingkeys.keyboard.layout.Box;
import com.codingkeys.keyboard.layout.KeyboardDefinitions;
import com.codingkeys.keyboard.layout.KeyboardLayoutBuilder;
import com.codingkeys.keyboard.layout.KeyboardLayoutView;
import com.codingkeys.keyboard.layout.KeyboardUiFactory;
import com.codingkeys.keyboard.layout.SymbolLayoutManager;
import com.codingkeys.keyboard.theme.ThemeDefinitions;
import com.codingkeys.keyboard.theme.ThemeInfo;
import com.codingkeys.keyboard.theme.ThemeManager;
import com.codingkeys.keyboard.theme.UiTheme;

/**
 * Theme selection with live preview and a custom theme editor.
 */
public class ThemeActivity extends AppCompatActivity {

    private KeyboardPreferences prefs;
    private RadioGroup themeGroup;
    private FrameLayout previewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        prefs = new KeyboardPreferences(this);
        themeGroup = findViewById(R.id.theme_group);
        previewContainer = findViewById(R.id.preview_container);

        String[] themeNames = {
                "Premium Blue", "Midnight Blue", "Deep Purple", "Cyber Dark",
                "AMOLED Black", "Pure Black", "Material Dark", "Material Light",
                "Ocean Blue", "Neon Purple", "Premium Gradient", "Custom"};

        for (int i = 0; i < themeNames.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(themeNames[i]);
            rb.setTextColor(getColor(R.color.brand_text_primary));
            rb.setId(i);
            rb.setPadding(8, 12, 8, 12);
            themeGroup.addView(rb);
            if (prefs.getThemeIndex() == i) {
                themeGroup.check(i);
            }
        }
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            prefs.setThemeIndex(checkedId);
            Toast.makeText(this, R.string.theme_applied, Toast.LENGTH_SHORT).show();
            previewContainer.post(this::renderPreview);
        });

        // Pre-fill the custom theme editor with current values.
        ThemeInfo custom = ThemeDefinitions.Custom();
        ThemeManager.applyCustomColors(custom, prefs);
        setText(R.id.custom_background, custom.backgroundColor);
        Integer grad = custom.backgroundColorGradientEnd;
        ((EditText) findViewById(R.id.custom_gradient)).setText(
                grad == null ? "" : String.format("#%08X", grad));
        setText(R.id.custom_key, custom.keyBackgroundColor);
        setText(R.id.custom_special, custom.specialKeyBackgroundColor);
        setText(R.id.custom_modifier, custom.modifierKeyBackgroundColor);
        setText(R.id.custom_action, custom.actionKeyBackgroundColor);
        setText(R.id.custom_text, custom.keyTextColor);

        Button apply = findViewById(R.id.btn_apply_custom);
        apply.setOnClickListener(v -> applyCustomTheme());

        previewContainer.post(this::renderPreview);
    }

    private void setText(int viewId, int color) {
        ((EditText) findViewById(viewId)).setText(String.format("#%06X", 0xFFFFFF & color));
    }

    /** Parse "#RRGGBB" or "#AARRGGBB"; returns fallback when invalid. */
    private static int parseColor(EditText input, int fallback) {
        try {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) return fallback;
            return Color.parseColor(text.startsWith("#") ? text : "#" + text);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void applyCustomTheme() {
        ThemeInfo base = ThemeDefinitions.Custom();
        ThemeManager.applyCustomColors(base, prefs);

        prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_BACKGROUND,
                parseColor(findEditText(R.id.custom_background), base.backgroundColor));
        EditText grad = findEditText(R.id.custom_gradient);
        String gradText = grad.getText().toString().trim();
        if (gradText.isEmpty()) {
            prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_GRADIENT_END, 0);
        } else {
            prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_GRADIENT_END,
                    parseColor(grad, base.backgroundColor));
        }
        prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_KEY,
                parseColor(findEditText(R.id.custom_key), base.keyBackgroundColor));
        prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_SPECIAL,
                parseColor(findEditText(R.id.custom_special), base.specialKeyBackgroundColor));
        prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_MODIFIER,
                parseColor(findEditText(R.id.custom_modifier), base.modifierKeyBackgroundColor));
        prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_ACTION,
                parseColor(findEditText(R.id.custom_action), base.actionKeyBackgroundColor));
        prefs.setCustomColor(KeyboardPreferences.CUSTOM_COLOR_TEXT,
                parseColor(findEditText(R.id.custom_text), base.keyTextColor));

        prefs.setThemeIndex(ThemeManager.CUSTOM_THEME_INDEX);
        themeGroup.check(ThemeManager.CUSTOM_THEME_INDEX);
        Toast.makeText(this, R.string.theme_applied, Toast.LENGTH_SHORT).show();
        previewContainer.post(this::renderPreview);
    }

    private EditText findEditText(int viewId) {
        return findViewById(viewId);
    }

    private void renderPreview() {
        ThemeInfo themeInfo = ThemeManager.resolve(prefs);
        try {
            KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(this);
            builder.setBox(Box.create(0, 0, 1, 1));
            KeyboardDefinitions.addEditingToolbarRow(this, builder);
            KeyboardDefinitions.addQwertyRows(builder);
            KeyboardDefinitions.addBottomRow(this, builder, KeyboardDefinitions.PAGE_ABC,
                    SymbolLayoutManager.sanitizeRow(prefs.getBottomChars()));
            int previewHeight = previewContainer.getHeight();
            if (previewHeight <= 0) previewHeight = 400;
            KeyboardLayoutView preview = KeyboardUiFactory.buildPreview(this, builder.build(),
                    UiTheme.buildFromInfo(themeInfo), new KeyboardUiFactory.NoOpListener(),
                    previewHeight);
            previewContainer.removeAllViews();
            previewContainer.addView(preview,
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));
        } catch (Exception ignored) {
        }
    }
}
