# Attribution and License Compliance

Coding Keys is a derivative work of the **CodeBoard** project and complies with
its GNU GPL-3.0-or-later license by being licensed under the same terms.

## Origin

- **Upstream project:** CodeBoard — Keyboard for coding
- **Repository:** https://github.com/gazlaws-dev/codeboard
- **Reference version:** tag `5.0.3`
- **Upstream author / copyright:** Gazala Muhamed ("Ruby / gazlaws"), 2020
- **Upstream license:** GNU General Public License v3.0 or later
  (`LICENSE.txt` in the CodeBoard repository)

## What was derived

The following architectural pieces in Coding Keys are **derived from CodeBoard
5.0.3 source code** (restructured, extended and rebranded for Coding Keys):

- `layout/Box.java` — normalized keyboard geometry (from `Box.java`)
- `layout/Key.java`, `layout/KeyInfo.java` — key model (from `Key.java`, `KeyInfo.java`)
- `layout/KeyboardLayoutException.java`
- `layout/KeyboardLayoutRowBuilder.java` — row layout math (from `KeyboardLayoutRowBuilder.java`)
- `layout/KeyboardLayoutBuilder.java` — fluent keyboard builder (from `KeyboardLayoutBuilder.java`)
- `layout/KeyboardButtonView.java` — per-key rendering/press handling (from `KeyboardButtonView.java`)
- `layout/KeyboardLayoutView.java` — keyboard panel view (from `KeyboardLayoutView.java`)
- `layout/KeyboardUiFactory.java` — view factory (from `KeyboardUiFactory.java`)
- `layout/KeyboardDefinitions.java` — key-code table and row definitions, including
  the proven editing-action codes (53737–53742) and the modifier key-event strategy
  (from `Definitions.java` and `CodeBoardIME.java`)
- `layout/SymbolLayoutManager.java` — "each character is a key" symbol-row model
  (from the custom-symbol feature in `Definitions.java` / `KeyboardPreferences.java`)
- `theme/ThemeInfo.java`, `theme/UiTheme.java`, `theme/ThemeDefinitions.java`,
  `theme/ThemeManager.java` — theme pipeline (from `ThemeInfo.java`, `UiTheme.java`,
  `ThemeDefinitions.java`); all theme colors/palettes are original to Coding Keys
- `core/KeyboardPreferences.java` — settings-store pattern (from `KeyboardPreferences.java`)
- `core/ModifierKeyManager.java` — shift/ctrl meta-key strategy (from `CodeBoardIME.java`)
- `core/InputConnectionManager.java`, `core/KeyboardStateManager.java`,
  `core/KeyboardEventDispatcher.java` — split out of the monolithic IME service
  (from `CodeBoardIME.java`) and extended
- `editing/*.java` — editing strategy (context-menu actions with codes inherited
  from `CodeBoardIME.java`), extended with fallbacks and the bounded local undo
- `CodingKeysIME.java` — IME service orchestration (from `CodeBoardIME.java`)
- `app/src/test/.../KeyboardLayoutBuilderTest.java` — ported from the upstream builder tests

These files carry a header noting their derivation.

## What is original to Coding Keys

- The brand identity, launcher icon and all color palettes
- The 12-theme palette set and the custom-theme editor
- The page system (Abc / `#+=` / `</>` / Nav) and all page compositions
- The editing managers with bounded-read fallbacks and the local undo/redo
  operation stack (no upstream equivalent)
- The symbol-row editor UI, settings dashboard, theme preview system
- The navigation page (Home/End/PgUp/PgDn/forward-delete), Alt-key support
- All Gradle/CI configuration and documentation

## License

Coding Keys is licensed under the **GNU General Public License v3.0 or later**
(see [LICENSE](LICENSE)). You may redistribute and/or modify it under the terms
of the GPL. There is NO WARRANTY, to the extent permitted by law.

Under GPL-3.0 §5/§6, distribution of this derivative work must carry the license,
attributions, and provide corresponding source code. Keeping this file intact
satisfies the attribution requirement; the full source is provided in this repository.

## Reference

The reference source used for development was obtained from the official
repository at tag `5.0.3`
(`https://codeload.github.com/gazlaws-dev/codeboard/tar.gz/refs/tags/5.0.3`).
