# StampCleaner — QA and HIVE review handoff

Updated: 2026-11-05

## Current implementation
- Android app: Jetpack Compose screen for selecting an image, tuning light-background removal, recoloring ink (original, blue, black, purple, red, green), previewing, and saving PNG.
- Web app: local browser-side canvas processing and PNG download in `web/`.
- GitHub Actions: Android debug APK build workflow and GitHub Pages deployment workflow.
- Work is on branch `feature/rescue-rangers-ui-hive`, PR #1 targets `main`.

## Verified CI failure and correction
The first observed PR workflow run was #15 (run ID `42062889940`). Checkout, JDK 17, and Gradle 8.7 setup passed. The job failed at `android-actions/setup-android@v3` because that action invoked `sdkmanager tools`, and the current Android SDK repository no longer provides the obsolete `tools` package. Gradle wrapper generation and APK compilation were skipped.

Correction committed:
- Removed the legacy Android setup action.
- Install only the required SDK packages directly: `platform-tools`, `platforms;android-34`, and `build-tools;34.0.0`.
- Accept SDK licenses in the workflow.
- Generate Gradle wrapper 8.7 when absent.

A separate source review found a conflicting Compose import name (`androidx.compose.foundation.Image` and the material icon `Image`). The icon is now imported as `ImageIcon` and references updated.

## Evidence limits
- The previous CI run failed before Kotlin compilation; it does not prove or disprove app source correctness.
- After the workflow/source fixes, the GitHub connector did not yet return a PR workflow run for the new commit. Do not mark the APK build as passed until a fresh Actions run completes and the APK artifact exists.
- No independent HIVE/Claude/Gemini review has been received. Do not claim an agent reviewed the code without a substantive review artifact.

## Required next steps
1. Confirm a fresh workflow run starts on the latest PR head.
2. If it fails, inspect the failing job logs and fix the first real error; rerun until `assembleDebug` succeeds.
3. Confirm artifact `StampCleaner-Android-APK` contains `app-debug.apk`.
4. Test image selection, background removal, all ink colors, transparent PNG output, and saving on a real Android device.
5. Check web version on mobile and desktop; verify transparent PNG download.
6. Merge only after CI passes and material review findings are resolved.
7. After merge, confirm Pages deployment succeeds and open the published site.
8. Request HIVE review focused on correctness, image-processing edge cases, memory use for large images, Android save permissions on API 26–28, accessibility, and security. Record substantive findings and dispositions here.

## Known product risks to test
- Large images may cause memory pressure because processing currently allocates a full-size pixel array and output bitmap.
- PNG export now uses Android's Storage Access Framework document picker, avoiding broad storage permissions across Android versions. Verify the picker and file write on API 26, 28, 29, and 34.
- The simple luminance threshold may remove pale ink or retain shadows; test with real stamps/signatures under uneven lighting.
- The result should be visually inspected before export; avoid promising perfect separation for every scan.
