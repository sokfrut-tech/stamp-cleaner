# StampCleaner — Neon Erotic Club / Cyber Glam theme

## Product decision
Create an adult, sensual, deliberately veiled club aesthetic for a stamp/signature utility. The visual language is suggestive rather than explicit: silhouettes, stagewear, shoulders/back, soft-focus lighting, haze, neon reflections and brief choreographic accents. No explicit nudity or graphic sexual activity.

## Non-negotiable product requirements
- Preserve every existing image-processing, crop, color, DPI and transparent PNG export feature.
- Do not modify extraction algorithms as part of a visual-theme change.
- Keep stamp/signature previews crisp and checkerboard transparency legible; never blur the work canvas.
- Keep controls compact and usable on phones. Avoid oversized buttons and excessive vertical spacing.
- The background must never block pointer/touch input or reduce contrast of working panels.

## Background behavior
- Built-in fallback: dark plum/black cyber-club gradient, magenta/violet glow and a restrained abstract dancing silhouette.
- Optional user-selected local video or image; no upload, no server transfer, no external tracking.
- Video is muted, looping, inline, paused while the page/app is hidden, and disabled when motion is off or reduced-motion is requested.
- Provide reset-to-default and a simple motion on/off control.
- Use a dark overlay and low opacity so text and tools remain readable.
- User-provided media stays local and is not bundled into releases unless separately supplied with permission and explicitly approved.

## Android implementation
- Match the web theme with Compose colors and a backdrop layer.
- Add a document picker for local video/image backgrounds, mute and loop video, and release player resources on reset/disposal.
- Keep the existing image picker and PNG export behavior unchanged.
- Avoid adding broad storage permissions; use Android's system document picker.

## Acceptance checks
1. Existing web processor tests pass unchanged.
2. Web JS syntax checks pass.
3. Android debug APK builds.
4. Manual QA confirms photo selection, processing, crop, DPI export and PNG transparency remain functional.
5. Background can be changed/reset/disabled without affecting the work canvas.
6. Reduced-motion and hidden-tab behavior stop animation/video.
7. No copyrighted performer image/video is bundled without user-provided rights/permission.

## Asset note
The user has not supplied the reference dance video or an authorized portrait. Until supplied, use only original abstract CSS artwork and allow local selection of the user's own media.


## App icon requirement — Neon Club / Cyber Glam
- Create a distinctive, sensual, premium adult-club app icon, not a generic cleaning-tool icon.
- Visual direction: elegant feminine silhouette in a confident dance pose, dramatic hip-line/shoulder contour, glossy neon magenta and violet rim lighting, dark plum/black background, subtle glass or chrome accents.
- Keep it tasteful and non-explicit: fully clothed/stagewear silhouette, no visible genitals, no explicit nudity or sexual act. The icon should suggest nightlife and sensuality through pose, lighting and composition rather than explicit anatomy.
- Icon must remain recognizable at small Android launcher sizes: one clear central silhouette, strong contrast, no tiny text, no fine details.
- Deliver adaptive icon foreground/background assets and a legacy launcher icon; test round and square masks.
- Do not use Aeysa's likeness or any real performer's portrait unless the user supplies an image and confirms rights/permission. Until then, create original artwork.
- Add icon preview to the design QA checklist and verify it is the actual installed APK launcher icon, not just an in-app logo.
