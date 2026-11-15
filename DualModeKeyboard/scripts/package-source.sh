#!/data/data/com.termux/files/usr/bin/bash
# Packages the complete Dual Mode Keyboard source into DualModeKeyboard-Source.zip
# for delivery (upload as an artifact or attach anywhere). Excludes generated
# files, secrets and VCS data.
set -euo pipefail

cd "$(dirname "$0")/.."

ZIP_NAME="DualModeKeyboard-Source.zip"

echo "==> Validating project structure"
REQUIRED_PATHS=(
    "settings.gradle.kts"
    "build.gradle.kts"
    "gradle.properties"
    "gradle/libs.versions.toml"
    "gradle/wrapper/gradle-wrapper.jar"
    "gradle/wrapper/gradle-wrapper.properties"
    "gradlew"
    "gradlew.bat"
    "app/build.gradle.kts"
    "app/proguard-rules.pro"
    "app/src/main/AndroidManifest.xml"
    "app/src/main/res/xml/method.xml"
    "app/src/main/res/values/strings.xml"
    "app/src/main/java/com/dualmode/keyboard/MainActivity.kt"
    "app/src/main/java/com/dualmode/keyboard/ime/KeyboardService.kt"
    ".github/workflows/android-build.yml"
    "scripts/termux-setup.sh"
    "scripts/build.sh"
    "README.md"
    "PRIVACY.md"
)
for f in "${REQUIRED_PATHS[@]}"; do
    if [[ ! -e "$f" ]]; then
        echo "ERROR: required file missing: $f" >&2
        exit 1
    fi
done
echo "    All required files present."

echo "==> Creating $ZIP_NAME"
rm -f "$ZIP_NAME"
zip -r -q "$ZIP_NAME" . \
    -x "*.git/*" \
    -x ".git/*" \
    -x "*build/*" \
    -x "*.gradle/*" \
    -x "local.properties" \
    -x "*.iml" \
    -x "*.idea*" \
    -x "*.DS_Store" \
    -x "*DualModeKeyboard-Source.zip" \
    -x "*keystore*" \
    -x "*.jks" \
    -x "*.keystore"

echo "==> Verifying archive integrity"
if command -v unzip >/dev/null 2>&1; then
    unzip -t "$ZIP_NAME" > /dev/null
    echo "    Archive verified OK ($(unzip -l "$ZIP_NAME" | tail -n 1 | awk '{print $2}') files)"
fi

SIZE="$(du -h "$ZIP_NAME" | cut -f1)"
echo "==> Done."
echo "    Location: $(pwd)/$ZIP_NAME"
echo "    Size:     $SIZE"
