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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codingkeys.keyboard.R;
import com.codingkeys.keyboard.core.KeyboardPreferences;

/**
 * Professional settings dashboard: general, typing and appearance sections.
 */
public class SettingsActivity extends AppCompatActivity {

    private KeyboardPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = new KeyboardPreferences(this);

        // ---- General ----
        Button btnTheme = findViewById(R.id.btn_theme);
        Button btnLayout = findViewById(R.id.btn_layout);
        btnTheme.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeActivity.class)));
        btnLayout.setOnClickListener(v ->
                startActivity(new Intent(this, LayoutActivity.class)));

        Spinner toolbarSpinner = findViewById(R.id.spinner_toolbar);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        getString(R.string.toolbar_editing),
                        getString(R.string.toolbar_navigation),
                        getString(R.string.toolbar_hidden)});
        toolbarSpinner.setAdapter(adapter);
        toolbarSpinner.setSelection(prefs.getToolbarMode());
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setToolbarMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ---- Keyboard height ----
        bindSeek(R.id.seek_height, R.id.label_height, prefs.getKeyHeightPercent(), 20, 60,
                value -> prefs.setKeyHeightPercent(value));
        bindSeek(R.id.seek_height_landscape, R.id.label_height_landscape,
                prefs.getLandscapeHeightPercent(), 30, 80,
                value -> prefs.setLandscapeHeightPercent(value));

        // ---- Typing ----
        Switch sound = findViewById(R.id.switch_sound);
        sound.setChecked(prefs.isSoundEnabled());
        sound.setOnCheckedChangeListener((b, checked) -> prefs.setSoundEnabled(checked));

        Switch vibrate = findViewById(R.id.switch_vibrate);
        vibrate.setChecked(prefs.isVibrateEnabled());
        vibrate.setOnCheckedChangeListener((b, checked) -> prefs.setVibrateEnabled(checked));

        Switch repeat = findViewById(R.id.switch_repeat);
        repeat.setChecked(prefs.isKeyRepeatEnabled());
        repeat.setOnCheckedChangeListener((b, checked) -> prefs.setKeyRepeatEnabled(checked));

        Switch sticky = findViewById(R.id.switch_sticky);
        sticky.setChecked(prefs.isStickyModifiers());
        sticky.setOnCheckedChangeListener((b, checked) -> prefs.setStickyModifiers(checked));

        bindSeek(R.id.seek_repeat_delay, R.id.label_repeat_delay, prefs.getRepeatDelayMs(), 100, 1000,
                value -> prefs.setRepeatDelayMs(value));
        bindSeek(R.id.seek_repeat_rate, R.id.label_repeat_rate, prefs.getRepeatRateMs(), 20, 200,
                value -> prefs.setRepeatRateMs(value));

        // ---- Appearance ----
        Switch preview = findViewById(R.id.switch_preview);
        preview.setChecked(prefs.isPreviewEnabled());
        preview.setOnCheckedChangeListener((b, checked) -> prefs.setPreviewEnabled(checked));

        Switch border = findViewById(R.id.switch_border);
        border.setChecked(prefs.isBorderEnabled());
        border.setOnCheckedChangeListener((b, checked) -> prefs.setBorderEnabled(checked));

        bindSeek(R.id.seek_font, R.id.label_font, prefs.getFontSizeSp(), 10, 28,
                value -> prefs.setFontSizeSp(value));
        bindSeek(R.id.seek_radius, R.id.label_radius, prefs.getKeyCornerRadiusDp(), 0, 24,
                value -> prefs.setKeyCornerRadiusDp(value));
        bindSeek(R.id.seek_gap, R.id.label_gap, prefs.getKeyGapDp(), 0, 8,
                value -> prefs.setKeyGapDp(value));

        // ---- Reset ----
        Button reset = findViewById(R.id.btn_reset);
        reset.setOnClickListener(v -> {
            prefs.resetAll();
            Toast.makeText(this, R.string.reset_done, Toast.LENGTH_SHORT).show();
            recreate();
        });
    }

    private interface IntConsumer {
        void accept(int value);
    }

    private void bindSeek(int seekId, int labelId, int value, int min, int max, IntConsumer setter) {
        SeekBar seek = findViewById(seekId);
        TextView label = findViewById(labelId);
        seek.setMax(max - min);
        seek.setProgress(value - min);
        label.setText(String.valueOf(value));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int v = progress + min;
                label.setText(String.valueOf(v));
                if (fromUser) setter.accept(v);
            }

            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
    }
}
