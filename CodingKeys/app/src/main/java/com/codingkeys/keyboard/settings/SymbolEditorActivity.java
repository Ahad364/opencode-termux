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

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.codingkeys.keyboard.theme.ThemeManager;
import com.codingkeys.keyboard.theme.UiTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom symbol editor: add, remove, edit and reorder symbol rows for the
 * symbol and programming pages, with a live keyboard preview.
 *
 * Model: each editable line is one row; every character becomes one key.
 * Editors stay bound to list entries, so reordering is a list operation.
 */
public class SymbolEditorActivity extends AppCompatActivity {

    private KeyboardPreferences prefs;
    private LinearLayout rowsContainer;
    private FrameLayout previewContainer;

    private final List<EditText> symbolEditors = new ArrayList<>();
    private final List<EditText> progEditors = new ArrayList<>();
    private EditText bottomEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbol_editor);
        prefs = new KeyboardPreferences(this);
        rowsContainer = findViewById(R.id.rows_container);
        previewContainer = findViewById(R.id.preview_container);

        Button addRow = findViewById(R.id.btn_add_row);
        addRow.setOnClickListener(v -> {
            persistCurrentTexts();
            List<String> rows = SymbolLayoutManager.clampRows(
                    SymbolLayoutManager.parse(prefs.getSymbolRows()));
            if (rows.size() >= SymbolLayoutManager.MAX_ROWS) {
                Toast.makeText(this, "Maximum rows reached", Toast.LENGTH_SHORT).show();
                return;
            }
            rows.add("");
            prefs.setSymbolRows(SymbolLayoutManager.serialize(rows));
            rebuild();
            renderPreview();
        });

        Button restore = findViewById(R.id.btn_restore);
        restore.setOnClickListener(v -> {
            prefs.setSymbolRows(SymbolLayoutManager.DEFAULT_SYMBOL_ROWS);
            prefs.setProgRows(SymbolLayoutManager.DEFAULT_PROG_ROWS);
            prefs.setBottomChars(SymbolLayoutManager.DEFAULT_BOTTOM_CHARS);
            rebuild();
            renderPreview();
        });

        Button save = findViewById(R.id.btn_save);
        save.setOnClickListener(v -> save());

        rebuild();
        previewContainer.post(this::renderPreview);
    }

    // ------------------------------------------------------------------
    // Building the editor list
    // ------------------------------------------------------------------

    private void rebuild() {
        List<String> sym = SymbolLayoutManager.clampRows(
                SymbolLayoutManager.parse(prefs.getSymbolRows()));
        List<String> prog = SymbolLayoutManager.clampRows(
                SymbolLayoutManager.parse(prefs.getProgRows()));
        String bottom = SymbolLayoutManager.sanitizeRow(prefs.getBottomChars());
        buildEditors(sym, prog, bottom);
    }

    /** Full rebuild from preference data. */
    private void buildEditors(List<String> sym, List<String> prog, String bottom) {
        rowsContainer.removeAllViews();
        symbolEditors.clear();
        progEditors.clear();

        addHeader(getString(R.string.sym_editor_hint_symbols));
        for (int i = 0; i < sym.size(); i++) {
            final int index = i;
            EditText edit = addRowEditor(sym.get(i), () -> {
                // move up
                List<String> rows = readEditors(symbolEditors);
                rows = SymbolLayoutManager.moveRow(rows, index, true);
                prefs.setSymbolRows(SymbolLayoutManager.serialize(rows));
                rebuild();
            }, () -> {
                List<String> rows = readEditors(symbolEditors);
                rows = SymbolLayoutManager.moveRow(rows, index, false);
                prefs.setSymbolRows(SymbolLayoutManager.serialize(rows));
                rebuild();
            }, () -> {
                List<String> rows = readEditors(symbolEditors);
                rows.remove(index);
                prefs.setSymbolRows(SymbolLayoutManager.serialize(rows));
                rebuild();
            });
            symbolEditors.add(edit);
        }

        addHeader(getString(R.string.sym_editor_hint_prog));
        for (int i = 0; i < prog.size(); i++) {
            final int index = i;
            EditText edit = addRowEditor(prog.get(i), () -> {
                List<String> rows = readEditors(progEditors);
                rows = SymbolLayoutManager.moveRow(rows, index, true);
                prefs.setProgRows(SymbolLayoutManager.serialize(rows));
                rebuild();
            }, () -> {
                List<String> rows = readEditors(progEditors);
                rows = SymbolLayoutManager.moveRow(rows, index, false);
                prefs.setProgRows(SymbolLayoutManager.serialize(rows));
                rebuild();
            }, () -> {
                List<String> rows = readEditors(progEditors);
                rows.remove(index);
                prefs.setProgRows(SymbolLayoutManager.serialize(rows));
                rebuild();
            });
            progEditors.add(edit);
        }

        addHeader(getString(R.string.sym_editor_hint_bottom));
        bottomEditor = addRowEditor(bottom, null, null, null);
    }

    private void addHeader(String text) {
        TextView header = new TextView(this);
        header.setText(text);
        header.setTextColor(getColor(R.color.brand_text_secondary));
        header.setTextSize(12);
        header.setPadding(0, 14, 0, 6);
        rowsContainer.addView(header);
    }

    private interface Action {
        void run();
    }

    private EditText addRowEditor(String content, Action moveUp, Action moveDown, Action remove) {
        LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.item_symbol_row, rowsContainer, false);
        EditText edit = item.findViewById(R.id.edit_row);
        edit.setText(content);
        edit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                renderPreview();
            }
        });

        ImageButton up = item.findViewById(R.id.btn_up);
        ImageButton down = item.findViewById(R.id.btn_down);
        ImageButton removeBtn = item.findViewById(R.id.btn_remove);

        if (moveUp != null) up.setOnClickListener(v -> {
            persistCurrentTexts();
            moveUp.run();
        });
        else up.setOnClickListener(v -> { });

        if (moveDown != null) down.setOnClickListener(v -> {
            persistCurrentTexts();
            moveDown.run();
        });
        else down.setOnClickListener(v -> { });

        if (remove != null) removeBtn.setOnClickListener(v -> {
            persistCurrentTexts();
            remove.run();
        });
        else removeBtn.setOnClickListener(v -> { });

        rowsContainer.addView(item);
        return edit;
    }

    /** Copy current editor texts into prefs so reorder/remove keeps edits. */
    private void persistCurrentTexts() {
        prefs.setSymbolRows(SymbolLayoutManager.serialize(readEditors(symbolEditors)));
        prefs.setProgRows(SymbolLayoutManager.serialize(readEditors(progEditors)));
        if (bottomEditor != null) {
            prefs.setBottomChars(SymbolLayoutManager.sanitizeRow(bottomEditor.getText().toString()));
        }
    }

    private List<String> readEditors(List<EditText> editors) {
        List<String> rows = new ArrayList<>();
        for (EditText editor : editors) {
            rows.add(editor.getText().toString());
        }
        return rows;
    }

    // ------------------------------------------------------------------
    // Save / preview
    // ------------------------------------------------------------------

    private void save() {
        persistCurrentTexts();
        // Normalize through the parser (clamps, drops empties).
        prefs.setSymbolRows(SymbolLayoutManager.serialize(
                SymbolLayoutManager.dropEmptyRows(
                        SymbolLayoutManager.parse(prefs.getSymbolRows()))));
        prefs.setProgRows(SymbolLayoutManager.serialize(
                SymbolLayoutManager.dropEmptyRows(
                        SymbolLayoutManager.parse(prefs.getProgRows()))));
        prefs.setBottomChars(SymbolLayoutManager.sanitizeRow(prefs.getBottomChars()));
        Toast.makeText(this, R.string.sym_editor_saved, Toast.LENGTH_SHORT).show();
    }

    private void renderPreview() {
        try {
            List<String> sym = SymbolLayoutManager.dropEmptyRows(readEditors(symbolEditors));
            String bottom = bottomEditor == null ? ""
                    : SymbolLayoutManager.sanitizeRow(bottomEditor.getText().toString());

            KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(this);
            builder.setBox(Box.create(0, 0, 1, 1));
            KeyboardDefinitions.addSymbolRows(builder, sym);
            KeyboardDefinitions.addBottomRow(this, builder, KeyboardDefinitions.PAGE_SYM, bottom);

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
