# Repository Guidelines

## Project Structure & Module Organization
- `pom.xml` configures a single Maven module targeting Java 24 with FlatLaf and MigLayout for the Swing UI. Update dependencies here.
- `src/main/java/org/example/` hosts application classes; keep UI panels and helpers grouped by role (`GridPanel`, `Theme`, `Calcul`) and co-locate assets such as `img.png`.
- `src/main/resources/` is reserved for future configuration bundles; add localization files or icons that must stay on the classpath.
- `target/` contains generated jars, the shaded `LotoYoYo.jar`, and Windows installers from `jpackage`; do not commit its contents.

## Build, Test, and Development Commands
- `mvn clean package` compiles sources, runs tests, creates the shaded jar, and invokes `jpackage` to emit an installer in `target/`.
- `mvn exec:java` launches the Swing app using the configured `org.example.Main` entry point; use this for rapid UI checks.
- Use `mvn clean verify -DskipTests` only for UI-only tweaks and note the manual verification in the PR.

## Coding Style & Naming Conventions
- Format Java with 4-space indentation, `UpperCamelCase` classes, and `lowerCamelCase` members/methods. Follow the existing spacing around control blocks and lambda expressions.
- Centralize colors, fonts, and strings in `Theme` or small helpers so panels stay lean and focused.
- Keep package name `org.example` unless the artifact coordinates change; align new files accordingly.

## Testing Guidelines
- Add unit tests beneath `src/test/java/`, mirroring package structure (`org/example`). Prefer JUnit 5 (`org.junit.jupiter`) and assert Swing-independent logic such as `Calcul` helpers.
- Name tests after the feature under test (`CalculTest`, `ThemeDarkModeTest`) and annotate methods with `@Test`.
- Run `mvn test` before committing; when Swing flows stay manual, record the steps in the PR.

## Commit & Pull Request Guidelines
- There is no commit history yet; adopt Conventional Commit prefixes (`feat:`, `fix:`, `chore:`) to make changelogs predictable.
- Reference related issues in the description, list the commands you executed (`mvn package`, `mvn exec:java`), and attach screenshots or gifs for any UI-facing change.
- Keep PRs focused on one change and spin off follow-up tasks separately.

## Packaging & Assets
- The shade plugin embeds dependencies and sets `org.example.Main`; confirm `img.png` and new assets survive packaging.
- The `jpackage` goal emits `target/LotoYoYo-<version>.exe`; smoke-test on Windows before tagging and document extra JVM flags.
