# Privacy Policy — Dual Mode Keyboard

_Last updated: 2026-11-15_

## Summary

**Dual Mode Keyboard does not collect, store, or transmit any personal data.
It has no network access at all.**

## What the app does NOT do

- It does **not** log keystrokes or typed content, anywhere, ever.
- It does **not** collect or transmit passwords. Password fields
  (`TYPE_TEXT_VARIATION_PASSWORD`, `TYPE_NUMBER_VARIATION_PASSWORD`,
  `TYPE_TEXT_VARIATION_WEB_PASSWORD`) are detected and long-press popups are
  disabled in them.
- It does **not** transmit anything. The app requests no `INTERNET` permission,
  contains no analytics SDKs, no ads, no trackers, and makes no network calls
  of any kind.
- It does **not** read your clipboard in the background. The clipboard is read
  exclusively at the moment you tap the **Paste** key.
- It does **not** store typed text. Nothing you type is ever written to disk.

## What the app DOES access, and why

1. **The focused text field (via `InputConnection`)** — this is what every
   keyboard must do to insert the characters you press. The keyboard writes
   committed text and reads only what is needed for its features:
   the current selection (for Copy/Cut/Select All) and surrounding text
   (for backspace and Select All).
2. **The system clipboard** — set when you tap Copy or Cut, read when you tap
   Paste. Nothing else.
3. **Local settings storage** — your theme, sizes, sound/vibration preferences
   and preferred mode are stored in a private DataStore file that only this
   app can access. This data never leaves your device.
4. **Vibration** — for key press haptic feedback (permission `VIBRATE`).

## Permissions

| Permission | Purpose |
|---|---|
| `BIND_INPUT_METHOD` | Required by Android to run as a keyboard |
| `VIBRATE` | Key press haptic feedback |

That is the complete list. In particular there is **no** `INTERNET`,
`ACCESS_NETWORK_STATE`, `READ_CONTACTS`, `RECORD_AUDIO`, `CAMERA`, or location.

## Data sharing

None. There is nothing to share: the app never sends data anywhere.

## Contact

If you have questions about this policy, open an issue in the project repository.
