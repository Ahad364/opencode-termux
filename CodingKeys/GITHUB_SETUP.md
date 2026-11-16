# GitHub Setup Guide — Coding Keys

## Option A — inside the current repository (default)

The project is committed to `Ahad364/opencode-termux` under `CodingKeys/`.
The root workflow `.github/workflows/coding-keys-build.yml` triggers on any
push touching `CodingKeys/**` and uploads APK artifacts. Nothing else to do.

## Option B — move Coding Keys to its own repository

The project is self-contained and already includes
`CodingKeys/.github/workflows/android.yml`.

```bash
cd ~/opencode-termux
git subtree split -P CodingKeys -b coding-keys-standalone

# create an empty private repo (or use --public)
gh repo create coding-keys --private --source coding-keys-standalone --push
```

The standalone workflow builds on every push/PR and on manual trigger.

## Required repository settings

- **Actions**: enabled (default). The workflow needs no extra permissions.
- **Secrets (optional, release signing only):**
  Debug builds never need secrets.

  | Secret | Meaning |
  |---|---|
  | `KEYSTORE_BASE64` | base64 of a `.jks` keystore (`base64 -w0 my.jks`) |
  | `KEYSTORE_PASSWORD` | keystore password |
  | `KEYSTORE_ALIAS` | key alias |
  | `KEY_PASSWORD` | key password |

  Also create the **repository variable** `SIGNING_ENABLED=true` so the
  workflow activates signing. Without these, only the debug APK is produced -
  which is the documented default path.

## Where the APK appears

1. Repository → **Actions** → latest **Coding Keys Build** run
2. Scroll to **Artifacts**
3. `CodingKeys-debug-apk` → unzip → `app-debug.apk`
4. (With signing configured) `CodingKeys-release-apk` → `app-release.apk`

Artifacts keep for 90 days by default.

## Build command (what CI runs)

```bash
./gradlew clean testDebugUnitTest assembleDebug   # tests + debug APK
./gradlew lint                                    # lint report
./gradlew assembleRelease                         # release APK (signed only if configured)
```

Toolchain: ubuntu-latest runner, JDK 17 (Temurin), Gradle 8.9 wrapper, AGP 8.7.3,
compileSdk 35, minSdk 26, targetSdk 35.
