# Dual Mode Keyboard R8 rules.
# The project uses only AndroidX libraries, which ship their own consumer rules.
# Keep the IME service entry point safe from aggressive stripping.
-keep class com.dualmode.keyboard.ime.KeyboardService { *; }
-keep class com.dualmode.keyboard.KeyboardApplication { *; }
