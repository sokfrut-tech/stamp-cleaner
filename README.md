# StampCleaner

Android + web app for removing light paper backgrounds from stamp/signature photos and exporting transparent PNG files.

## Features
- Kotlin + Jetpack Compose Android app.
- Browser-based web app; image data stays on the device.
- Background removal by pixel brightness with sensitivity control.
- Original, blue, black, purple, red and green ink options.
- PNG export with transparency.
- GitHub Actions builds a debug APK artifact and deploys the web folder to GitHub Pages.

## GitHub setup
1. Open Settings → Pages and select **GitHub Actions** as the build source.
2. Open Actions and run **Build StampCleaner APK** (or push to main).
3. Download `StampCleaner-Android-APK` from the successful workflow run's Artifacts.
4. The Pages deployment URL appears in the workflow deployment environment.

## Android build
JDK 17 and Gradle 8.7 are required. Run `gradle assembleDebug`.
APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Limitations
This first version removes light pixels using luminance. Shadows, coloured paper, uneven lighting and very pale ink may need manual sensitivity tuning. Inspect the exported image before using it.

## Privacy
The web app processes images locally in the browser and does not upload them. The Android app processes the selected image locally.
