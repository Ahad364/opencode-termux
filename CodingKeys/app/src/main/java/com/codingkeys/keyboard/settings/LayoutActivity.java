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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.codingkeys.keyboard.R;
import com.codingkeys.keyboard.core.KeyboardPreferences;
import com.codingkeys.keyboard.layout.Box;
import com.codingkeys.keyboard.layout.KeyboardDefinitions;
import com.codingkeys.keyboard.layout.KeyboardLayoutBuilder;
import com.codingkeys.keyboard.layout.KeyboardLayoutView;
import com.codingkeys.keyboard.layout.KeyboardUiFactory;
import com.codingkeys.keyboard.layout.SymbolLayoutManager;
import com.codingkeys.keyboard.theme.ThemeManager;
import com.codingkeys.keyboard.theme.UiTheme;

/**
 * Alphabet layout selection (QWERTY / AZERTY / QWERTZ / DVORAK) with preview.
 */
public class LayoutActivity extends AppCompatActivity {

    private KeyboardPreferences prefs;
    private RadioGroup layoutGroup;
    private FrameLayout previewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        prefs = new KeyboardPreferences(this);
        layoutGroup = findViewById(R.id.layout_group);
        previewContainer = findViewById(R.id.preview_container);

        String[] names = {
                getString(R.string.layout_qwerty),
                getString(R.string.layout_azerty),
                getString(R.string.layout_qwertz),
                getString(R.string.layout_dvorak)};

        for (int i = 0; i < names.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(names[i]);
            rb.setTextColor(getColor(R.color.brand_text_primary));
            rb.setId(i);
            rb.setPadding(8, 12, 8, 12);
            layoutGroup.addView(rb);
        }
        layoutGroup.check(prefs.getLayoutIndex());
        layoutGroup.setOnCheckedChangeListener((group, checkedId) -> {
            prefs.setLayoutIndex(checkedId);
            previewContainer.post(this::renderPreview);
        });

        Button btnSymbols = findViewById(R.id.btn_symbols);
        btnSymbols.setOnClickListener(v ->
                startActivity(new Intent(this, SymbolEditorActivity.class)));

        previewContainer.post(this::renderPreview);
    }

    private void renderPreview() {
        try {
            KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(this);
            builder.setBox(Box.create(0, 0, 1, 1));
            switch (prefs.getLayoutIndex()) {
                case 1: KeyboardDefinitions.addAzertyRows(builder); break;
                case 2: KeyboardDefinitions.addQwertzRows(builder); break;
                case 3: KeyboardDefinitions.addDvorakRows(builder); break;
                case 0:
                default: KeyboardDefinitions.addQwertyRows(builder); break;
            }
            KeyboardDefinitions.addBottomRow(this, builder, KeyboardDefinitions.PAGE_ABC,
                    SymbolLayoutManager.sanitizeRow(prefs.getBottomChars()));
            int previewHeight = previewContainer.getHeight();
            if (previewHeight <= 0) previewHeight = 400;
            KeyboardLayoutView preview = KeyboardUiFactory.buildPreview(this, builder.build(),
                    UiTheme.buildFromInfo(ThemeManager.resolve(prefs)),
                    new KeyboardUiFactory.NoOpListener(), previewHeight);
            previewContainer.removeAllViews();
            previewContainer.addView(preview,
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));
        } catch (Exception ignored) {
        }
    }
}
