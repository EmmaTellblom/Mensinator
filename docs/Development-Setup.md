# Development setup

This repo is a single-module Android app (`:app`) built with Gradle and Kotlin.

## Requirements

- Android Studio (recommended)
- JDK 17 (CI uses Temurin 17)
- Android SDK + build tools installed via Android Studio

## Build / test / lint

From the repository root:

- Build (same as CI): `./gradlew build`
- Unit tests: `./gradlew testDebugUnitTest`
- Android Lint: `./gradlew lintDebug`

Instrumentation tests require an emulator or a device:

- `./gradlew connectedDebugAndroidTest`

## Common troubleshooting

- If Gradle fails with Java version errors, verify Android Studio is configured for **JDK 17**.
- If you see resource/locale warnings, see the translations notes in `docs/03-translations-and-release.md`.
