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
