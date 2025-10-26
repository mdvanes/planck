# Automated Tests Documentation

## Overview

Automated tests have been implemented for 2 main features of the Planck app:

1. **Song Model & Playback Logic** - Testing the core Song data model
2. **Offline Caching System** - Testing the smart caching functionality

## Test Files

### 1. SongModelTest.kt
**Location:** `app/src/test/java/nl/mdworld/planck4/SongModelTest.kt`

**Test Count:** 13 tests

**Coverage:**
- ✅ Song creation with all fields
- ✅ Song creation with null optional fields
- ✅ Radio stream song handling (special ID: "radio-stream")
- ✅ Song equality and comparison
- ✅ Song validation (empty strings, zero duration)
- ✅ Song filtering and list operations
- ✅ Song copy functionality
- ✅ Songs with partial data (artist without album, vice versa)
- ✅ Multiple songs in collections
- ✅ Song toString representation

**Purpose:**
Tests the Song data model which is fundamental to the entire playback system. Ensures songs are properly represented with all their metadata (title, artist, album, duration, cover art).

### 2. SongCacheManagerTest.kt
**Location:** `app/src/test/java/nl/mdworld/planck4/SongCacheManagerTest.kt`

**Test Count:** 15 tests

**Coverage:**
- ✅ Cache file existence checking (`isCached`)
- ✅ Cache file retrieval (`getCachedFile`)
- ✅ Cache size calculation (`sizeBytes`, `sizeBytesAsync`)
- ✅ Cache clearing (`clear`, `clearAsync`)
- ✅ Size formatting (bytes to MB display)
- ✅ Multiple songs cached simultaneously
- ✅ 500MB maximum cache size validation
- ✅ Edge cases handling

**Purpose:**
Tests the offline caching system which enables users to listen to music without internet connection. This is a critical feature for automotive use where connectivity may be unreliable.

## Test Dependencies

Added to `app/build.gradle.kts`:

```kotlin
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")
androidTestImplementation("io.mockk:mockk-android:1.13.8")
androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
```

## Running the Tests

### Run all unit tests:
```bash
./gradlew :app:testDebugUnitTest
```

### Run specific test classes:
```bash
./gradlew :app:testDebugUnitTest --tests "nl.mdworld.planck4.SongModelTest"
./gradlew :app:testDebugUnitTest --tests "nl.mdworld.planck4.SongCacheManagerTest"
```

### View test results:
Test reports are generated at:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

## Test Results

All tests are currently **PASSING** ✅

```
BUILD SUCCESSFUL
Total: 28 tests (13 Song Model + 15 Cache Manager)
```

## Future Test Expansion

Additional tests can be added for:

1. **Navigation System** - Screen transitions and state management
2. **Radio Metadata** - ICY metadata parsing and API integration
3. **Subsonic API** - Network requests and response parsing
4. **Settings Manager** - Preferences storage and retrieval
5. **Playlist Management** - Loading and caching playlists
6. **Network Monitor** - Connectivity detection
7. **Album/Artist Models** - Data validation
8. **Cover Art Loading** - Image caching and loading

## Test Strategy

**Unit Tests (Current):**
- Test individual components in isolation
- Use mocking for dependencies
- Fast execution, no Android device needed
- Focus on business logic and data models

**Integration Tests (Future):**
- Test component interactions
- Verify UI behavior
- Test actual API calls
- Require Android device/emulator

**Key Principles:**
- **Isolation** - Tests don't depend on external services
- **Repeatability** - Same results every run
- **Speed** - Fast feedback for developers
- **Coverage** - Focus on critical paths first
- **Maintainability** - Clear, readable test code

## Continuous Integration

These tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run Unit Tests
  run: ./gradlew :app:testDebugUnitTest

- name: Upload Test Results
  uses: actions/upload-artifact@v2
  with:
    name: test-results
    path: app/build/reports/tests/
```

## Notes

- Tests use MockK for mocking Android components
- Coroutine tests use `kotlinx-coroutines-test`
- Tests respect encapsulation (only test public APIs)
- Song cache tests verify the 500MB limit constraint
- Radio stream songs are tested with special ID "radio-stream"

