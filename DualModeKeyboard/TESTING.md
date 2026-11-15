# Testing Report — Dual Mode Keyboard

## Automated tests (run in CI)

| Test file | What it verifies |
|---|---|
| `KeyboardLayoutsTest` | English layout structure (toolbar + 4 rows), QWERTY keys present, shift produces uppercase, number page digits, Bengali digits on the bn number page, Bengali consonants/vowels/vowel signs present, shifted Bengali extra consonants (শ, ক্ষ, ড়) and Bengali digits, shortcut layout contains the Ctrl toggle and exactly all 14 shortcuts with correct labels |
| `KeyboardStateTest` | Shift cycle OFF→ON→LOCKED→OFF, shift auto-reset after one typed char, caps lock persistence, language toggle resets page/shift, Ctrl toggle flips, page change resets shift |
| `ShortcutManagerTest` | Every shortcut declares a valid `KEYCODE_*` name, round-trip lookup, exactly 14 shortcuts (4 clipboard + 5 editing + 5 document) |

Run locally with `./gradlew test` (requires JDK + SDK; use GitHub Actions in Termux setups).

## CI verification

| Item | Status |
|---|---|
| GitHub Actions workflow present | Yes — `.github/workflows/android-build.yml` |
| Workflow validates wrapper, runs `test`, `lint`, `assembleDebug`, `assembleRelease`, `bundleRelease`, packages source ZIP, uploads artifacts | Yes |

> Build status must be confirmed by an actual green Actions run after pushing the
> repository. It cannot be claimed from Termux (no Android SDK available there).

## Manual / real-device testing checklist

These require a physical device or emulator and should be verified after installing
the debug APK:

- [ ] IME appears in **Settings → System → On-screen keyboard**
- [ ] Keyboard opens in a text field; typing English works
- [ ] Shift/caps lock behave; long-press alternates popup works
- [ ] Bengali layout inserts Bengali characters; EN ⇄ বাং switching works
- [ ] Number/symbol/emoji pages open and return correctly
- [ ] ⇄ switches to the Ctrl shortcut keyboard without losing focus
- [ ] Copy/Cut/Paste/Select All work in the Messages app, Chrome address bar and Google Keep
- [ ] Ctrl+Z / Ctrl+S etc. show the "sent — depends on app" toast; actual effect visible in Termux (e.g. Ctrl+C in a running program)
- [ ] Password field: popups disabled, typing hidden
- [ ] Theme/sound/vibration/size changes apply to the open keyboard instantly and persist after app restart
- [ ] Keyboard survives app switching without crashing; modifier state resets on focus change
- [ ] No crash when the keyboard is opened on different screen sizes and Android 8/10/13/14+

## Known platform limitations (honest list)

1. **Undo/Redo/Save/Print/Open/New/Find/Bold/Italic/Center** cannot be forced onto
   other apps by any Android keyboard. They are dispatched as real Ctrl key events
   and honored only by apps that implement them.
2. **Bengali input is a direct character layout**, not a phonetic transliteration
   method. It is not advertised as phonetic typing.
3. The app **cannot** change the system default keyboard silently — only the user
   can, via system settings (the app opens the correct screens for them).
4. Full-screen (landscape) extract mode is disabled (`onEvaluateFullscreenMode`
   returns false) to keep the custom keyboard always visible.
