/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard's CodeBoardIME (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.codingkeys.keyboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codingkeys.keyboard.core.InputConnectionManager;
import com.codingkeys.keyboard.core.KeyboardEventDispatcher;
import com.codingkeys.keyboard.core.KeyboardPreferences;
import com.codingkeys.keyboard.core.KeyboardStateManager;
import com.codingkeys.keyboard.core.ModifierKeyManager;
import com.codingkeys.keyboard.editing.ClipboardManager;
import com.codingkeys.keyboard.editing.CursorNavigationManager;
import com.codingkeys.keyboard.editing.TextSelectionManager;
import com.codingkeys.keyboard.editing.UndoRedoManager;
import com.codingkeys.keyboard.layout.Box;
import com.codingkeys.keyboard.layout.KeyboardButtonView;
import com.codingkeys.keyboard.layout.KeyboardDefinitions;
import com.codingkeys.keyboard.layout.KeyboardLayoutBuilder;
import com.codingkeys.keyboard.layout.KeyboardLayoutException;
import com.codingkeys.keyboard.layout.KeyboardLayoutView;
import com.codingkeys.keyboard.layout.KeyboardUiFactory;
import com.codingkeys.keyboard.layout.Key;
import com.codingkeys.keyboard.layout.SymbolLayoutManager;
import com.codingkeys.keyboard.theme.ThemeManager;
import com.codingkeys.keyboard.theme.ThemeInfo;
import com.codingkeys.keyboard.theme.UiTheme;

import java.util.Collection;
import java.util.List;

/**
 * The Coding Keys input method service. Dynamically generates every page of
 * the keyboard; no static keyboard XML is used.
 */
public class CodingKeysIME extends InputMethodService
        implements KeyboardButtonView.KeyEventListener, KeyboardStateManager.OnStateChangedListener {

    private static final String TAG = "CodingKeysIME";

    private KeyboardUiFactory uiFactory;
    private KeyboardLayoutView currentView;

    /**
     * What the currently shown input view was built for. Used to reuse the
     * view across hide/show cycles instead of recreating it on every open -
     * the navigation bar fix depends on this (see ensureInputView).
     */
    private int builtForPage = -1;
    private String builtForFingerprint = null;

    private KeyboardPreferences preferences;
    private KeyboardStateManager stateManager;
    private ModifierKeyManager modifiers;
    private InputConnectionManager icm;
    private KeyboardEventDispatcher dispatcher;
    private UndoRedoManager undoRedo;

    private boolean soundEnabled = true;
    private boolean vibrateEnabled = true;
    private AudioManager audioManager;
    private Vibrator vibrator;

    /**
     * Last real system-bar insets reported by the platform listener. A newly
     * built keyboard view is seeded with these synchronously, so its very
     * first measurement already includes the navigation bar strip and never
     * depends on whether/when the insets dispatch arrives.
     */
    private int cachedInsetLeft = 0;
    private int cachedInsetRight = 0;
    private int cachedInsetBottom = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new KeyboardPreferences(this);
        stateManager = new KeyboardStateManager();
        stateManager.setOnStateChangedListener(this);
        modifiers = new ModifierKeyManager(this);
        modifiers.setSticky(preferences.isStickyModifiers());
        icm = new InputConnectionManager(this::getCurrentInputConnection);
        undoRedo = new UndoRedoManager(icm);
        ClipboardManager clipboard = new ClipboardManager(this);
        TextSelectionManager selection = new TextSelectionManager(icm, clipboard);
        CursorNavigationManager navigation = new CursorNavigationManager(icm, modifiers);
        dispatcher = new KeyboardEventDispatcher(this, icm, modifiers, selection, navigation, undoRedo);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    /** No fullscreen extract mode - code editors need to stay visible. */
    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    // ------------------------------------------------------------------
    // Input view lifecycle
    // ------------------------------------------------------------------

    @Override
    public View onCreateInputView() {
        if (uiFactory == null) {
            uiFactory = new KeyboardUiFactory(this);
        }
        applyPreferencesToFactory();
        return buildPage(stateManager.getCurrentPage());
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        // Release any held meta keys so the next editor starts clean.
        modifiers.releaseAll();
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        if (!restarting) {
            // New field: reset page and undo history, keep preferences fresh.
            stateManager.reset();
            undoRedo.reset();
        }
        modifiers.setSticky(preferences.isStickyModifiers());
        applyPreferencesToFactory();

        // Navigation bar positioning fix: do NOT rebuild the input view on
        // every open. Rebuild only when something actually changed; otherwise
        // reuse the already-correct view and re-sync the current insets.
        ensureInputView();
        updateModifierViews();
    }

    /**
     * ROOT CAUSE of the "correct on first open, wrong afterwards" bug:
     * the old code called {@code setInputView(buildPage(...))} inside
     * {@link #onStartInputView}, so every open tore down the correctly
     * padded view and installed a brand-new one whose navigation bar inset
     * was still 0. The platform dispatches WindowInsets on window creation
     * and on insets changes - a view swapped into an already-created window
     * is NOT guaranteed an insets pass. First open worked (window creation
     * traversal dispatches insets); every later open showed an unpadded
     * view whose bottom row slid behind the navigation bar.
     *
     * PERMANENT FIX, two parts:
     * 1. Reuse the input view across open/close cycles. Rebuild only when
     *    the page or a layout-affecting preference changed. The same view
     *    with the same insets renders the same position every time.
     * 2. After every show, force {@link ViewCompat#requestApplyInsets} so
     *    the CURRENT system insets are re-dispatched into whatever view is
     *    shown - fresh or reused. Positioning is recalculated on each open
     *    from live values, never from stale cached ones. The inset setter
     *    is idempotent, so repeated dispatches cause no duplicate padding
     *    and no layout loops.
     */
    private void ensureInputView() {
        int page = stateManager.getCurrentPage();
        boolean stale = currentView == null
                || builtForPage != page
                || !layoutFingerprint().equals(builtForFingerprint);
        if (stale) {
            // Page switch, theme/height/symbol change, first build, or view
            // recreation after a configuration change: build a fresh view.
            rebuildKeyboard();
            return;
        }
        // Reused view: re-dispatch the live window insets so the navigation
        // bar spacing is recalculated for THIS open. Posted so it runs after
        // the view is attached to the (possibly re-shown) window.
        final KeyboardLayoutView view = currentView;
        view.post(() -> ViewCompat.requestApplyInsets(view));
    }

    /**
     * Signature of every preference that influences the built layout. When
     * it changes, the input view must be rebuilt; otherwise it is reused.
     */
    private String layoutFingerprint() {
        return preferences.getThemeIndex()
                + "|" + preferences.getLayoutIndex()
                + "|" + preferences.getToolbarMode()
                + "|" + preferences.getKeyHeightPercent()
                + "|" + preferences.getLandscapeHeightPercent()
                + "|" + preferences.getFontSizeSp()
                + "|" + preferences.getKeyCornerRadiusDp()
                + "|" + preferences.getKeyGapDp()
                + "|" + preferences.isPreviewEnabled()
                + "|" + preferences.isBorderEnabled()
                + "|" + preferences.isKeyRepeatEnabled()
                + "|" + preferences.getRepeatDelayMs()
                + "|" + preferences.getRepeatRateMs()
                + "|" + preferences.getSymbolRows()
                + "|" + preferences.getProgRows()
                + "|" + preferences.getBottomChars();
    }

    /**
     * Called on every window show. Re-dispatch the current insets so the
     * keyboard is positioned against the CURRENT navigation bar state (e.g.
     * the user switched between 3-button and gesture navigation while the
     * keyboard was closed). The inset setter is change-guarded, so this is
     * a no-op when the values are unchanged.
     */
    @Override
    public void onWindowShown() {
        super.onWindowShown();
        if (currentView != null) {
            final KeyboardLayoutView view = currentView;
            view.post(() -> ViewCompat.requestApplyInsets(view));
        }
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        modifiers.releaseAll();
    }

    private void applyPreferencesToFactory() {
        if (uiFactory == null) return;
        ThemeInfo theme = ThemeManager.resolve(preferences);
        uiFactory.uiTheme = UiTheme.buildFromInfo(theme);
        uiFactory.keyRepeatEnabled = preferences.isKeyRepeatEnabled();
        uiFactory.repeatDelayMs = preferences.getRepeatDelayMs();
        uiFactory.repeatIntervalMs = preferences.getRepeatRateMs();
        uiFactory.keyGapPx = preferences.getKeyGapPx();
        soundEnabled = preferences.isSoundEnabled();
        vibrateEnabled = preferences.isVibrateEnabled();
    }

    // ------------------------------------------------------------------
    // Page construction
    // ------------------------------------------------------------------

    /** Dynamically build one page of the keyboard. */
    private KeyboardLayoutView buildPage(int page) {
        String fingerprint = layoutFingerprint();
        try {
            // The builder works in normalized space (Box 0..1), so gaps must
            // be converted from pixels to a fraction of the keyboard size.
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            boolean portrait = metrics.heightPixels > metrics.widthPixels;
            float keyboardHeightPx = metrics.heightPixels
                    * (portrait ? preferences.getKeyHeightPercent()
                    : preferences.getLandscapeHeightPercent()) / 100f;
            float keyGapNorm = preferences.getKeyGapPx() / metrics.widthPixels;
            float rowGapNorm = preferences.getKeyGapPx() / Math.max(1f, keyboardHeightPx);

            KeyboardLayoutBuilder builder = new KeyboardLayoutBuilder(this);
            builder.setBox(Box.create(0, 0, 1, 1));
            builder.setKeyGap(keyGapNorm);
            builder.setRowGap(rowGapNorm);
            builder.setPadding(0);

            int toolbarMode = preferences.getToolbarMode();
            if (toolbarMode == KeyboardPreferences.TOOLBAR_EDITING) {
                KeyboardDefinitions.addEditingToolbarRow(this, builder);
            } else if (toolbarMode == KeyboardPreferences.TOOLBAR_NAVIGATION) {
                KeyboardDefinitions.addNavigationToolbarRow(this, builder);
            }

            List<String> symRows = SymbolLayoutManager.dropEmptyRows(
                    SymbolLayoutManager.parse(preferences.getSymbolRows()));
            List<String> progRows = SymbolLayoutManager.dropEmptyRows(
                    SymbolLayoutManager.parse(preferences.getProgRows()));
            String bottomChars = SymbolLayoutManager.sanitizeRow(preferences.getBottomChars());

            switch (page) {
                case KeyboardDefinitions.PAGE_SYM:
                    KeyboardDefinitions.addSymbolRows(builder, symRows);
                    break;
                case KeyboardDefinitions.PAGE_PROG:
                    KeyboardDefinitions.addSymbolRows(builder, progRows);
                    break;
                case KeyboardDefinitions.PAGE_NAV:
                    KeyboardDefinitions.addNavRows(this, builder);
                    break;
                case KeyboardDefinitions.PAGE_ABC:
                default:
                    KeyboardDefinitions.addNumberRow(builder);
                    addAlphabetRows(builder);
                    break;
            }
            KeyboardDefinitions.addBottomRow(this, builder, page, bottomChars);

            Collection<Key> keys = builder.build();
            currentView = uiFactory.createKeyboardView(this, keys);
            attachNavigationBarInsetsHandling(currentView);
            // Seed with the last real system-bar insets so the very first
            // measurement already includes the navigation bar strip and never
            // depends on whether/when the insets dispatch arrives.
            currentView.setNavigationBarInsets(
                    cachedInsetLeft, cachedInsetRight, cachedInsetBottom);
            builtForPage = page;
            builtForFingerprint = fingerprint;
            return currentView;
        } catch (KeyboardLayoutException e) {
            Log.e(TAG, "Layout generation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected keyboard build error", e);
        }
        // Never return a broken keyboard: fall back to an empty panel so the
        // system still shows something and the user can switch IMEs.
        KeyboardLayoutView fallback = new KeyboardLayoutView(this,
                uiFactory != null ? uiFactory.uiTheme
                        : UiTheme.buildFromInfo(ThemeManager.resolve(preferences)));
        attachNavigationBarInsetsHandling(fallback);
        fallback.setNavigationBarInsets(
                cachedInsetLeft, cachedInsetRight, cachedInsetBottom);
        builtForPage = page;
        builtForFingerprint = fingerprint;
        return fallback;
    }

    /**
     * Navigation bar integration (3-button and gesture navigation).
     *
     * The system normally lays out the IME window directly above the
     * navigation bar; the reported navigation bar inset is then 0 and the
     * keyboard layout is unchanged. When the window is drawn edge-to-edge
     * (Android 15+ or OEM translucent navigation), the real system bar
     * insets reach this view: the keys are lifted above the bar and the
     * keyboard background continues behind it, so Back / Home / Recents
     * stay visible and tappable and no key is ever covered.
     *
     * FLAG_LAYOUT_NO_LIMITS is deliberately NOT used - the window keeps
     * fitting the system bars and real WindowInsets are respected instead.
     * No fake navigation bar is drawn: only the keyboard background is
     * extended behind the real system bar.
     */
    private void attachNavigationBarInsetsHandling(KeyboardLayoutView view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            // Fully qualified: the inherited InputMethodService.Insets would
            // otherwise shadow the androidx type inside this class.
            androidx.core.graphics.Insets bars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Remember the real platform values so any view built later can be
            // seeded synchronously and never depends on dispatch timing.
            cachedInsetLeft = bars.left;
            cachedInsetRight = bars.right;
            cachedInsetBottom = bars.bottom;
            view.setNavigationBarInsets(bars.left, bars.right, bars.bottom);
            // Consumed: children (key buttons) must not apply the insets again.
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void addAlphabetRows(KeyboardLayoutBuilder builder) {
        switch (preferences.getLayoutIndex()) {
            case 1: KeyboardDefinitions.addAzertyRows(builder); break;
            case 2: KeyboardDefinitions.addQwertzRows(builder); break;
            case 3: KeyboardDefinitions.addDvorakRows(builder); break;
            case 0:
            default: KeyboardDefinitions.addQwertyRows(builder); break;
        }
    }

    private void rebuildKeyboard() {
        // Idempotence guard: KeyboardStateManager.reset() fires onPageChanged
        // unconditionally on every input session, even when the page did not
        // change. Rebuilding there would tear down the correctly padded view
        // and swap in a fresh one that is not guaranteed an insets pass (the
        // platform dispatches insets on window creation and on insets changes,
        // not on view swaps into a live window) - the direct cause of the
        // alternating correct/incorrect positioning across opens. When the
        // current view already matches this page and layout, keep it.
        int page = stateManager.getCurrentPage();
        String fingerprint = layoutFingerprint();
        if (currentView != null && builtForPage == page
                && fingerprint.equals(builtForFingerprint)) {
            updateModifierViews();
            return;
        }
        applyPreferencesToFactory();
        KeyboardLayoutView view = buildPage(page);
        setInputView(view);
        updateModifierViews();
    }

    @Override
    public void onPageChanged(int newPage) {
        rebuildKeyboard();
    }

    @Override
    public void onShiftLockChanged(boolean locked) {
        updateModifierViews();
    }

    private void updateModifierViews() {
        if (currentView == null) return;
        currentView.applyShiftModifier(modifiers.isShift());
        currentView.applyCtrlModifier(modifiers.isCtrl());
        currentView.applyAltModifier(modifiers.isAlt());
    }

    // ------------------------------------------------------------------
    // Key handling (called by KeyboardButtonView)
    // ------------------------------------------------------------------

    @Override
    public void onPress(int code) {
        playSound();
        vibrate(20);
    }

    @Override
    public void onKey(int code) {
        // Page switching first - rebuilds the keyboard.
        switch (code) {
            case KeyboardDefinitions.CODE_PAGE_ABC:
                stateManager.setCurrentPage(KeyboardDefinitions.PAGE_ABC);
                return;
            case KeyboardDefinitions.CODE_PAGE_SYM:
                stateManager.setCurrentPage(KeyboardDefinitions.PAGE_SYM);
                return;
            case KeyboardDefinitions.CODE_PAGE_PROG:
                stateManager.setCurrentPage(KeyboardDefinitions.PAGE_PROG);
                return;
            case KeyboardDefinitions.CODE_PAGE_NAV:
                stateManager.setCurrentPage(KeyboardDefinitions.PAGE_NAV);
                return;
            case KeyboardDefinitions.CODE_IME_SWITCH:
                showImePicker();
                return;
            default:
                break;
        }

        // Control keys (editing actions, modifiers).
        if (dispatcher.handleKey(code)) {
            updateModifierViews();
            return;
        }

        // Navigation and action keys.
        if (dispatcher.handleNavigationKey(code)) {
            updateModifierViews();
            return;
        }

        // Space.
        if (code == KeyboardDefinitions.CODE_SPACE) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                modifiers.consumeBeforeKey(ic);
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
            }
            return;
        }

        // Plain character input.
        boolean shiftWanted = modifiers.isShift();
        char c = (char) code;
        try {
            if (Character.isLetter(c)) {
                // Letters are sent as key events so ctrl+letter combos keep
                // working in terminals and code editors (reference behavior).
                InputConnection ic = getCurrentInputConnection();
                if (ic == null) return;
                int meta = modifiers.consumeBeforeKey(ic);
                String keyCodeName = "KEYCODE_" + Character.toUpperCase(c);
                int ke = KeyEvent.keyCodeFromString(keyCodeName);
                if (ke != KeyEvent.KEYCODE_UNKNOWN) {
                    ic.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, ke, 0, meta));
                    ic.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP, ke, 0, meta));
                } else {
                    dispatcher.commitCharacter(c, shiftWanted);
                }
            } else if (c != 0) {
                dispatcher.commitCharacter(c, shiftWanted);
            }
        } catch (Exception e) {
            Log.w(TAG, "Key handling failed for code " + code, e);
        }
    }

    @Override
    public void onText(CharSequence text) {
        // Output-text keys (multi-char labels), committed directly.
        if (!dispatcher.commitText(text)) {
            Log.w(TAG, "No input connection for text key");
        }
    }

    @Override
    public void onRelease(int code) {
        // Nothing needed: repeat timers are owned by the button view.
    }

    @Override
    public void onLongPress(int code) {
        if (code == KeyboardDefinitions.CODE_SHIFT) {
            vibrate(50);
            modifiers.toggleShiftLock();
            updateModifierViews();
        } else if (code == KeyboardDefinitions.CODE_SPACE) {
            vibrate(50);
            showImePicker();
        }
    }

    private void showImePicker() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
    }

    // ------------------------------------------------------------------
    // Feedback
    // ------------------------------------------------------------------

    private void playSound() {
        if (!soundEnabled || audioManager == null) return;
        try {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        } catch (Exception e) {
            Log.w(TAG, "Keypress sound failed", e);
        }
    }

    private void vibrate(int ms) {
        if (!vibrateEnabled || vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        } catch (Exception e) {
            Log.w(TAG, "Vibration failed", e);
        }
    }
}
