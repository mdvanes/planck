[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/mdvanes/planck/production.yml)](https://github.com/mdvanes/planck/actions/workflows/production.yml)
![GitHub Release](https://img.shields.io/github/v/release/mdvanes/planck)

# Planck


Android Automotive (AAOS) client for Radio Streams and Subsonic

Planck brings your complete music library to your car's dashboard with an interface designed specifically for Android Automotive OS.

### 🎵 Stream Your Music Library
- **Subsonic/Navidrome Support** - Connect to your personal music server
- **Browse by Playlists, Artists, or Albums** - Multiple ways to explore your collection
- **Two Browsing Modes** - Tag-based (metadata) or folder-based navigation

### 📻 Internet Radio
- **Live Radio Streaming** - Listen to your favorite stations
- **Real-Time Metadata** - See current song titles, artists, and album art
- **Track History** - View previously played songs with timestamps
- **Smart Skip Feature** - Temporarily skip commercials by resuming your last song until the next track starts

### 📱 Offline Playback
- **Smart Caching** - Automatically downloads upcoming songs (500MB cache)
- **Offline Mode** - Keep listening when you lose connection
- **Album Art Cache** - Beautiful cover art available offline (100MB cache)

### 🎛️ Powerful Features
- **Media Button Support** - Control with steering wheel buttons
- **Persistent Notifications** - Show playing song in notification
- **Multi-Disc Album Support** - Flattens multi-disc albums for seamless playback
- **Customizable Overlay** - Adjust background darkness for readability

### 🔧 Flexible Configuration
- **Custom Server URLs** - Connect to any Subsonic-compatible server
- **Radio Station Management** - Load stations from your server or use custom URLs
- **Cache Control** - Manage storage usage for songs and artwork
- **Two Library Modes** - Choose between metadata tags or folder structure browsing

Perfect for long drives, commutes, and road trips. Planck transforms your Android Automotive system into a powerful music streaming platform with both online and offline capabilities.

**Requirements:**
- Android Automotive OS
- Subsonic, Navidrome, Airsonic, or compatible music server
- Internet connection (for streaming; offline playback available for cached content)

---

## Local Development in Android Studio

In the "Device Manager", create a new virtual device with the following settings:

- Automotive (1080p landscape) API 33
- Click "Run app" green triangle on top right op IDE. This will build and start the VM
- Keep an eye on the "Build Output" tab at the bottom of the IDE for any errors
- After a change, click "Sync Project with Gradle Files" (elephant icon) to ensure everything is up to date and "Run app" again

## Running tests

```bash
./gradlew :app:testDebugUnitTest
```

## Debug on a Android phone

- Go to Build > Generate App Bundle(s) / APK(s) > Generate APK
- The bundle is in app/build/outputs/bundle/debug/app-debug.apk
- Install the bundle on your phone

## Publishing to Google Play Store

- in build.gradle.kts, bump the versionCode and versionName
- Go to Build > Generate Signed App Bundle(s) / APK(s) > Generate Bundles
- Go to https://play.google.com/console/
- Create a new Closed test release for the Android Automotive track. Internal tests releases will *not* be shown in the car.

## Automated Releases

A GitHub Actions workflow automatically creates a release when `versionName` in `app/build.gradle.kts` changes on pushes to `main`.

Details:
- Uses the `versionName` value as both the tag and release title.
- Skips creation if a release with the same tag already exists.
- Collects commit messages since the previous release as release notes.

To trigger:
1. Update `versionName` (and typically `versionCode`) in `app/build.gradle.kts`.
2. Commit and push to `main`.
3. The workflow `.github/workflows/auto-release.yml` will run and create the release.

## Marking Activities as Distraction-Optimized

Activities that should run while driving need to be marked as distraction-optimized in the AndroidManifest.xml file. Add the following metadata to the `<activity>` element:

```xml
<meta-data android:name="distractionOptimized" android:value="true"/>
```

This tells the system that your activity is designed to be safe to use while driving.
