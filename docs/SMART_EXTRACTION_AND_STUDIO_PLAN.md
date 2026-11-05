# StampCleaner — product and engineering plan

Updated: 2026-11-05
Branch: feature/smart-extraction-and-stamp-studio
Status: approved product direction; implementation not yet complete.

## Product decision

The primary job is **extracting real, existing ink impressions** from phone photos and scans:
- signatures;
- round and rectangular stamps;
- text stamps and dater stamps.

The secondary job is **creating new stamp layouts**. It belongs in a separate “Конструктор” workspace and must never be confused with extraction of an authentic physical impression.

## User outcomes

1. Import a photo or scan from the phone.
2. Select the content type: “Печать”, “Штамп”, or “Подпись”.
3. Remove paper colour, shadows, stains and form artefacts while preserving thin and pale ink strokes.
4. Preview on a checkerboard that represents true transparency.
5. Crop automatically to the detected ink bounds or manually adjust a crop rectangle with touch.
6. Set exact physical output dimensions in millimetres and resolution (default 300 DPI); preserve the requested physical dimensions in export metadata where supported.
7. Export a transparent PNG suitable for placement in Word and PDF.
8. Keep all image processing local on the device/browser by default. Do not upload user documents.

## Phase 1 — extraction (highest priority)

### Image pipeline
- Decode with EXIF orientation correction.
- Work on a bounded-resolution copy to protect memory; retain original dimensions for export where feasible.
- Estimate paper/background colour from border samples and robust local neighbourhoods rather than assuming pure white.
- Offer three processing strengths: “Мягкая”, “Обычная”, “Глубокая”.
- Use adaptive/local background estimation and colour-distance confidence, not a single global luminance cutoff.
- Preserve anti-aliased edges by deriving alpha from ink/background separation; avoid hard binary edges.
- Offer ink modes: keep original colour, normalize to blue/black/red/green/purple, and optionally separate overlapping stamp/signature only if a reliable method is implemented.
- Add a non-destructive preview and “До/После” comparison.
- Provide undo/reset to original settings.

### Crop
- Automatic tight crop from non-trivial alpha bounds with configurable transparent padding.
- Manual touch crop rectangle with drag and corner handles.
- Never silently cut strokes: show crop preview and allow reset.
- Optional presets for square/portrait/landscape canvas, while keeping the content aspect ratio.

### Physical size
- Separate width/height or diameter in mm from pixel dimensions.
- Default export resolution: 300 DPI; allow 150/300/600 DPI.
- Stamp presets may suggest common diameters (for example 40 and 45 mm), but all dimensions remain editable and are not asserted as universal legal standards.
- Explain that physical size in Word also depends on document/editor import settings; provide a quick “check size” guide.
- Keep transparent padding and crop bounds explicit.

### Export and verification
- PNG must contain an actual alpha channel; checkerboard is preview-only and must not be baked into the file.
- Verify sample pixels: background alpha near 0; ink and anti-aliased edges retain non-zero alpha.
- Verify exported image dimensions against mm × DPI / 25.4.
- Keep original input untouched.
- Name output clearly, e.g. stamp-cleaner-signature.png.

## Phase 2 — constructor (separate workspace)

- Round, oval and rectangular layout templates.
- Editable text lines, font, size, tracking, alignment and border thickness.
- Editable diameter/width/height in mm.
- Optional centre logo/image upload, with fit and alignment controls.
- Live preview and transparent PNG export.
- Save/reopen editable project data locally.
- Clearly label output as a newly designed layout, not an extracted or authentic physical stamp. Do not include government emblems or imply official authorization.

## Platforms

### Android
- Keep Jetpack Compose and local processing.
- Use a clear three-stage workflow: Import → Clean/Crop → Size/Export.
- Touch-first crop handles and readable controls.
- Handle large images and low-memory devices gracefully.
- Preserve SAF document picker export.

### Web
- Keep browser-side Canvas/Web APIs and local-only processing.
- Mirror the Android workflow and options.
- Provide responsive phone and desktop layouts.
- Do not send image data to a server.

## Quality gates (must pass before calling the feature complete)

### Image quality corpus
Create synthetic fixtures and, where available, user-consented test scans:
- white, cream, grey and coloured paper;
- uneven illumination and cast shadows;
- blue/black/red ink;
- thin, pale and broken strokes;
- stamps crossing printed text, ruled lines and signatures;
- JPEG artefacts and rotated EXIF photos.

### Automated assertions
- Background becomes transparent without a checkerboard baked into the pixels.
- Thin strokes remain connected and visible.
- No large background regions remain opaque.
- Alpha edges remain anti-aliased.
- Automatic crop contains every retained ink pixel plus the selected padding.
- Manual crop coordinates map correctly between preview and source image.
- Physical-size export dimensions match the chosen mm and DPI within rounding.
- Large-image processing avoids crashes/OOM and gives actionable error messages.

### Release gates
- CI compiles Android and checks web JavaScript.
- Add unit tests for image pipeline and dimension calculations.
- Run emulator smoke test and real-device test for import, crop, export and transparency.
- Verify GitHub Pages deployment after merge.
- Do not merge a release candidate with failing tests.
- Record actual CI run IDs, artifact checksums and device test evidence in docs/QA_AND_HIVE_REVIEW.md.

## Privacy, safety and legal clarity
- Default to on-device processing; no cloud upload without explicit user choice.
- Do not retain source photos or extracted marks beyond user-controlled local storage.
- A PNG of a stamp/signature is an image asset, not a cryptographic or legally qualified electronic signature.
- The constructor must not imply that generated artwork is an authorized official seal.

## Delivery sequence

1. Implement and test robust background extraction first.
2. Add EXIF correction and safe large-image handling.
3. Add checkerboard transparency preview and before/after comparison.
4. Add automatic and manual crop.
5. Add mm/DPI sizing and metadata-aware export.
6. Mirror features in web.
7. Add constructor as a distinct module.
8. Perform QA, independent review where available, and device verification.
9. Merge only after release gates pass.

## Current known baseline

The current main branch uses a simple global luminance threshold in Android and web. This is known to leave paper texture/shadows and/or remove pale strokes. The user has already tested it and reported that the output still looks like it has a background. Therefore, the next implementation must prioritize a better extraction algorithm; cosmetic UI work is secondary.


## Implementation progress — 2026-11-05

- Android and web now expose explicit content modes: signature, stamp, and text stamp.
- Mode selection applies a starting sensitivity value (signature 24, stamp 38, text stamp 48); the user can still adjust the slider.
- Automatic transparent-bound crop is implemented in Android and web with a small safety margin and an opt-out checkbox.
- Android preview uses a checkerboard background; the checkerboard is not part of the exported bitmap.
- The mode-specific values are presets over the current shared adaptive extraction algorithm, not three separate extraction engines. This limitation must remain visible until image-corpus testing proves mode-specific processing quality.
- Physical-size controls, DPI-aware PNG metadata, EXIF orientation, manual crop, undo, and automated image-quality fixtures remain pending.
- Latest CI run at this update: run #67 (42067953280); JavaScript syntax step passed, Android compile/build was still running. Do not treat this as a verified release until the job and artifact complete.


## Physical-size export progress — 2026-11-05

- Added editable output width and height in millimetres to Android and web export screens.
- Added 150, 300 and 600 DPI presets; 300 DPI is the default.
- Export pixel dimensions are calculated as `mm × DPI / 25.4` and bounded to 40 million pixels / 5–300 mm per side.
- PNG exports now include the standard `pHYs` chunk (pixels per metre) with the selected DPI.
- Output content is fitted proportionally inside the requested physical canvas; it is not stretched. Any unused area remains transparent.
- Important caveat: some document editors or import paths may ignore PNG physical-resolution metadata. Users should verify the displayed size in Word after insertion.
- CI for web commit `ea325ffd8ddf90c31e8786ddfb4ca253cb07b187` was queued/in progress at the time of this note; Android compile and artifact still require a completed green run. No release claim is made yet.


## Manual crop implementation progress — 2026-11-05

- Web preview now supports pointer/touch drag to draw a crop rectangle, apply it to the transparent result, and clear the pending selection. Cropping uses source bitmap coordinates and preserves alpha.
- Android result preview now supports finger-drag rectangle selection and applying the crop; a pending selection can be cleared before applying.
- The current manual crop interaction draws a new rectangle; drag-to-move and corner/edge resize handles are not implemented yet. This is a first usable crop pass, not the final editing experience.
- Successful workflow run #81 (`42068729226`) completed all steps, including JavaScript syntax check, Android debug APK build, and artifact upload. That run predates the latest manual-crop commits; the new crop changes still require a fresh green build.
- The crop mapping and touch behavior need device QA on narrow screens, portrait/landscape images, and very small selections. Do not merge PR #2 until the new workflow passes and the manual crop has been exercised.


### Manual crop refinement — 2026-11-05

- Web: crop rectangle can now be drawn, moved by dragging inside it, resized from four corner handles, applied, or reset before applying. Touch and mouse use pointer events.
- Android: crop rectangle can be drawn, moved by dragging inside it, resized from the lower-right corner, applied, or cleared before applying.
- Known limitation: Android currently exposes one resize corner rather than all four; its crop preview mapping needs device validation for letterboxed images. Web corner handles and pointer capture also require mobile-browser QA.
- Latest code commit: `b3a0fe4fc323f0c9521395bb54ca4d51f931b8ed`. The GitHub Actions wrapper did not return a run for this push, and the combined commit status is empty; therefore this revision is not yet CI-verified.
- Do not merge PR #2 until a fresh workflow run confirms JS syntax and Android compilation, then verify crop gestures on a physical phone.


## Synthetic extraction tests and CI — 2026-11-05

- Extracted the web image-processing algorithm into `web/processor.js`, shared by the browser and Node test runner. The browser UI now calls the same tested `StampProcessor.processImageData` function.
- Added `web/tests/processor.test.js` with synthetic white, cream and gray paper fixtures; checks that background corners become transparent and a blue ink stroke remains; verifies recoloring preserves alpha; checks automatic transparent crop bounds; and checks empty/malformed input handling.
- Updated `.github/workflows/build-apk.yml` to syntax-check both JavaScript files and run `node --test web/tests/*.test.js` before Android compilation.
- These tests characterize the current adaptive heuristic, not a proof of perfect extraction for arbitrary photos. Dense stamps, strong gradients, shadows and very faint ink still require broader fixtures and real-device validation.
- Await a fresh CI run for this revision before treating the tests or Android build as passing.


### First synthetic test run — diagnosis and correction

- CI run #120 (`42070442608`) passed both JavaScript syntax checks, then failed 3 of 6 synthetic tests at the ink-retention assertion. Android build was correctly skipped by the fail-fast workflow.
- Root cause was in the fixture assertion, not a demonstrated processor regression: the test drew a curved sinusoidal stroke but sampled a single fixed pixel that was not on the curve. Updated the assertion to scan the defined stroke region and require retained alpha there.
- New test commit: `92f2530d40d6ae1350dc8af64199edef8b6221d0`. Await the next CI run; no passing claim until confirmed.
