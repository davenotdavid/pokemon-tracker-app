# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew test                    # run all unit tests (lifecycle task, aggregates testDebugUnitTest)
./gradlew testDebugUnitTest --tests "com.davenotdavid.pokemontrackerapp.ui.PokemonViewModelTest"
./gradlew testDebugUnitTest --tests "*.PokemonRepositoryTest.setCaptured rethrows cancellation*"
./gradlew assembleDebug            # build the debug APK
./gradlew installDebug             # build + install on a connected device/emulator
./gradlew lintDebug                # Android Lint
```

`--tests` only works against the concrete `testDebugUnitTest` task, not the `test` lifecycle
alias (AGP registers `--tests` filtering on the real `Test` task type, and `test` is just an
aggregating no-op that depends on it).

CI (`.github/workflows/ci.yml`) runs exactly `./gradlew test` then `./gradlew assembleDebug` on
every push to `main` and on pull requests — that pair is the fast way to sanity-check a change
locally before pushing.

To run on a physical device instead of the emulator: install via `adb`, then
`adb shell am start -n com.davenotdavid.pokemontrackerapp/.MainActivity`. `adb logcat --pid=$(adb
shell pidof com.davenotdavid.pokemontrackerapp)` is the fastest way to isolate this app's log
output (Timber-tagged, e.g. `PokemonViewModel$loadPokemon`) from the rest of the system log.

## Architecture

**Data flow is one-directional and network-first-with-cache-fallback, not reactive.** Compose
screens (`ui/PokemonListScreen.kt`, `ui/PokemonDetailScreen.kt`) never mutate state — they only
call functions on `PokemonViewModel` (`loadPokemon()`, `toggleCaptured()`) and render whatever
comes back on its `StateFlow<PokemonUiState>` (sealed: `Loading` / `Error` / `Success`, all
immutable). `PokemonViewModel` calls plain suspend functions on `PokemonRepository`, which are
themselves imperative try/catch, not Room `Flow` observation:

- `refreshFromNetwork()` hits the API and writes the result through to Room; throws on failure.
- `getCached()` reads whatever's currently in Room.
- `setCaptured()` tries the network `PUT`; if that throws, it just applies the toggle locally
  and writes that to Room instead — it never rethrows except for `CancellationException`.

`PokemonViewModel.loadPokemon()` tries `refreshFromNetwork()` first and only falls back to
`getCached()` in the catch block (showing `PokemonUiState.Error` only if the cache is *also*
empty). This was a deliberate simplification from an earlier `combine()`-of-three-flows design —
if you're tempted to make the repository reactive (Room `Flow` as source of truth), that's a
bigger architectural change than it looks like; ask before doing it.

**`CancellationException` handling is a real invariant, not boilerplate.** Both
`PokemonRepository.setCaptured()` and `PokemonViewModel.loadPokemon()` explicitly re-throw
`CancellationException` before falling into their broad `catch (ex: Exception)` blocks. Any new
catch-and-fall-back code added to either class needs the same guard, or coroutine cancellation
(e.g. the ViewModel being cleared mid-request) gets silently misread as a network failure.

**One `Pokemon` model, not separate DTO/entity/domain types.** `data/Pokemon.kt` is
simultaneously the Gson network model, the Room `@Entity` (`@PrimaryKey val id`), and the UI
model — mirroring the sibling `pokemon-tracker-api` Spring Boot repo, whose own `Pokemon.kt` is
likewise annotated for both JPA and Jackson. `type: List<String>` requires the Room
`TypeConverter` in `data/local/Converters.kt` (comma-joined string) since Room can't persist
lists directly.

**Hilt is the only DI mechanism** — no manual factories anywhere. `di/NetworkModule.kt` provides
the `OkHttpClient`/`Retrofit`/`PokemonService` singletons; `di/DatabaseModule.kt` provides the
Room database/DAO singletons. `PokemonRepository` and `PokemonViewModel` (`@HiltViewModel`) use
`@Inject constructor` and are otherwise plain classes — that's what lets both be unit-tested by
constructing them directly with MockK fakes (`PokemonRepositoryTest`, `PokemonViewModelTest`),
bypassing Hilt entirely in tests.

**Logging is gated by `BuildConfig.DEBUG`, in two places that need to stay in sync:**
`PokemonTrackerApplication` only plants `Timber.DebugTree()` in debug, and
`NetworkModule.provideOkHttpClient()` only sets `HttpLoggingInterceptor.Level.BODY` in debug
(`Level.NONE` otherwise). Release builds are silent by design — there's no crash-reporting
backend wired up for unplanted `Timber` calls to feed.

**Testing ViewModels needs `Dispatchers.setMain`.** `PokemonViewModel` runs its coroutines on
`viewModelScope` (`Dispatchers.Main.immediate`), so any test needs `Dispatchers.setMain
(UnconfinedTestDispatcher())` in `@Before` / `Dispatchers.resetMain()` in `@After` — see
`PokemonViewModelTest` for the pattern. Without it, `init { loadPokemon() }` throws because
there's no Main dispatcher on the JVM test runner.

**Networking target and cleartext HTTP.** `BASE_URL` in `di/NetworkModule.kt` points at a live
AWS EC2 demo of the sibling `pokemon-tracker-api` repo (`http://54.209.33.157/`, plain HTTP, no
auth). Android blocks cleartext traffic by default (API 28+), so switching `BASE_URL` to any
other host also requires adding that host to
`app/src/main/res/xml/network_security_config.xml`. For local API development against a
physical device (not the emulator, where `10.0.2.2` works out of the box), the reliable path is
`adb reverse tcp:8080 tcp:8080` plus `BASE_URL = "http://127.0.0.1:8080/"` — this tunnels over
USB and sidesteps Wi-Fi client-isolation issues that block phone-to-Mac LAN traffic on some
routers.

**A required, slightly unusual Gradle flag:** `gradle.properties` sets
`android.disallowKotlinSourceSets=false`. AGP 9.3.2's built-in-Kotlin support otherwise rejects
how KSP (used for both Hilt's and Room's annotation processing) registers generated sources —
don't remove this flag without confirming a KSP/AGP version bump has actually fixed the
underlying incompatibility.

## Consuming a separate API repo

This app is a pure client for [pokemon-tracker-api](https://github.com/davenotdavid/pokemon-tracker-api)
(Kotlin/Spring Boot, separate repo) — `GET/PUT /pokemon`, model `{ id, name, type: List<String>,
hp, isCaptured }`. There is no server-side code in this repository; API behavior changes (new
fields, validation, error formats) come from that repo, not this one.
