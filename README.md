# Planck

Android Automotive (AAOS) client for Radio Streams and Subsonic

## Local Development in Android Studio

In the "Device Manager", create a new virtual device with the following settings:

- Automotive (1080p landscape) API 33
- Click "Run app" green triangle on top right op IDE. This will build and start the VM
- Keep an eye on the "Build Output" tab at the bottom of the IDE for any errors
- After a change, click "Sync Project with Gradle Files" (elephant icon) to ensure everything is up to date and "Run app" again

## Debug on a Android phone

- Go to Build > Generate App Bundle(s) / APK(s) > Generate APK
- The bundle is in app/build/outputs/bundle/debug/app-debug.apk
- Install the bundle on your phone

## Publishing to Google Play Store

- in build.gradle.kts, bump the versionCode and versionName
- Go to Build > Generate Signed App Bundle(s) / APK(s) > Generate Bundles
- Go to https://play.google.com/console/
- Create a new Closed test release for the Android Automotive track. Internal tests releases will *not* be shown in the car.

## Marking Activities as Distraction-Optimized

Activities that should run while driving need to be marked as distraction-optimized in the AndroidManifest.xml file. Add the following metadata to the `<activity>` element:

```xml
<meta-data android:name="distractionOptimized" android:value="true"/>
```

This tells the system that your activity is designed to be safe to use while driving.
