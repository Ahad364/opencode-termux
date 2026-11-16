# Coding Keys ProGuard rules.
# Minification is disabled by default (minifyEnabled false); these rules keep
# the IME service safe if release minification is ever enabled.

-keep class com.codingkeys.keyboard.CodingKeysIME { *; }
-keep class com.codingkeys.keyboard.layout.** { *; }
-keep class com.codingkeys.keyboard.theme.** { *; }

# AndroidX
-keep class androidx.appcompat.** { *; }
