# Project structure

Quick map of the repository:

- `app/` — Android application module
  - `src/main/AndroidManifest.xml`
  - `src/main/java/` — Kotlin/Java sources (package `com.mensinator`)
  - `src/main/res/` — resources (Compose, drawables, strings, etc.)
  - `lint.xml` — lint configuration (e.g., translation severity)
  - `proguard-rules.pro` — release Proguard/R8 rules (minification currently off)
- `.github/workflows/push.yml` — CI pipeline (runs `./gradlew build` and uploads lint SARIF)
- `gradle/libs.versions.toml` — dependency + plugin version catalog
- `fastlane/metadata/` — store listing metadata for multiple locales

## Build configuration notes

- Android config lives in `app/build.gradle.kts`.
- `compileSdk` / `targetSdk` are set to 36, `minSdk` is 26.
- Kotlin/Java target is **17**.

## Where to start

If you’re new to the codebase:

1. Open the project in Android Studio.
2. Run the `app` configuration.
3. Use `./gradlew build` to confirm everything works from CLI.
