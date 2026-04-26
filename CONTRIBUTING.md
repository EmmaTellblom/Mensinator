# Contributing to Mensinator

Thanks for considering contributing to Mensinator! This project is an Android app focused on privacy-first period tracking.

## Ways to contribute

- **Bug reports / feature requests**: Open an issue on GitHub. For bigger changes, please discuss first.
- **Translations**: Prefer contributing via Weblate: https://toolate.othing.xyz/projects/mensinator/
- **Code changes**: Fix bugs, improve UX, add tests, improve performance, etc.
- **Store listing**: Help improve Fastlane metadata under `fastlane/metadata/` (descriptions, changelogs).

You can also join the community on Discord: https://discord.gg/tHA2k3bFRN

## Before you start

- Check existing issues/PRs to avoid duplicate work.
- For **major changes** (new features, behavior changes, refactors), please **open an issue first** and describe:
  - the problem you’re solving
  - the proposed approach
  - any UI/UX impact

## Development setup

### Requirements

- **Android Studio** (recommended)
- **JDK 17**
- Android SDK installed via Android Studio

### Build from the command line

From the repository root:

- Build (what CI runs): `./gradlew build`
- Run unit tests: `./gradlew testDebugUnitTest`
- Run lint: `./gradlew lintDebug`

Instrumentation tests require an emulator/device:

- `./gradlew connectedDebugAndroidTest`

## Making changes

### Code style

- Kotlin style is set to **official**.
- Keep PRs focused: avoid drive-by reformatting of unrelated files.
- Follow existing patterns in the codebase (Compose/UI, state handling, etc.).

### Translations / strings

- Default (unqualified) resources are treated as **English** (see `app/src/main/res/resources.properties`).
- This repo contains many `values-xx/` folders. To reduce churn and merge conflicts, **translation contributions should go through Weblate** when possible.
- Android lint is configured to **warn** on missing translations (see `app/lint.xml`). It’s OK to add new English strings without immediately translating every locale, but translations are appreciated.

### UI changes

If you change UI/behavior, include:

- a short description of the UX change
- screenshots or screen recordings when it helps reviewers

## Submitting a pull request

1. Fork the repo and create a branch from `main`.
2. Make your changes.
3. Verify locally:
   - `./gradlew build`
   - (optional but recommended) `./gradlew testDebugUnitTest`
   - (optional but recommended) `./gradlew lintDebug`
4. Open a PR targeting `main`.

PR tips:

- Link the relevant issue (or explain the context).
- Describe what changed and why.
- Mention any follow-ups or known limitations.

## Reporting bugs

Please include as much as you can:

- app version (and install source, e.g. F-Droid/Play/GitHub)
- Android version and device model
- steps to reproduce
- expected vs actual behavior
- screenshots/videos if relevant

## License

By contributing, you agree that your contributions will be licensed under the project’s MIT License (see `LICENSE`).
