# Translations and release-related files

## Translations

Mensinator is localized into many languages.

Preferred workflow:

- Contribute translations via Weblate:
  - https://toolate.othing.xyz/projects/mensinator/

Notes:

- Unqualified resources (`app/src/main/res/values/`) are treated as **English**.
  - This is declared in `app/src/main/res/resources.properties`.
- Android Lint is configured to *warn* (not error) on missing translations.
  - See `app/lint.xml`.

If you need to add a new user-visible string:

- Add the English string in `values/strings.xml`.
- Avoid editing many `values-xx/` folders manually unless necessary (Weblate reduces conflicts).

## Store listing metadata (Fastlane)

The folder `fastlane/metadata/android/` contains store listing text per locale for **F-Droid / IzzyOnDroid** (not Google Play):

- `title.txt`
- `short_description.txt`
- `full_description.txt`
- `changelogs/`

If you change user-facing behavior, consider updating the relevant locale’s changelog text.

## CI expectations

CI runs on push and pull requests:

- `./gradlew build`

It also uploads an Android Lint SARIF report for GitHub code scanning.
