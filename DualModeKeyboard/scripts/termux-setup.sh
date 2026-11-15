#!/data/data/com.termux/files/usr/bin/bash
# Termux setup for Dual Mode Keyboard source management.
# Installs the tools needed to manage this repository from Android/Termux:
# git, curl, zip/unzip and a JDK for running Gradle tasks locally where
# possible. Full Android SDK builds in Termux are NOT supported — the APK/AAB
# are built by GitHub Actions (see README.md, section "Building").
set -e

echo "==> Updating Termux packages"
pkg update -y

echo "==> Installing required packages"
yes | pkg install -y git curl zip unzip jq openjdk-17

echo "==> Java version"
java -version 2>&1 || true

echo "==> Git version"
git --version

echo
echo "Setup complete. Next steps:"
echo "  1. Configure git:  git config --global user.name \"Your Name\""
echo "                     git config --global user.email \"you@example.com\""
echo "  2. Clone your fork of this project."
echo "  3. Use GitHub Actions for real APK/AAB builds (see README.md)."
