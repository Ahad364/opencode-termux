# Dual Mode Keyboard

A **production-quality, native Android keyboard application** with two modes in one app:

1. **Normal Keyboard Mode** — a full typing keyboard (English QWERTY + Bengali layout, numbers, symbols, emoji).
2. **Ctrl Shortcut Keyboard Mode** — a dedicated interface with large Ctrl shortcut keys (Copy, Paste, Cut, Select All, Undo, Redo, Bold, Italic, Center, Save, Print, Open, New, Find).

Real Android IME, real typing, real shortcuts. No WebView, no HTML, no prototypes.

---

## Table of contents
1. [Features](#features)
2. [Technology stack](#technology-stack)
3. [Project structure](#project-structure)
4. [Development requirements](#development-requirements)
5. [Termux setup](#termux-setup)
6. [GitHub repository & Actions setup](#github-repository--actions-setup)
7. [APK / AAB builds](#apk--aab-builds)
8. [Release signing (optional)](#release-signing-optional)
9. [Source ZIP packaging](#source-zip-packaging)
10. [Installation instructions](#installation-instructions)
11. [Enabling the keyboard](#enabling-the-keyboard)
12. [What the Ctrl shortcuts really do on Android](#what-the-ctrl-shortcuts-really-do-on-android)
13. [Privacy](#privacy)
14. [Troubleshooting](#troubleshooting)

---

## Features

### Normal keyboard mode
- QWERTY English layout, lowercase/uppercase, Shift and Caps Lock
- Number page, two symbol pages, emoji panel
- Long-press alternatives popup (accents and extra punctuation)
- **Bengali layout**: real Bengali characters (consonants, vowels, vowel signs, punctuation), Bengali digits (Shift on the vowel row / 123 page), one-tap EN ⇄ বাং switching
- Backspace with press-and-hold repeat
- Smart Enter: triggers the editor's action (Go/Search/Send/Done) or inserts a newline
- Input-type aware layouts: normal text, email, password, number, phone, multiline

### Ctrl shortcut mode
- One-tap switch with the ⇄ key; the user's last mode is remembered
- Sticky Ctrl toggle with visual active state
- Clipboard shortcuts implemented **directly** through `InputConnection`:
  Copy (Ctrl+C), Cut (Ctrl+X), Paste (Ctrl+V), Select All (Ctrl+A)
- Real `KeyEvent` dispatch with `META_CTRL_ON` for: Undo (Ctrl+Z), Redo (Ctrl+Y),
  Bold (Ctrl+B), Italic (Ctrl+I), Center (Ctrl+E), Save (Ctrl+S), Print (Ctrl+P),
  Open (Ctrl+O), New document (Ctrl+N), Find (Ctrl+F)
- Debounced execution to prevent accidental repeated shortcuts
- Honest feedback: the keyboard tells you what happened (e.g. "Copied 12 characters",
  "Ctrl+S sent — works in apps that support it")

### Settings dashboard (Jetpack Compose, Material 3)
- Home dashboard: keyboard enabled/default status, current mode, active theme, quick settings, activation guide
- Live interactive keyboard preview (the same renderer the IME uses)
- 6 themes: Premium Dark, Premium Light, Midnight Blue, Purple Neon, Electric Blue, Minimal Black
- Per-element color overrides: accent, key background, key text, keyboard background
- Keyboard size customization: key height, corner radius, key spacing, font size
- Sound (toggle + volume), vibration (toggle + strength), animation toggle
- Language settings, shortcut manager reference, privacy screen, about screen
- All settings persist locally via **Jetpack DataStore**

---

## Technology stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Minimum SDK | 26 (Android 8.0) |
| Target/Compile SDK | 35 |
| IME engine | `InputMethodService` + custom Canvas-rendered `KeyboardView` |
| Settings app UI | Jetpack Compose (Material 3) |
| Storage | Jetpack DataStore (Preferences) |
| Architecture | MVVM + clean separation (`ime`, `keyboard`, `shortcuts`, `data`, `ui`, `themes`) |
| Build | Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`) |
| CI | GitHub Actions |

**Note on Room:** no local database is required by any feature — DataStore covers all persistence, so Room is intentionally not included (the project brief allows "Room only where necessary").

---

## Project structure

```
DualModeKeyboard/
├── app/
│   ├── src/main/
│   │   ├── java/com/dualmode/keyboard/
│   │   │   ├── MainActivity.kt
│   │   │   ├── KeyboardApplication.kt
│   │   │   ├── ime/
│   │   │   │   ├── KeyboardService.kt        # the real InputMethodService
│   │   │   │   ├── KeyboardController.kt     # view building + mode switching
│   │   │   │   ├── KeyboardState.kt          # shift/ctrl/page/mode state machine
│   │   │   │   └── EditorHandler.kt          # EditorInfo / input-type handling
│   │   │   ├── keyboard/
│   │   │   │   ├── KeyboardView.kt           # Canvas renderer (keys, popups, press FX)
│   │   │   │   ├── KeyboardLayouts.kt        # EN/BN/number/symbol/emoji/shortcut layouts
│   │   │   │   └── Key.kt                    # key model
│   │   │   ├── shortcuts/
│   │   │   │   ├── ShortcutManager.kt        # ShortcutId definitions
│   │   │   │   ├── ShortcutExecutor.kt       # routing + debounce
│   │   │   │   ├── ClipboardHandler.kt       # copy/cut/paste/select-all
│   │   │   │   ├── KeyEventSender.kt         # real Ctrl key events
│   │   │   │   ├── EditingHandler.kt         # undo/redo
│   │   │   │   └── FormattingHandler.kt      # bold/italic/center/save/print/…
│   │   │   ├── data/                         # KeyboardSettings + SettingsRepository (DataStore)
│   │   │   ├── themes/                       # 6 premium themes
│   │   │   ├── settings/                     # SettingsViewModel
│   │   │   ├── ui/                           # Compose dashboard (11 screens)
│   │   │   └── utils/
│   │   ├── res/ (values, xml/method.xml, icons)
│   │   └── AndroidManifest.xml
│   ├── src/test/                             # JUnit tests (layouts, state machine, shortcuts)
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── .github/workflows/android-build.yml       # CI: tests, lint, APK, AAB, source ZIP
├── scripts/
│   ├── termux-setup.sh
│   ├── build.sh
│   └── package-source.sh
├── README.md · PRIVACY.md · TESTING.md
└── gradlew / gradlew.bat / gradle wrapper
```

---

## Development requirements

- **Android Studio** (Koala or newer) with JDK 17
- Android SDK Platform 35
- For CI: any GitHub-hosted runner (Java + SDK are installed by the workflow)
- **Termux**: only for source management and workflow control (see below)

---

## Termux setup

In Termux:

```bash
curl -fsSL <your-repo-url>/scripts/termux-setup.sh -o setup.sh
bash setup.sh          # installs git, curl, zip, unzip, jq, openjdk-17
```

Or manually:

```bash
pkg update -y
pkg install -y git curl zip unzip jq openjdk-17
```

Clone and work on the project:

```bash
git clone https://github.com/<your-user>/DualModeKeyboard.git
cd DualModeKeyboard
git checkout -b my-feature
# ... edit ...
git add .
git commit -m "My change"
git push origin my-feature
```

> **Important:** a full Android SDK build inside Termux is not supported. Termux is for
> source management, triggering workflows and downloading artifacts. `scripts/build.sh`
> explains this and defers to GitHub Actions.

### Checking build status and downloading artifacts from Termux

```bash
# After pushing, watch the workflow run:
gh run list --workflow=android-build.yml
gh run watch <run-id>

# Download APK / AAB / source ZIP artifacts:
gh run download <run-id> -D ~/downloads
```

(`gh` can be installed with `pkg install gh`, then `gh auth login`.)

---

## GitHub repository & Actions setup

1. Create an empty GitHub repository.
2. Push this project:
   ```bash
   git init && git add . && git commit -m "Dual Mode Keyboard v1.0"
   git branch -M main
   git remote add origin https://github.com/<your-user>/DualModeKeyboard.git
   git push -u origin main
   ```
3. The included workflow `.github/workflows/android-build.yml` automatically runs on
   **push**, **pull request**, and **manual dispatch**, and performs:
   - Gradle wrapper validation
   - unit tests (`./gradlew test`)
   - lint (`./gradlew lint`)
   - debug APK (`assembleDebug`), release APK (`assembleRelease`), AAB (`bundleRelease`)
   - source ZIP packaging (`scripts/package-source.sh`)
   - artifact uploads: debug APK, release APK, AAB, source ZIP, build reports

Do not consider any build "successful" until the Actions run shows a green checkmark.

---

## APK / AAB builds

| Artifact | Gradle task | Output |
|---|---|---|
| Debug APK | `./gradlew assembleDebug` | `app/build/outputs/apk/debug/` |
| Release APK (unsigned unless signing configured) | `./gradlew assembleRelease` | `app/build/outputs/apk/release/` |
| Release AAB | `./gradlew bundleRelease` | `app/build/outputs/bundle/release/` |

Artifacts appear on each Actions run page under **Artifacts**. The debug APK is
installable immediately; the release APK/AAB need signing (below).

## Release signing (optional)

1. Create a keystore:
   ```bash
   keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   base64 -w0 release.jks > release.jks.b64
   ```
2. In the GitHub repository, add a **repository variable** `SIGNING_ENABLED=true` and
   **secrets**: `KEYSTORE_BASE64` (contents of `release.jks.b64`), `KEYSTORE_PASSWORD`,
   `KEYSTORE_ALIAS`, `KEY_PASSWORD`.
3. Re-run the workflow — `assembleRelease` then produces a **signed** release APK.

Never commit keystore files or passwords.

## Source ZIP packaging

```bash
bash scripts/package-source.sh
```
Validates the project structure, creates `DualModeKeyboard-Source.zip` (excluding
`.git/`, `build/`, `.gradle/`, `local.properties`, keystores and temp files), verifies
the archive, and prints its location and size. CI uploads it as an artifact named
`DualModeKeyboard-Source`.

---

## Installation instructions

1. Download `app-debug.apk` from a green Actions run (or build locally in Android Studio).
2. On the device, allow "Install unknown apps" for your file manager/browser.
3. Install the APK.
4. Enable and select the keyboard (next section).

## Enabling the keyboard

Typical path (varies by manufacturer — Samsung/Xiaomi/etc. use different wording):

**Settings → System → Languages & input → On-screen keyboard (or Manage keyboards) → enable "Dual Mode Keyboard"**, then select it as the current input method.

From the app's Home dashboard you can jump straight there with the
**Enable keyboard** and **Choose keyboard** buttons (the app opens the system
settings / input method picker for you — it cannot change system settings silently).

---

## What the Ctrl shortcuts really do on Android

Honest platform behavior, as implemented:

| Shortcut | Implementation | Scope |
|---|---|---|
| Copy (Ctrl+C) | Direct `InputConnection.getSelectedText` → system clipboard | Works in ordinary editors with selectable text |
| Cut (Ctrl+X) | Copy + delete selection via `InputConnection` | Same |
| Paste (Ctrl+V) | Read system clipboard (only on your tap) → `commitText` | Works wherever text can be committed |
| Select All (Ctrl+A) | `getExtractedText` + `setSelection` | Works in editors that expose full text |
| Undo / Redo / Bold / Italic / Center / Save / Print / Open / New / Find | Real `KeyEvent` with `META_CTRL_ON` sent via `InputConnection.sendKeyEvent` | Only in apps that implement desktop-style Ctrl handling (e.g. Termux, Google Docs, some office/code editors). Others ignore the event; the keyboard shows "sent — depends on app" |

Android does not allow an IME to force apps to implement these commands — any
keyboard claiming otherwise is not telling the truth. This keyboard implements the
maximum that the platform honestly allows.

## Privacy

- No `INTERNET` permission — the app **cannot** transmit anything.
- No keystroke logging, no analytics, no ads, no tracking.
- Password fields are detected; long-press popups are disabled there.
- The clipboard is read **only** when you tap Paste.
- Full policy: [PRIVACY.md](PRIVACY.md).

## Troubleshooting

| Problem | Fix |
|---|---|
| Keyboard not in the input-method list | Reboot after enabling in system settings; check that the app is not "force stopped" |
| Shortcut keys don't do anything in app X | That app doesn't implement Ctrl keys — use the clipboard keys (Copy/Paste work almost everywhere) |
| Bengali characters look wrong in some apps | The app's font may lack Bengali glyphs — this is a rendering issue of the target app, not the keyboard |
| Preview in settings feels laggy | The preview re-renders live; performance inside real apps is faster because the IME view persists |
| Ctrl mode resets when switching apps | By design: the Ctrl modifier resets when input focus changes, to avoid accidental combos |

---

**License**: MIT (see repository). Built with Kotlin, Android SDK and Jetpack Compose.
