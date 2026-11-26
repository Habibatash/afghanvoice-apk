@echo off
gradle --version | findstr /C:"8.14" >nul
if errorlevel 1 (
  echo This project requires Gradle 8.14.x and JDK 17.
  exit /b 1
)
gradle --no-daemon assembleDebug
if errorlevel 1 exit /b 1
echo APK: app\build\outputs\apk\debug\app-debug.apk
