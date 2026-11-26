#!/usr/bin/env bash
set -euo pipefail
if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle 8.14.8 او JDK 17 نصب کړئ؛ بیا دا فایل وچلوئ." >&2
  exit 1
fi
gradle --version | grep -q '8.14' || { echo "دا پروژه Gradle 8.14.x ته اړتیا لري." >&2; exit 1; }
gradle --no-daemon assembleDebug
echo "APK: app/build/outputs/apk/debug/app-debug.apk"
