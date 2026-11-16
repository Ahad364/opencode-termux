# Coding Keys

A complete, professional, privacy-first **Android keyboard for programmers**, built in pure **Java** with a dynamically generated keyboard UI.

Coding Keys puts everything a coder needs on the phone keyboard: programming braces, a dedicated programming symbol panel, developer control keys (ESC, TAB, CTRL, SHIFT, ALT, HOME, END, DELETE), repeatable arrows, a full editing toolbar (Select All / Cut / Copy / Paste / Undo / Redo), four alphabet layouts, twelve premium themes, a custom symbol editor and a professional settings dashboard.

> **Attribution:** Coding Keys is derived from [CodeBoard 5.0.3](https://github.com/gazlaws-dev/codeboard) by Gazala Muhamed (GNU GPL-3.0-or-later). The keyboard architecture (layout builder, row builder, box geometry, key/button views, IME event flow) is based on the original work. See [ATTRIBUTION.md](ATTRIBUTION.md) and [LICENSE](LICENSE).

---

## Features

### Keyboard
- Dynamically generated pages (no static keyboard XML): **Abc**, `#+=` symbols, `</>` programming, **Nav**
- Alphabet layouts: **QWERTY, AZERTY, QWERTZ, DVORAK**
- Programming panel: `{ } [ ] ( ) < >`, `+ - = * / % ^ ~ _ |`, `! @ # $ & \ ' " \``
- Developer keys: **ESC, TAB, CTRL, SHIFT (lock on long-press), ALT, HOME, END, PgUp, PgDn, forward-DELETE**
- Repeatable navigation keys and backspace (configurable delay/rate)
- Direct page switching keys on every page (no cycling)

### Editing (works with short text and huge documents)
- **Select All, Cut, Copy, Paste** through editor-side context actions first - never extracts the whole document
- **Undo / Redo** - uses the editor's native undo where supported; otherwise a bounded local fallback inverts the keyboard's own commits, deletes and pastes
- **SHIFT + arrows** selects (via editor meta-key events)
- **CTRL + LEFT / RIGHT** moves by word, with a bounded-scan fallback for editors that ignore ctrl+arrows
- **Navigation bar safe**: keys always sit above the Android 3-button navigation bar (and gesture pill); real `WindowInsets` are respected on every screen size, orientation and edge-to-edge configuration - no `FLAG_LAYOUT_NO_LIMITS`, no fake bar
- **HOME / END / PgUp / PgDn** navigation
- Honest result reporting: an operation is only treated as successful when the editor confirms it

### Themes (12)
Premium Blue, Midnight Blue, Deep Purple, Cyber Dark, AMOLED Black, Pure Black, Material Dark, Material Light, Ocean Blue, Neon Purple, Premium Gradient, and a fully **Custom theme** (7 editable colors incl. optional background gradient). Theme changes apply to the keyboard immediately - no reinstall.

### Settings dashboard
General (activation, toolbar mode, keyboard heights), Typing (sound, vibration, key repeat, sticky modifiers), Appearance (preview, border, font size, corner radius, key spacing, heights) and Layout (alphabet layout + symbol row editor). All stored locally in SharedPreferences.

### Privacy first
- **No INTERNET permission** - the keyboard physically cannot transmit anything
- No keystroke logging, no analytics, no background services
- Clipboard is read only at the moment you tap Paste, and never stored
- Password fields are handled by the platform; Coding Keys never logs content
- See [PRIVACY.md](PRIVACY.md)

---

## Build

Requirements: JDK 17, Android SDK 35, Gradle 8.9 (wrapper included).

```bash
./gradlew clean testDebugUnitTest assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/`.

**Cloud build (recommended from Termux):** push to GitHub - the included workflow (`.github/workflows/android.yml`) runs unit tests, lint, and builds debug + release APKs on every push/PR/manual trigger. The APKs appear under *Actions → run → Artifacts*. See [GITHUB_SETUP.md](GITHUB_SETUP.md) and [TERMUX.md](TERMUX.md).

## Install

Full guide in [INSTALL.md](INSTALL.md). Short version:

1. Install the APK (`adb install` or open it on the phone)
2. *Settings → System → Keyboards → On-screen keyboard → Manage keyboards* → enable **Coding Keys**
3. Open any text field, switch keyboards (long-press the space bar of your current keyboard, or the notification) and pick **Coding Keys**
4. The in-app dashboard shows your activation status and has direct buttons for both steps

## Project structure

```
CodingKeys/
├── app/src/main/java/com/codingkeys/keyboard/
│   ├── MainActivity.java            # dashboard
│   ├── CodingKeysIME.java           # the input method service
│   ├── core/                        # preferences, state, dispatcher,
│   │                                # modifier + InputConnection managers
│   ├── layout/                      # dynamic keyboard builder + views
│   │                                # + symbol layout manager
│   ├── editing/                     # selection, clipboard, cursor,
│   │                                # undo/redo managers
│   ├── theme/                       # theme engine (12 themes)
│   └── settings/                    # Settings/Theme/Layout/SymbolEditor activities
├── app/src/main/res/                # layouts, drawables, values, xml/method
├── app/src/test/java/               # JUnit unit tests
└── .github/workflows/android.yml    # CI: test + lint + debug/release APK
```

## Testing

See [TESTING.md](TESTING.md). Unit tests run in CI (`testDebugUnitTest`); the document also lists the manual on-device test plan and honest platform limitations (third-party editors differ in IME support).

## License

GNU GPL-3.0-or-later - see [LICENSE](LICENSE). Coding Keys is free software; you may redistribute and/or modify it under the terms of the GPL. Original CodeBoard code remains copyright Gazala Muhamed.
