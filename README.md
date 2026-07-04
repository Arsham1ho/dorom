# Dorom

A personal, single-user Android app for Arsham — plan each night for the next day, track long-term
goals, weekly plans, course progress, countdowns, gym workouts, guitar practice, finances, and a
locked personal journal. Everything is stored locally on-device (Room + private file storage) —
no backend, no accounts.

## Stack
Kotlin, Jetpack Compose (custom flat/no-gradient design system, `ui/theme`), Room, DataStore,
AlarmManager-based local notifications, BiometricPrompt + Keystore-backed AES/GCM for the locked
journal.

## Build & install
```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Requires: Android SDK (compileSdk 36, minSdk 26), JDK 17+.
