/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.codingkeys.keyboard.core.KeyboardPreferences;
import com.codingkeys.keyboard.layout.KeyboardDefinitions;
import com.codingkeys.keyboard.layout.KeyboardLayoutBuilder;
import com.codingkeys.keyboard.layout.KeyboardLayoutView;
import com.codingkeys.keyboard.layout.KeyboardUiFactory;
import com.codingkeys.keyboard.layout.Box;
import com.codingkeys.keyboard.layout.SymbolLayoutManager;
import com.codingkeys.keyboard.theme.ThemeDefinitions;
import com.codingkeys.keyboard.theme.ThemeInfo;
import com.codingkeys.keyboard.theme.ThemeManager;
import com.codingkeys.keyboard.theme.UiTheme;

import java.util.List;

/**
 * Dashboard: activation status, theme preview and navigation to settings.
 */
public class MainActivity extends AppCompatActivity {

    private FrameLayout previewContainer;
    private TextView previewThemeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewContainer = findViewById(R.id.preview_container);
        previewThemeName = findViewById(R.id.preview_theme_name);

        Button btnEnable = findViewById(R.id.btn_enable);
        Button btnSwitch = findViewById(R.id.btn_switch);
        Button btnSettings = findViewById(R.id.btn_settings);
        Button btnThemes = findViewById(R.id.btn_themes);
        Button btnLayouts = findViewById(R.id.btn_layouts);
        Button btnSymbols = findViewById(R.id.btn_symbols);
        TextView statusEnabled = findViewById(R.id.status_enabled);
        TextView statusDefault = findViewById(R.id.status_default);
        TextView aboutVersion = findViewById(R.id.about_version);
        EditText tryInput = findViewById(R.id.try_input);

        btnEnable.setOnClickListener(v ->
                startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)));

        btnSwitch.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, com.codingkeys.keyboard.settings.SettingsActivity.class)));
        btnThemes.setOnClickListener(v ->
                startActivity(new Intent(this, com.codingkeys.keyboard.settings.ThemeActivity.class)));
        btnLayouts.setOnClickListener(v ->
                startActivity(new Intent(this, com.codingkeys.keyboard.settings.LayoutActivity.class)));
        btnSymbols.setOnClickListener(v ->
                startActivity(new Intent(this, com.codingkeys.keyboard.settings.SymbolEditorActivity.class)));

        tryInput.setShowSoftInputOnFocus(false); // typing happens via the real keyboard

        String version = "1.0.0";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        aboutVersion.setText(getString(R.string.about_version, version));

        refreshStatus(statusEnabled, statusDefault);
        previewContainer.post(this::renderPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView statusEnabled = findViewById(R.id.status_enabled);
        TextView statusDefault = findViewById(R.id.status_default);
        refreshStatus(statusEnabled, statusDefault);
    }

    private void refreshStatus(TextView statusEnabled, TextView statusDefault) {
        Context ctx = this;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        boolean enabled = false;
        boolean chosen = false;
        String myPackage = getPackageName();
        try {
            for (InputMethodInfo info : imm.getEnabledInputMethodList()) {
                if (info.getPackageName().equals(myPackage)) {
                    enabled = true;
                    break;
                }
            }
            String currentId = android.provider.Settings.Secure.getString(
                    getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
            chosen = currentId != null && currentId.contains(myPackage);
        } catch (Exception ignored) {
        }

        statusEnabled.setText(enabled ? R.string.status_ime_enabled : R.string.status_ime_disabled);
        statusEnabled.setTextColor(getColor(enabled ? R.color.brand_status_ok : R.color.brand_status_warn));
        statusDefault.setText(chosen ? R.string.status_ime_default : R.string.status_ime_not_default);
        statusDefault.setBackgroundResource(chosen ? R.drawable.bg_chip_ok : R.drawable.bg_chip_warn);
        statusDefault.setTextColor(getColor(chosen ? R.color.brand_status_ok : R.color.brand_status_warn));
    }

    /** Render a small ABC-page keyboard preview with the current theme. */
    private void renderPreview() {
        KeyboardPreferences prefs = new KeyboardPreferences(this);
        ThemeInfo themeInfo = ThemeManager.resolve(prefs);
        previewThemeName.setText(themeInfo.name);

        UiTheme uiTheme = UiTheme.buildFromInfo(themeInfo);
        try {
            KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(this);
            builder.setBox(Box.create(0, 0, 1, 1));
            KeyboardDefinitions.addEditingToolbarRow(this, builder);
            KeyboardDefinitions.addNumberRow(builder);
            KeyboardDefinitions.addQwertyRows(builder);
            KeyboardDefinitions.addBottomRow(this, builder, KeyboardDefinitions.PAGE_ABC,
                    SymbolLayoutManager.sanitizeRow(prefs.getBottomChars()));

            int previewHeight = previewContainer.getHeight();
            if (previewHeight <= 0) previewHeight = 400;
            KeyboardLayoutView preview = KeyboardUiFactory.buildPreview(this, builder.build(),
                    uiTheme, new KeyboardUiFactory.NoOpListener(), previewHeight);
            previewContainer.removeAllViews();
            previewContainer.addView(preview,
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));
        } catch (Exception e) {
            previewThemeName.setText("Preview unavailable");
        }
    }
}
