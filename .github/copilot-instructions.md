# Copilot instructions for Fuel-Consumption-Log

## Project context
- Kotlin Multiplatform app (Android + iOS + Desktop JVM) using Compose Multiplatform UI.
- Main business/domain/UI logic lives in `shared/src/commonMain`.
- Persistence uses SQLDelight (`FuelLogDatabase.sq`) with platform drivers in `androidMain`, `iosMain`, `jvmMain`.
- Dependency injection uses Koin (`shared/src/commonMain/.../di/Koin.kt`).

## Where to make changes
- Put shared features in `shared/src/commonMain/kotlin/com/lszczepanski/fuelconsumptionlog/...`.
- Keep platform-only code in platform source sets:
  - `shared/src/androidMain`
  - `shared/src/iosMain`
  - `shared/src/jvmMain`
- Android entrypoint: `androidApp/src/main/.../MainActivity.kt`.
- Desktop entrypoint: `desktopApp/src/main/.../main.kt`.
- iOS bridge: `shared/src/iosMain/.../MainViewController.kt` and `iosApp`.

## Existing architecture patterns
- Use `ViewModel` + `MutableStateFlow`/`StateFlow` for screen state.
- Keep UI state in immutable data classes (`CarsUiState`, `CarDetailsUiState`, `SettingsUiState`).
- Update state with `.update { it.copy(...) }`.
- Validate form input in domain validators (`CarDraftValidator`, `RefuelDraftValidator`) before repository calls.
- Keep repository interface-first (`CarRepository`, `SettingsRepository`), with SQLDelight implementations in `data/local`.

## Data and database rules
- Schema source of truth: `shared/src/commonMain/sqldelight/.../FuelLogDatabase.sq`.
- When changing schema, add/update SQLDelight queries and ensure repository mappings stay in sync.
- Keep domain models (`Car`, `RefuelEntry`, inputs/drafts) aligned with SQL columns and query order.
- Respect existing business rules:
  - Registration number is unique.
  - Only latest refuel entry is editable.
  - New refuel cannot be added until latest entry has liters filled.

## UI and product conventions
- Keep user-visible copy in Polish, consistent with current screens.
- Follow current Compose style: Material3, small composables, previews for new reusable UI pieces where reasonable.
- Preserve current navigation pattern in `App.kt` unless task explicitly asks to redesign it.

## Coding conventions for this repo
- Prefer explicit, simple Kotlin over clever abstractions.
- Reuse existing helpers (for numbers/date formatting and decimal input filtering) before adding new ones.
- Avoid broad `try/catch`; prefer `Result`-based error propagation pattern already used in repositories/viewmodels.
- Keep new dependencies in existing Gradle version catalog (`gradle/libs.versions.toml`).

## Validation and commands
- Build Android app: `./gradlew :androidApp:assembleDebug`
- Run desktop app: `./gradlew :desktopApp:run`
- Run desktop tests: `./gradlew :shared:jvmTest`
- Run Android host tests: `./gradlew :shared:testAndroidHostTest`
- Run iOS simulator tests: `./gradlew :shared:iosSimulatorArm64Test`

## Change discipline for Copilot
- Make surgical changes only in files related to the task.
- Do not rewrite unrelated modules or refactor broadly unless requested.
- When adding a feature, wire all affected layers: model/validator -> repository -> viewmodel -> UI.
- If behavior changes, update `README.md` only when user-facing usage or module behavior changes.
