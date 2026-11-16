/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses, validates and stores the user-customizable symbol rows.
 *
 * Model (same as the reference project): each character in a row string
 * becomes one key. Rows are separated by '\n' when persisted. Spaces inside
 * a row are skipped (the space bar is part of the fixed bottom row), so the
 * editor can use them for readability.
 */
public class SymbolLayoutManager {

    public static final String ROW_SEPARATOR = "\n";
    public static final String DEFAULT_SYMBOL_ROWS = "~!@#$%^&*()_+\n{}[]()<>\\|/;:\n'\"`,.?!#&=";
    public static final String DEFAULT_PROG_ROWS = "(){}[]<>;:=\n+-=*/%^~_|\n!@#$&\\'\"`?";
    public static final String DEFAULT_BOTTOM_CHARS = ",.'/";

    /** Maximum number of keys per row - keeps keys tappable. */
    public static final int MAX_KEYS_PER_ROW = 15;
    /** Maximum number of custom rows per page. */
    public static final int MAX_ROWS = 6;

    private SymbolLayoutManager() {
    }

    /** Split a stored string into rows. */
    public static List<String> parse(String stored) {
        List<String> rows = new ArrayList<>();
        if (stored == null) {
            return rows;
        }
        for (String line : stored.split(ROW_SEPARATOR)) {
            rows.add(line == null ? "" : line);
        }
        return rows;
    }

    /** Join rows into a stored string. */
    public static String serialize(List<String> rows) {
        if (rows == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append(ROW_SEPARATOR);
            sb.append(rows.get(i) == null ? "" : rows.get(i));
        }
        return sb.toString();
    }

    /** Remove spaces and enforce the per-row key limit. */
    public static String sanitizeRow(String row) {
        if (row == null) return "";
        String cleaned = row.replace(" ", "").replace(ROW_SEPARATOR, "");
        if (cleaned.length() > MAX_KEYS_PER_ROW) {
            cleaned = cleaned.substring(0, MAX_KEYS_PER_ROW);
        }
        return cleaned;
    }

    /** Clamp the row list length to MAX_ROWS. */
    public static List<String> clampRows(List<String> rows) {
        List<String> result = new ArrayList<>();
        if (rows == null) return result;
        for (String row : rows) {
            if (result.size() >= MAX_ROWS) break;
            result.add(sanitizeRow(row));
        }
        return result;
    }

    /** True when the row contains at least one printable key character. */
    public static boolean isRowEmpty(String row) {
        return sanitizeRow(row).isEmpty();
    }

    /** Drop rows that contain no keys. */
    public static List<String> dropEmptyRows(List<String> rows) {
        List<String> result = new ArrayList<>();
        if (rows == null) return result;
        for (String row : rows) {
            if (!isRowEmpty(row)) {
                result.add(sanitizeRow(row));
            }
        }
        return result;
    }

    /** Move a row up (toIndex-1) or down (toIndex+1); returns the new list. */
    public static List<String> moveRow(List<String> rows, int fromIndex, boolean up) {
        if (rows == null || fromIndex < 0 || fromIndex >= rows.size()) {
            return rows;
        }
        int toIndex = up ? fromIndex - 1 : fromIndex + 1;
        if (toIndex < 0 || toIndex >= rows.size()) {
            return rows;
        }
        List<String> copy = new ArrayList<>(rows);
        String moved = copy.remove(fromIndex);
        copy.add(toIndex, moved);
        return copy;
    }
}
