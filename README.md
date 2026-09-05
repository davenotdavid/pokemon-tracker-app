# Pokemon Tracker App

[![CI](https://github.com/davenotdavid/pokemon-tracker-app/actions/workflows/ci.yml/badge.svg)](https://github.com/davenotdavid/pokemon-tracker-app/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An Android app for browsing Pokemon and tracking which ones you've captured, built with Kotlin and Jetpack Compose.

It consumes the [pokemon-tracker-api](https://github.com/davenotdavid/pokemon-tracker-api) Spring Boot backend, pointed at its live demo deployment by default.

## Features

- Browse all 151 Pokemon with name, type, and HP
- Tap a Pokemon to view its details
- Mark a Pokemon as captured/not captured, synced to the API
- Works offline: the last-synced data is cached locally and shown (with an "offline" indicator) if the API can't be reached

## Tech stack

- Kotlin + Jetpack Compose
- MVVM (`ViewModel` + `StateFlow`)
- Hilt for dependency injection
- Retrofit + OkHttp + Gson for networking
- Room DB for local caching/offline support
- Navigation Compose
- JUnit + MockK for unit tests

## Running the app

Open the project in Android Studio and run the `app` configuration on an emulator or device.

By default the app points at the API's live demo at `http://54.209.33.157/`. To run against a local instance of the API instead, update `BASE_URL` in `di/NetworkModule.kt` and add the corresponding host to `network_security_config.xml` (cleartext HTTP is only permitted for hosts listed there).

## Running tests

```
./gradlew test
```

Unit tests cover `PokemonRepository` (network/cache write-through, offline fallback) and `PokemonViewModel` (UI state transitions), using MockK to fake the network and database layers.

## CI

GitHub Actions (`.github/workflows/ci.yml`) runs the unit test suite and builds the debug APK on every push to `main` and on pull requests.

## License

[MIT](LICENSE)