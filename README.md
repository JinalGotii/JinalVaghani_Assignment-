# Gallery App

A native Android gallery application built with Kotlin, Jetpack Compose, and Clean Architecture. Browse device images with a fast grid, full-screen zoom viewer, and multi-select share/delete.

## Architecture

The app follows **MVVM + Clean Architecture** with three layers:

- **Domain** — `MediaItem` model, repository interface, and use cases (`GetImages`, `DeleteImages`, grid column preferences).
- **Data** — `MediaStoreDataSource` queries images via scoped storage; `MediaRepositoryImpl` coordinates data and preferences.
- **Presentation** — Compose screens with Hilt-injected ViewModels, `StateFlow` for UI state, and `SharedFlow` for one-shot events.

**Dependency injection** is handled by Hilt (`DataModule`, `AppModule`).



## Features

- Runtime permissions (`READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`) with partial-access handling (API 34+)
- Image grid with 2/3 column toggle (persisted via DataStore)
- Pull-to-refresh and empty state
- Full-screen `HorizontalPager` with pinch-to-zoom, double-tap zoom, and pan
- Multi-selection (long-press) with batch share and delete
- System delete confirmation on Android 11+ via `MediaStore.createDeleteRequest`
- Light / Dark theme (Material 3, primary blue `#1a73e8`)
- Edge-to-edge layout

## Build & Run

**Requirements:** Android Studio Ladybug or newer, JDK 11+, Android SDK 36.

## Project Structure

```
app/src/main/java/com/pexodrive/galleryapp/
├── di/                    # Hilt modules
├── data/                  # Repository + MediaStore data source
├── domain/                # Models, use cases, repository interface
├── presentation/          # Screens, navigation, common UI
├── ui/theme/              # Material 3 theme
└── utils/                 # Permissions, sharing, delete helpers
```

## Tech Stack

| Area | Library |
|------|---------|
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Images | Coil |
| Navigation | Navigation Compose |
| Preferences | DataStore |
| Async | Coroutines, Flow |

## SDK

- `minSdk` 24 (Android 7.0)
- `targetSdk` 36 (Android 15)
- Scoped storage compliant — no `requestLegacyExternalStorage`
