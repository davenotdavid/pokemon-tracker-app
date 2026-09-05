# Pokemon Tracker App

An Android app for browsing Pokemon and tracking which ones you've captured, built with Kotlin and Jetpack Compose.

It consumes the [pokemon-tracker-api](https://github.com/davenotdavid/pokemon-tracker-api) Spring Boot backend, pointed at its live demo deployment by default.

## Features

- Browse all 151 Pokemon with name, type, and HP
- Tap a Pokemon to view its details
- Mark a Pokemon as captured/not captured, synced to the API

## Tech stack

- Kotlin + Jetpack Compose
- MVVM (`ViewModel` + `StateFlow`)
- Retrofit + OkHttp + Gson for networking
- Navigation Compose

## Running the app

Open the project in Android Studio and run the `app` configuration on an emulator or device.

By default the app points at the API's live demo at `http://54.209.33.157/`. To run against a local instance of the API instead, update `BASE_URL` in `RetrofitInstance.kt` and add the corresponding host to `network_security_config.xml` (cleartext HTTP is only permitted for hosts listed there).