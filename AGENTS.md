# AGENTS.md - qtest (Exam Grader & Tracker)

## Project structure

- Single-module Android app (`:app`) — Kotlin, Jetpack Compose, Room, Moshi, Retrofit
- AGP 9.1.1, Kotlin 2.2.10, Gradle 9.3.1 (CI), compileSdk 36, minSdk 24
- `metadata.json` name is **"Exam Grader & Tracker"**; app_name string resource matches

## Developer commands

| Command | Description |
|---------|-------------|
| `gradle test` | Run all local unit/Robolectric tests (preferred over building APK) |
| `gradle test --tests "...ClassName.methodName"` | Single test class/method (standard Gradle filter) |
| `gradle connectedAndroidTest` | Run instrumented tests (needs device/emulator) |

- **No local APK builds** — compilation/debug APK is done only via GitHub Actions CI (`gradle assembleDebug` on push/PR).
- No lint or typecheck tasks are configured.

## Setup quirks

- **`.env` required** at project root with `GEMINI_API_KEY` (secrets-gradle-plugin reads it). `.env` is gitignored.
- **Debug signing**: `app/build.gradle.kts` (lines 41-49) conditionally uses `debug.keystore` if present; no manual removal of signing config is required for local runs.
- **CI** uses JDK 21 (Temurin), decodes `debug.keystore` from base64 if present.
- **Release signing**: env vars `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`.
- Gradle config caching and parallel execution enabled; configuration cache is on.

## Testing

- **Robolectric tests** — in `src/test/` (need `@Config(sdk = [36])`). Requires `testOptions { unitTests { isIncludeAndroidResources = true } }` (already set).
- **Roborazzi** — Compose screenshot tests in `src/test/` using `RobolectricDeviceQualifiers.Pixel8`. Captures go to `src/test/screenshots/`.
- **Instrumented tests** — in `src/androidTest/`, run on device/emulator via `connectedAndroidTest`.
- Tests use JUnit 4 (no JUnit 5).

## Codegen & patterns

- **KSP** drives Room and Moshi code generation — `ksp()` in dependencies for both `room-compiler` and `moshi-kotlin-codegen`.
- Room database `"exam_practice_database"` (version 2) uses `fallbackToDestructiveMigration()` — data loss on schema change.
- Moshi serializer (`JsonSerializer.kt`) serializes `Map<Int, Int>` as JSON with string keys. Moshi uses both `@JsonClass(generateAdapter = true)` on entities and `KotlinJsonAdapterFactory()`.
- Negative marking formula in `ExamViewModel.kt`: `correct * 1.0 + incorrect * -0.33`.
- Navigation is manual (no Navigation Compose) — `Screen` sealed class + `when` in `MainActivity`.

## Auto-save & draft system

- `ExamAttempt` entity has `isDraft: Boolean = false` field (added in DB version 2).
- **Auto-save**: Answers are persisted to Room after every question change (debounced 500ms via `snapshotFlow` on `activeAnswers`).
- **Draft resume**: `startExam()` checks for an existing draft via `repository.getDraftAttempt(examId)` and restores answers + timer position.
- **Save & Exit**: `saveAndExit()` in `ExamViewModel` saves a draft and navigates to `ExamDetail`. Available as an icon button in `TakeExamScreen` TopAppBar and as the cancel dialog's confirm action.
- **Start fresh**: `startFreshExam()` deletes any existing draft before starting a new attempt.
- **Dashboard**: `ExamCard` shows a "Draft" badge and "Continue" button when `exam.id in draftExamIds`.
- **ExamDetail**: Shows "Continue Practice" (primary) + "Start New Attempt" (outlined) when a draft exists.
- **Draft cleanup**: Draft is deleted when exam is submitted via `submitExamAnswers()`.
- `getAttemptsForExam()` excludes drafts (`WHERE isDraft = 0`); `getDraftExamIds()` returns exam IDs with active drafts.

## UI Design System

**All UI implementation rules, component patterns, and screen-specific guidelines live in `docs/DESIGN.md`.**

When implementing or modifying any UI:
1. Read `docs/DESIGN.md` first
2. Use components from `com.example.ui.components` package (built per DESIGN.md spec)
3. Follow the icon whitelist, spacing tokens, and animation rules documented there

Key points from DESIGN.md (summary only — read full file for details):
- **Icons**: Only `material-icons-core` is enabled. Use `Icons.Default.*` only. Extended icons are commented out.
- **Navigation**: Manual `Screen` sealed class. No Navigation Compose.
- **State**: All screen state lives in `ExamViewModel`.
- **Back handling**: Use `BackHandler` from `androidx.activity.compose` to intercept system back.

## Gradle build

- Version catalog at `gradle/libs.versions.toml` — add new deps there, not inline.
- Many dependencies commented out in `app/build.gradle.kts` (CameraX, Navigation Compose, Coil, Firebase AI, etc.) — unused but preserved for easy re-enable.

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tools** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them. `codegraph_node` returns one symbol's source + callers, or reads a whole file with line numbers. If the tools are listed but deferred, load them by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` and `codegraph node <symbol-or-file>` print the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
