#!/usr/bin/env bash
# Package the Coding Keys source (excluding build artifacts) into a ZIP.
# Output: CodingKeys-Source.zip in the repo root of CodingKeys.
set -euo pipefail

cd "$(dirname "$0")/.."

OUT="CodingKeys-Source.zip"
rm -f "$OUT"

zip -r "$OUT" \
    app gradle .github \
    build.gradle settings.gradle gradle.properties gradlew gradlew.bat \
    README.md INSTALL.md TERMUX.md GITHUB_SETUP.md \
    TESTING.md PRIVACY.md ATTRIBUTION.md LICENSE scripts \
    -x "app/build/*" -x ".gradle/*" -x "*.iml"

echo "Packaged -> $OUT"
