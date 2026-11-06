# Implementation checkpoint — HSV blue-ink assist

Date: 2026-11-06
Branch: `feature/neon-club-cyber-glam`
Commit: `59e8e24e7d10cc8eabb125fa2b816311b4b3d392`

## Implemented

- Added an Android option: **«Выделять синие чернила (HSV)»**.
- It uses Android HSV hue degrees 180–260, corresponding to the blue hue interval 90–130 used by the public Stamp-Detection-and-Processing OpenCV example (OpenCV hue scale 0–180).
- Saturation gating suppresses low-saturation grey/black page text.
- The option is OFF by default and does not replace the existing extraction algorithm.
- Existing «Только цвет чернил» remains a separate RGB-distance filter.
- No image is uploaded; processing remains local on the device.

## Newly identified reference implementation

- `fchaussin/signature-remove-bg` — https://github.com/fchaussin/signature-remove-bg
- Public README describes a lightweight MIT-licensed, no-ML pipeline with auto-detection (Otsu + chrominance), threshold, blue tolerance, contrast, edge smoothing, line cleanup, crop and eraser with undo.
- This is a stronger practical reference for the next feature set than importing a large generic ML model.
- It is a Python/Pillow/NumPy/OpenCV service, not an Android library. Port only the small deterministic image-processing concepts to Kotlin after matching its LICENSE and dependency notices; do not add a network backend or upload user documents.
- Candidate features for native Android: blue tolerance, contrast, smoothing, ruled-line removal, processing presets and multi-level eraser undo.

## Validation status

- GitHub Actions build started for this commit: https://github.com/sokfrut-tech/stamp-cleaner/actions/runs/42208498356
- Build result pending at the time this note was written.
- No on-device test has been performed.
- The hard HSV interval may reject cyan or violet inks; user should compare original mode and blue-only mode. Next iteration should use adjustable hue tolerance rather than silently broadening the filter.

## Visual performer asset

The requested Neon Club performer must be a real bundled image asset, not the existing abstract mascot. Current image-generation output is available in the conversation, but the GitHub text-file integration cannot upload binary image assets. Do not pretend it is already included in the APK. Keep the asset integration task open until a binary upload path is available. The design target is a glamorous adult dancer in a revealing but non-nude black club dress, positioned to the side, with a restrained looping dance/turn animation and a hide/reduced-motion option.
