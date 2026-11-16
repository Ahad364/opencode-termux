# Installation Guide — Coding Keys

## 1. Get the APK

**Option A — GitHub Actions (recommended):**
1. Push the project to GitHub (see [GITHUB_SETUP.md](GITHUB_SETUP.md)) or open the repository's Actions tab.
2. Open the latest **Android Build** run → **Artifacts**.
3. Download `CodingKeys-debug-apk` and unzip it to get `app-debug.apk`.

**Option B — build locally** (needs JDK 17 + Android SDK 35):
```bash
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk
```

## 2. Install the APK

On the phone (directly):
```bash
adb install -r app-debug.apk
```
or copy `app-debug.apk` to the phone, tap it, and allow "Install unknown apps" for your file manager when prompted.

## 3. Enable the keyboard (one time)

1. Open **Coding Keys** from the launcher.
2. Tap **Enable Coding Keys** → the system *Manage keyboards* screen opens.
3. Turn on **Coding Keys**. Android shows a security notice for third-party keyboards: confirm it. Coding Keys requests no network permission, so it cannot send any data (see [PRIVACY.md](PRIVACY.md)).

You can also reach the same screen manually:
*Settings → System → Keyboards → On-screen keyboard → Manage keyboards*

## 4. Set Coding Keys as your keyboard

1. Open any app with a text field (the in-app **Try the keyboard** box works too).
2. Switch keyboards:
   - long-press the **space bar** of your current keyboard and pick **Coding Keys**, or
   - use the **keyboard icon** in the navigation bar / notification shade, or
   - tap **Switch keyboard** on the Coding Keys dashboard.
3. The dashboard shows **Active keyboard** when Coding Keys is current.

## 5. First steps on the keyboard

| Key | Action |
|---|---|
| `#+=` | symbol page |
| `</>` | dedicated programming panel |
| `Nav` | arrows, Home/End, PgUp/PgDn, Del |
| `Shift` long-press | caps-style shift lock |
| `Space` long-press | switch to another keyboard |
| `Ctrl` then `a/c/x/v` | select all / copy / cut / paste |

## 6. Verify it works

Type in the dashboard's text box:
- letters + digits (Abc page number row)
- `{ } [ ] ( ) < > ; : , . ? !` on the symbol page
- `Tab` in a code editor, `Esc` in Termux/vim
- arrows with `Shift` held to select, then `Cut` / `Copy` / `Paste` from the toolbar

## 7. Uninstall

Uninstall like any app (long-press the icon → Uninstall). No data is kept beyond preferences, which are removed with the app.

## Troubleshooting

| Symptom | Fix |
|---|---|
| Keyboard not listed | Reboot after first install, then re-check *Manage keyboards* |
| Arrows/Tab ignored in one app | That app ignores IME key events; use the Nav page fallbacks — see TESTING.md limitations |
| Undo does nothing in one app | The app implements neither context-undo nor editable selection; Coding Keys reports this honestly instead of pretending |
| Keyboard too small | Settings → Keyboard height (portrait) slider |
