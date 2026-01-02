# Repository Guidelines

## Project Structure & Module Organization
- `app/` is the Android application module.
- Kotlin sources live in `app/src/main/java/com/example/yoyo_loto/`.
  - `core/` contains pure calculation logic (no Android UI).
  - `data/` contains persistence (DataStore).
  - `ui/` holds Compose UI and theming.
  - `viewmodel/` manages app state.
- UI assets and resources live in `app/src/main/res/` (e.g., `drawable/`, `font/`, `values/`).
- Unit tests: `app/src/test/java/`; instrumentation tests: `app/src/androidTest/java/`.
- `Exemple/` contains the legacy desktop reference app and is not part of the Android build.

## Build, Test, and Development Commands
Run from the repo root:
- `./gradlew :app:assembleDebug` builds the debug APK.
- `./gradlew :app:installDebug` installs to a connected device/emulator.
- `./gradlew :app:compileDebugKotlin` is a fast compile check.
- `./gradlew :app:testDebugUnitTest` runs unit tests (if present).

Note: Gradle requires JDK 11+ (`JAVA_HOME` set accordingly).

## Coding Style & Naming Conventions
- Kotlin + Jetpack Compose. Use 4-space indentation.
- Classes in `UpperCamelCase`, functions/vars in `lowerCamelCase`.
- Keep UI logic in `ui/`, state in `viewmodel/`, and pure logic in `core/`.
- Prefer small, focused composables; keep styling in `ui/theme/`.
- Use ASCII text in source files unless a non-ASCII character is required.

## Testing Guidelines
- Unit tests use JUnit (see `libs.versions.toml`).
- Place tests under `app/src/test/java/` mirroring package structure.
- Instrumentation/UI tests go in `app/src/androidTest/java/`.
- Name tests after the feature, e.g., `CalculTest`, `AutoGrilleTest`.

## Commit & Pull Request Guidelines
- No git history was detected in this repo; no existing convention to follow.
- Recommended: Conventional Commits (`feat:`, `fix:`, `chore:`) and short, scoped messages.
- For UI changes, include screenshots or emulator captures in PRs.
- Mention key commands run (e.g., `./gradlew :app:assembleDebug`).

## Configuration Notes
- `compileSdk`/`targetSdk` are set to API 36 in `app/build.gradle.kts`.
- The app label is `LotoYoYo` (`app/src/main/res/values/strings.xml`).
