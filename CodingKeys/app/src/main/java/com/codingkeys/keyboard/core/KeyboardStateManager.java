/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard.core;

import com.codingkeys.keyboard.layout.KeyboardDefinitions;

/**
 * Tracks the volatile keyboard state: current page and shift lock.
 * State resets to a clean baseline whenever a new editor gains focus.
 */
public class KeyboardStateManager {

    /** Listener for state-driven view updates (implemented by the IME). */
    public interface OnStateChangedListener {
        void onPageChanged(int newPage);

        void onShiftLockChanged(boolean locked);
    }

    private int currentPage = KeyboardDefinitions.PAGE_ABC;
    private boolean shiftLock = false;
    private OnStateChangedListener listener;

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        this.listener = listener;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        if (page < KeyboardDefinitions.PAGE_ABC || page > KeyboardDefinitions.PAGE_NAV) {
            page = KeyboardDefinitions.PAGE_ABC;
        }
        if (page != currentPage) {
            currentPage = page;
            notifyPageChanged();
        }
    }

    /** Advance to the next page (used by quick-cycling if ever needed). */
    public void cyclePage() {
        setCurrentPage((currentPage + 1) % 4);
    }

    public boolean isShiftLock() {
        return shiftLock;
    }

    public void setShiftLock(boolean locked) {
        if (locked != shiftLock) {
            shiftLock = locked;
            if (listener != null) {
                listener.onShiftLockChanged(locked);
            }
        }
    }

    /** Reset to the default state for a new input session. */
    public void reset() {
        currentPage = KeyboardDefinitions.PAGE_ABC;
        shiftLock = false;
        notifyPageChanged();
    }

    private void notifyPageChanged() {
        if (listener != null) {
            listener.onPageChanged(currentPage);
        }
    }
}
