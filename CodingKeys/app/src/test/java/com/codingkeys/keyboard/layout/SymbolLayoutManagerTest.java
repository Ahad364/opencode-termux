/*
 * Coding Keys - unit tests.
 * Copyright (C) 2026 Coding Keys contributors
 */
package com.codingkeys.keyboard.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SymbolLayoutManagerTest {

    @Test
    public void parse_splitsOnNewlines() {
        List<String> rows = SymbolLayoutManager.parse("{}[]\n+-=");
        assertEquals(2, rows.size());
        assertEquals("{}[]", rows.get(0));
        assertEquals("+-=", rows.get(1));
    }

    @Test
    public void serialize_parsRoundTrip() {
        List<String> rows = Arrays.asList("{}[]()", "+-=*/%^~_|", "!@#$&");
        String stored = SymbolLayoutManager.serialize(rows);
        assertEquals(rows, SymbolLayoutManager.parse(stored));
    }

    @Test
    public void sanitizeRow_stripsSpaces() {
        assertEquals("{}[]", SymbolLayoutManager.sanitizeRow("{ } [ ]"));
    }

    @Test
    public void sanitizeRow_enforcesMaxLength() {
        String longRow = "0123456789ABCDEFGHIJ";
        assertEquals(SymbolLayoutManager.MAX_KEYS_PER_ROW,
                SymbolLayoutManager.sanitizeRow(longRow).length());
    }

    @Test
    public void clampRows_limitsRowcount() {
        List<String> rows = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        assertEquals(SymbolLayoutManager.MAX_ROWS, SymbolLayoutManager.clampRows(rows).size());
    }

    @Test
    public void dropEmptyRows_removesBlankRows() {
        List<String> rows = Arrays.asList("{}[]", "", "  ", "+-=");
        assertEquals(2, SymbolLayoutManager.dropEmptyRows(rows).size());
    }

    @Test
    public void moveRow_upAndDown() {
        List<String> rows = Arrays.asList("a", "b", "c");
        List<String> moved = SymbolLayoutManager.moveRow(rows, 1, true);
        assertEquals(Arrays.asList("b", "a", "c"), moved);

        moved = SymbolLayoutManager.moveRow(rows, 1, false);
        assertEquals(Arrays.asList("a", "c", "b"), moved);
    }

    @Test
    public void moveRow_atEdges_isNoop() {
        List<String> rows = Arrays.asList("a", "b");
        assertEquals(rows, SymbolLayoutManager.moveRow(rows, 0, true));
        assertEquals(rows, SymbolLayoutManager.moveRow(rows, 1, false));
    }

    @Test
    public void defaults_areNonEmpty() {
        assertTrue(SymbolLayoutManager.DEFAULT_SYMBOL_ROWS.length() > 0);
        assertTrue(SymbolLayoutManager.DEFAULT_PROG_ROWS.length() > 0);
        assertTrue(SymbolLayoutManager.DEFAULT_BOTTOM_CHARS.length() > 0);
    }
}
