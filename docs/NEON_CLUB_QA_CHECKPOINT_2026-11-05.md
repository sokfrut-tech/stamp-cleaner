# Neon Club theme — implementation checkpoint

Date: 2026-11-05
Branch: `feature/neon-club-cyber-glam`
Pull request: #4 (draft; not merged)

## Verified from GitHub
- Android workflow run `42090375591` completed successfully for commit `b53aac9f2178899a3bf0b0ccd591a5a9038b6dd9`.
- The run uploaded artifact `StampCleaner-Android-APK` (15,134,639 bytes; artifact ID `13445605774`).
- Earlier Android runs for the theme commits `94fbc0eb` and `cc2f3610` also completed successfully.
- The adaptive launcher icon foreground is now an original neon feminine stage silhouette; launcher background is deep plum. App label is `StampCleaner · Neon Club`.
- Android MainActivity includes local image/video selection controls, mute/loop playback, motion toggle and reset. The Android screen uses the Neon Club palette.
- Web app includes local-only image/video background selection, reset, motion toggle, reduced-motion handling and pause-on-hidden behavior. JS declares the status element before the background handler.
- PR remains open and draft. No merge was performed.

## Not yet verified
- No emulator/device visual QA has been completed in this checkpoint.
- The APK artifact has been generated, but the installed launcher icon and actual phone appearance have not been visually inspected.
- Android video lifecycle/resource release when the activity is paused or the background is toggled off still needs explicit device testing and hardening.
- User's reference dance video was not supplied; the current dancer is abstract original artwork, not a recreation of a particular performer or choreography.

## Required next QA
1. Install the generated APK on an Android device and inspect round/square launcher icon masks.
2. Verify photo selection, processing, manual crop, DPI export and transparent PNG remain unchanged.
3. Select a local image background, reset it, then select a local muted looping video.
4. Toggle motion off/on, switch apps, lock/unlock, and confirm playback pauses/resumes without audio or resource leaks.
5. Test small-screen layout and ensure background controls do not cover the main workflow.
6. Run web JS syntax and processor tests; verify reduced-motion and page-hidden behavior.
7. Fix any issues found before marking PR ready. Keep PR #4 unmerged until these checks are complete.

## Scope guard
Do not alter stamp extraction algorithms as part of the visual theme. Do not bundle third-party performer imagery or video without rights/permission.


## Android player hardening update — 2026-11-05
- Replaced the one-shot `GetContent` pickers with Android's `OpenDocument` picker and requested persistable read access for the selected local media URI. No broad storage permission was added.
- Added `lifecycle-runtime-compose` and a lifecycle-aware video background composable.
- Video is muted and looping; it pauses on activity pause/stop, resumes from the saved playback position when appropriate, and calls `stopPlayback()` when removed/released (including motion-off and URI replacement).
- Fixed pause/resume state so the subsequent ON_STOP event does not erase the resume intent.
- Commits: `d3293bb88c192924ad02cff2edb7652bb0378e5c`, `220af05a2ea08dd8316690eb0f347e08af914367`, `dad2fd3b502cb8bb5858966e3709520e94d13356`.
- Fresh Android CI runs for the dependency and player commits: `42091131768`, `42091140463`, `42091145477`. At checkpoint update time these were still in progress; do not mark the new player changes as build-verified until a completed successful run is observed.
- Physical-device QA remains required: verify background playback pause/resume, motion toggle, local URI persistence after app restart, and confirm all stamp-cleaning workflows remain unchanged.

- Additional guard: if the video preparation callback fires while the Activity is already stopped, it no longer starts playback in the background; playback waits for the lifecycle resume event. Commit: `5e598a836fac5f7c78521b786a8f8be2024e0569`.
- CI runner status note: runs `42091131768`, `42091140463`, `42091145477`, `42091171173`, `42091187047`, and `42091248568` reached post-job cleanup but GitHub still reported `in_progress` with no conclusion/artifact at the latest inspection. Treat the current source as not yet build-verified.


## CI follow-up — 2026-11-05
- Confirmed completed success for Android runs `42091140463`, `42091145477`, `42091171173`, `42091187047`, `42091248568`, `42091505778`, and `42091518642`.
- Run `42091518642` built the current player-source commit `5e598a836fac5f7c78521b786a8f8be2024e0569`; artifact `StampCleaner-Android-APK`, ID `13445232878`, size `15,160,373` bytes, available and not expired.
- Run `42091565324` for documentation checkpoint commit `cdf866c9570abb9d1018a0f3b6405cf94841dba3` was still `in_progress` at latest inspection. The Android source itself is covered by the successful `42091518642` run.
- Resource tree currently contains the adaptive launcher icon XML at `mipmap-anydpi-v26/ic_launcher.xml` and foreground vector; manifest references it for both `icon` and `roundIcon`. Since `minSdk=26`, no pre-26 legacy icon density assets are required. Visual launcher-mask appearance still needs device inspection.
- No physical-device/emulator interaction test has been performed; CI build success does not prove touch layering, actual playback lifecycle behavior, or image-processing quality.


## Persistent background selection — implementation update
- The previously persisted URI grant was not enough to restore the chosen background after process restart because the URI itself was only held in Compose memory. Fixed this by storing the selected URI, media type, and motion toggle in app-private SharedPreferences and restoring them when the screen starts.
- This stores only the local document URI and UI preferences; it does not copy the user's media file or request broad storage access.
- Commit: `f870a018fa62fb4351fc599cd71db3c9440ee19e`. Build verification for this latest persistence change is pending.


## Small-screen layout refinement
- Reserved a 58 dp top inset in the scrollable StampCleaner screen so the floating background picker/reset/motion controls no longer sit over the app title/header on narrow phones. The rest of the screen remains vertically scrollable; processing logic and image preview/export behavior are unchanged.
- Commit: `eae8ab5dd7c2f999282ff5879bd91287c1d0d02b`. Build and visual device verification pending.
