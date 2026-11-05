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

## Independent review requests
On 2026-11-05, independent review requests were written to the Universal-AI-Factory inboxes:
- `coordination/claude/inbox/2026-11-05_stampcleaner-pr1-review.md`
- `coordination/gemini/inbox/2026-11-05_stampcleaner-pr1-review.md`

These are sent requests only. No substantive Claude or Gemini review response has been received yet; the channels are not considered verified until an agent writes a matching report to its own outbox.

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
- Android source images are decoded with a 2500 px maximum dimension. Browser canvas input is capped at 6.25 megapixels to limit simultaneous canvas/ImageData allocations; verify quality and peak memory on low-RAM devices.
- PNG export now uses Android's Storage Access Framework document picker, avoiding broad storage permissions across Android versions. Verify the picker and file write on API 26, 28, 29, and 34.
- The simple luminance threshold may remove pale ink or retain shadows; test with real stamps/signatures under uneven lighting.
- The result should be visually inspected before export; avoid promising perfect separation for every scan.


## ChatGPT self-review and first successful APK build

### Successful CI evidence
- Workflow: `Build StampCleaner APK`
- Run: #31, ID `42064189821`
- Source commit built: `fba22646a0a5d2db309b674fd4675ec258883eff`
- Result: **success**. Checkout, JDK 17, Gradle 8.7, Android SDK package installation, wrapper generation, Kotlin compilation, `assembleDebug`, and artifact upload all completed successfully.
- Gradle reported: `BUILD SUCCESSFUL`; 35 actionable tasks executed.
- Artifact: `StampCleaner-Android-APK`, artifact ID `13433770600`, ZIP size 15,100,309 bytes.
- Artifact ZIP SHA-256: `44e49f8b600f3d0148bb893ac1e4da396f674e53580c8c7883116ec67b2c65f7`.
- Extracted APK: `app-debug.apk`, 15,529,618 bytes; SHA-256 `e5ff1881d8837a4dfec866deedecf8329f831533bb0a426ecbf04deb3190343f`.
- The artifact was downloaded and its ZIP contents and Android package file signature were checked locally.
- Web JavaScript syntax was checked separately with `node --check` against the current `web/app.js`; it exited successfully.

### Self-review findings (not an independent audit)
- **P2 — Image quality limitation:** the current background-removal algorithm is a global luminance threshold. Pale ink, colored paper, shadows, and uneven illumination can cause lost strokes or residual background. Keep the sensitivity control and require visual preview; consider adaptive thresholding and a background sampling mode in a later iteration.
- **P2 — EXIF orientation:** Android bitmap decoding does not currently apply EXIF orientation. Some phone photos may appear rotated; add EXIF-aware orientation handling and test portrait/landscape camera images.
- **P2 — Runtime coverage:** no emulator or physical-device interaction test has yet been recorded. The CI build proves compilation and APK packaging, not picker behavior, image quality, file saving, or installability on a user's specific phone.
- No P0/P1 code defect was established by this source inspection. This is not a substitute for a device test or security audit.

### Release gate
The APK build is now verified and an installable debug APK artifact exists. Before calling the product fully released, install and test the APK on a real Android device, test a set of real stamps/signatures, and verify the saved PNG retains transparency. GitHub Pages deployment still requires merging the web workflow to `main` and confirming the deployment run.


## Merge and handoff status
- PR #1 was marked ready and squash-merged into `main` on 2026-11-05.
- Merge commit: `4d99112d8df2663a067e9822201d445ecea837b3`.
- The APK from successful CI run #31 was downloaded and checked as a valid Android APK archive; it is a **debug build**, not a Play Store/release-signed build.
- The user instructed not to wait for Claude/Gemini responses. Their inbox requests remain informational only; this project proceeds under ChatGPT self-review.
- GitHub Pages deployment is expected from the merge push, but the deployment result and public URL availability have not been confirmed through the available GitHub Actions lookup. Do not report the website as live until that check succeeds.
- Physical Android-device testing remains outstanding.


## Smart extraction implementation — first iteration (2026-11-05)

Implemented on `feature/smart-extraction-and-stamp-studio`:
- Replaced the global luminance-only extraction in Android with a local paper-colour estimate.
- Replaced the web global luminance loop with the same tile-median background estimation approach.
- The estimator samples each 24 px tile, uses per-channel medians to reduce the effect of sparse ink, then bilinearly interpolates the estimated paper colour.
- Alpha is derived from RGB distance to the local paper estimate; ink recolouring is applied after alpha estimation.
- Image processing remains local; no upload service was added.

### Important limitations
- This is an initial adaptive heuristic, not a trained segmentation model and not a guarantee that all shadows/forms will be removed.
- Median tiles can be contaminated by dense stamps or text filling most of a tile; this needs dedicated test fixtures.
- The Android and browser implementations were manually kept structurally aligned, but automated cross-platform parity tests have not yet been added.
- EXIF orientation, crop tools, mm/DPI sizing and the constructor are not yet implemented.

### CI
- Workflow run #37, ID `42066957060`, was observed in progress after the Android and web algorithm changes.
- Do not call this iteration build-verified until the run concludes successfully and the APK artifact is present.
- Required next QA: alpha-channel unit fixtures for white/cream/grey paper, gradients, shadows, pale blue ink, thin lines, dense stamps, and recolouring; compare Android and web outputs.
