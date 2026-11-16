# Testing Report — Coding Keys

Status legend: ✅ executed and passed · ⬜ planned/manual (requires an on-device environment) · ❌ known limitation

## 1. Automated tests (executed in CI)

Run via `./gradlew clean testDebugUnitTest assembleDebug` on GitHub Actions
(ubuntu-latest, JDK 17). Result of the run that produced the shipped debug APK
is recorded in the Actions run; the table below documents the suites:

| Suite | Tests | What is verified |
|---|---|---|
| `KeyboardLayoutBuilderTest` | 7 | builder math: key counts, normalized bounds, equal-width keys, size-weighted widths, row heights, empty-row failure |
| `KeyboardDefinitionsTest` | 10 | QWERTY(27)/QWERTZ(27)/AZERTY(29)/DVORAK(28)/number-row(13) key counts; symbol rows (char→key, spaces skipped); custom-code uniqueness |
| `SymbolLayoutManagerTest` | 9 | parse/serialize round trip, sanitizing, limits, reorder, empty-row dropping, defaults |
| `ThemeDefinitionsTest` | 7 | 12 themes present, sane colors, fallback index, custom slot, palette copy |
| `UndoRedoManagerTest` | 8 | local undo/redo inversion of commit/back-delete/forward-delete via a fake InputConnection; honest result reporting; bounded payloads; redo reset |

Total: 41 automated tests. CI additionally runs `lint` (report uploaded as artifact).

**Verification of the test run:** the CI run must be green before the APK
artifact is used. Do not trust this document for a specific commit - check the
Actions run that built that commit.

## 2. Installation / activation tests (manual, on device)

⬜ Install APK via `adb install -r` and by opening it in a file manager
⬜ Launch dashboard; status card shows "Not enabled yet"
⬜ *Enable Coding Keys* opens the system Manage-keyboards screen; toggling works
⬜ Switch keyboards from the dashboard button and via space-bar long-press
⬜ Keyboard shows/hides on field focus; IME picker lists Coding Keys

## 3. Typing tests (manual, on device)

⬜ Lowercase/uppercase (shift, shift-lock via long-press), digits row
⬜ All symbol rows on `#+=`; braces panel on `</>`; bottom-row custom symbols
⬜ Tab in Termux (vim), Esc works, Enter sends newline in multiline and IME action in single-line (search/send)
⬜ Backspace repeat, arrow repeat (held), forward delete on Nav page
⬜ CTRL + a/c/x/v/z in Termux; CTRL+LEFT/RIGHT word jumps in editors that support them
⬜ Page keys `Abc / #+= / </> / Nav` switch instantly with active-page highlight
⬜ Password field: no crash, no logging, normal masked input

## 4. Editing tests (manual, on device)

⬜ Select All → Cut/Copy/Paste in Google Keep, Gmail compose, VSCode-remote, Termux
⬜ SHIFT+arrows extends selection; Shift+Ctrl+arrows selects words (TextView apps)
⬜ Undo/Redo in editors with native support (Keep, Notes) and the local fallback
   after keyboard-initiated edits in plain editors
⬜ Selection then type replaces selection; backspace with selection deletes it

## 5. Large-document performance (manual)

⬜ 5,000-line file in a code editor: Select All uses the editor action (no lag, no full extraction)
⬜ Rapid typing sustained (repeat rate 50 ms) - no ANR (repeats run on the main looper with cheap handler posts)
⬜ Keyboard open/close cycles 20× in a huge document - no memory growth (undo stack capped at 100 ops / 10k chars each)

## 6. Theme / layout tests (manual)

⬜ All 12 themes render; gradient themes (Premium Blue, Ocean Blue, Premium Gradient) show the gradient
⬜ Custom theme: all 7 colors apply, invalid hex falls back, persistence across restarts
⬜ Layout switch (QWERTY/AZERTY/QWERTZ/DVORAK) reflected immediately and persisted
⬜ Symbol editor: add/remove/reorder rows, restore defaults, live preview matches keyboard
⬜ Height/font/radius/spacing sliders change the keyboard on next show, persist across reboot

## 7. Lifecycle / robustness tests (manual)

⬜ Orientation change while typing: page resets to Abc, no crash, modifiers released
⬜ App switching mid-typing: no stuck Ctrl (meta keys released on `onStartInput`)
⬜ Field with no InputConnection: keys no-op without crash
⬜ Invalid preferences (wrong types) fall back to defaults instead of crashing

## 8. Navigation bar integration (manual, on device)

The keyboard must sit immediately above the real Android system navigation
bar - never overlapping, clipping or hiding keys. The IME window keeps
fitting the system bars (no `FLAG_LAYOUT_NO_LIMITS`, no fake bar); real
`WindowInsets` are respected via `WindowInsetsCompat`.

**3-button navigation (Back / Home / Recents visible):**
⬜ Portrait: keyboard bottom edge touches the top of the navigation bar; the
   bottom key row (Abc/`#+=`/`</>`/Nav, space, Enter) is fully visible
⬜ Landscape: same, on every screen size tested
⬜ Every key fully tappable; nothing drawn behind the Back/Home/Recents buttons
⬜ Navigation buttons remain visible and functional while the keyboard is open
⬜ With a themed keyboard, the area behind the navigation bar matches the
   keyboard background (edge-to-edge devices); on fitted windows the bar is
   owned by the system/app as usual

**Gesture navigation:**
⬜ Gesture pill area never covers the bottom key row
⬜ Swipe-up/home gestures work while the keyboard is open

**Edge cases:**
⬜ Android 15 (edge-to-edge enforced): keys lift above the navigation bar
   inset; the keyboard background continues behind the bar
⬜ Tablet/foldable landscape (side navigation bar): keys shift horizontally
   out of the bar area
⬜ Keyboard height preference (20–60%) combines correctly with the inset:
   the key area keeps its configured height and the inset is added below
⬜ Split-screen: keyboard sits above the navigation bar inside the focused
   split's window

## Known limitations (documented honestly)

1. **Undo/Redo support varies by editor.** Editors exposing `performContextMenuAction(UNDO/REDO)` (e.g. TextView-based apps) get full native undo. Others get the local fallback, which only covers keyboard-initiated changes - it cannot reverse edits the app made itself. Coding Keys reports failure instead of pretending.
2. **Select All fallback.** If an editor rejects the context action, Coding Keys refuses to extract the entire document just to select it (large-document rule); the operation is reported unsupported in those apps.
3. **Key events vs. text.** Letters are sent as key events (so CTRL+letter works in terminals). A few editors ignore synthesized key events for letters; such apps also ignore hardware keyboards and are rare.
4. **Sticky Alt** is honored by editors that map ALT meta state; some apps ignore Alt entirely.
5. **Clipboard access** on Android 13+ may show the system "pasted from Coding Keys" toast - that is the platform, not data collection.

## Fix policy

Any reproducible failure from sections 1–4 results in a fix commit before the
affected APK is distributed. Failures in manual sections are recorded here with
the device/Android version until resolved.
