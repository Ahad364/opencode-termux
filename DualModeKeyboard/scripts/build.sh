#!/data/data/com.termux/files/usr/bin/bash
# Build helper for Dual Mode Keyboard.
#
# 1. Validates that the project structure is complete.
# 2. If a compatible JDK is available, runs the requested Gradle tasks.
# 3. If no Android SDK is available (typical Termux setup), prints guidance to
#    use GitHub Actions, which is the supported build environment.
set -euo pipefail

cd "$(dirname "$0")/.."
PROJECT_DIR="$(pwd)"

REQUIRED_FILES=(
    "settings.gradle.kts"
    "build.gradle.kts"
    "gradle/libs.versions.toml"
    "gradle/wrapper/gradle-wrapper.jar"
    "gradle/wrapper/gradle-wrapper.properties"
    "gradlew"
    "app/build.gradle.kts"
    "app/src/main/AndroidManifest.xml"
    "app/src/main/res/xml/method.xml"
    "app/src/main/java/com/dualmode/keyboard/ime/KeyboardService.kt"
)

echo "==> Validating project structure"
MISSING=0
for f in "${REQUIRED_FILES[@]}"; do
    if [[ ! -f "$f" ]]; then
        echo "    MISSING: $f"
        MISSING=1
    fi
done
if [[ "$MISSING" -ne 0 ]]; then
    echo "Project structure is incomplete — aborting." >&2
    exit 1
fi
echo "    Project structure OK ($PROJECT_DIR)"

chmod +x gradlew 2>/dev/null || true

if ! command -v java >/dev/null 2>&1; then
    echo "==> No JDK found."
    echo "    Install one with:  pkg install openjdk-17   (or run scripts/termux-setup.sh)"
    echo "    The supported build environment is GitHub Actions — push this"
    echo "    project and open the Actions tab, or run 'gh workflow run'."
    exit 1
fi

echo "==> Java found: $(java -version 2>&1 | head -n 1)"

if [[ -z "${ANDROID_HOME:-}" && -z "${ANDROID_SDK_ROOT:-}" ]]; then
    echo "==> ANDROID_HOME / ANDROID_SDK_ROOT not set — no Android SDK detected."
    echo "    Local APK builds in Termux are not supported for this project."
    echo "    Use GitHub Actions instead:"
    echo "      git push            (triggers the build workflow)"
    echo "      gh workflow run android-build.yml   (manual trigger)"
    echo "    Then download the APK/AAB artifacts from the run page."
    exit 0
fi

echo "==> Running Gradle tasks: $*"
exec ./gradlew "$@"
