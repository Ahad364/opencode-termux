# Termux Workflow Guide — Coding Keys

All development for Coding Keys can be done from Termux. Compilation happens on
GitHub Actions (Termux has no Android SDK capable of running `aapt2`/`d8`), so
your job in Termux is: edit, commit, push, download the APK.

## 1. One-time setup

```bash
pkg update -y && pkg upgrade -y
pkg install -y git gh curl unzip
gh auth login          # choose GitHub.com, HTTPS, browser auth
```

## 2. Get the project

```bash
# clone the repository containing CodingKeys/
git clone https://github.com/Ahad364/opencode-termux.git
cd opencode-termux
# project lives at ./CodingKeys
```

## 3. Daily workflow

```bash
cd ~/opencode-termux/CodingKeys

# edit files (vi, nano, or the editor of your choice)
$EDITOR app/src/main/java/com/codingkeys/keyboard/CodingKeysIME.java

git add -A
git commit -m "IME: describe your change"
git push              # this triggers the GitHub Actions build
```

## 4. Watch the build and download the APK

```bash
# list recent runs
gh run list --workflow coding-keys-build.yml --limit 3

# watch the newest run live
gh run watch

# when it is green, download the APK artifact
gh run download -n CodingKeys-debug-apk -D ~/downloads
ls ~/downloads          # -> app-debug.apk

# install to a connected device (optional)
adb install -r ~/downloads/app-debug.apk
```

If the build fails, fetch the logs:

```bash
gh run view --log-failed | less
```

Fix, commit, push - the push triggers a fresh build automatically.

## 5. Manual build (no code change needed)

```bash
gh workflow run coding-keys-build.yml
```

## 6. Reference: full command chain from scratch

```bash
pkg install -y git gh curl unzip
gh auth login
git clone https://github.com/Ahad364/opencode-termux.git
cd opencode-termux/CodingKeys
git add -A && git commit -m "start" && git push
gh run watch
gh run download -n CodingKeys-debug-apk -D ~/downloads
```

## Notes

- You never need `pkg install openjdk` or `gradle`: the wrapper (Gradle 8.9)
  and JDK 17 are provisioned by Actions. Local `./gradlew` builds cannot run
  in Termux because the Android build tools are x86_64 binaries.
- `gradlew` must stay executable: `chmod +x gradlew` if git reports a mode change.
- The workflow file lives at `.github/workflows/coding-keys-build.yml` (repo
  root, paths-filtered to `CodingKeys/**`). `CodingKeys/.github/workflows/android.yml`
  is a copy for standalone-repo use.
