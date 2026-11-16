# Privacy Policy — Coding Keys

Last updated: 2026-11-16

Coding Keys is a keyboard for programmers. Its privacy model is simple: **everything stays on your device.**

## What we collect
**Nothing.** There is no server, no account, no analytics and no crash reporting.

## Technical guarantees
- The app manifest requests exactly one permission: `VIBRATE` (for keypress feedback).
- There is **no `android.permission.INTERNET`** anywhere in the manifest. Android therefore makes it impossible for the app to open a network connection. Your keystrokes, source code and clipboard content cannot leave your phone through Coding Keys.
- No third-party SDKs or libraries are included (only AndroidX, which does not add network telemetry).
- All settings are stored in the app's private SharedPreferences, readable only by the app.

## Keyboard-specific guarantees
- Coding Keys never stores, logs, buffers or transmits typed text. Nothing you type is written to persistent storage.
- The system clipboard is read **only** at the instant you tap Paste, and never saved.
- Undo/Redo fallback data (bounded, in-memory only) contains short text fragments from your own recent edits and is discarded when the editor closes or the service restarts. It is never written to disk.
- IME services are bound by Android's `BIND_INPUT_METHOD` permission; other apps cannot read your input through the standard IME channel.

## Password fields
The platform decides how password fields behave; Coding Keys does not inspect, log or special-case their content beyond normal text entry. Nothing typed into any field is recorded.

## Permissions summary
| Permission | Purpose |
|---|---|
| `android.permission.VIBRATE` | Optional keypress vibration |

## Changes to this policy
Any change will be published in this repository with a new date above.

## Contact
Open an issue in this project's GitHub repository.
