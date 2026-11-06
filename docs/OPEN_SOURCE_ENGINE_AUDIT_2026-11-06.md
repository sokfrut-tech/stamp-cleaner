# StampCleaner — open-source engine and visual asset audit

Date: 2026-11-06
Target: `feature/neon-club-cyber-glam` / PR #4
Status: research checkpoint; no third-party code copied or dependency added yet.

## User-visible design requirement

The Neon Club identity must include an adult, glamorous woman in a revealing but non-nude club outfit, positioned to one side of the workspace. She dances and turns in a smooth loop; the editor, source preview, result preview, and controls remain unobstructed. Provide a motion/reduced-motion switch and a hide-mascot option. The same visual identity should inform the launcher icon and splash/background. Do not use an abstract silhouette as a substitute for the requested woman.

Preferred asset pipeline: original generated/commissioned artwork with explicit project usage rights, stored in the repository as a bundled local asset. Animate with a lightweight sprite/sequence or restrained transform loop; avoid remote media and avoid forcing video decoding on low-memory phones.

## Existing open-source candidates reviewed

### 1. Stamp-Detection-and-Processing
Repository: https://github.com/gagan2113/Stamp-Detection-and-Processing

Public description indicates:
- YOLO-based stamp detection; an additional stamp/signature model is described.
- HSV colour filtering for blue stamps.
- Background removal / extraction pipeline.

Assessment:
- Strong candidate for stamp-region detection and blue-ink isolation concepts.
- Must inspect model files, training-data provenance, inference code, model license and redistribution terms before bundling anything.
- HSV range filtering is useful as a selectable assist, not a universal solution for faded, purple, red, or unevenly lit stamps.
- Repository license was not independently verified in this audit; do not copy code or weights until confirmed.

### 2. Stamp-Signature-Segregate
Repository: https://github.com/KumarShivam1908/Stamp-Signature-Segregate

Public description indicates:
- YOLO object detection plus SegFormer segmentation for overlapping stamps and signatures.
- Streamlit demo.

Assessment:
- Most directly relevant research lead for separating overlapping marks.
- Likely model/runtime and conversion burden for Android; inspect weights, model card, dataset rights, inference dependencies and license.
- Treat as a research prototype, not production-ready Android code, until reproducible tests prove otherwise.
- License was not independently verified in this audit; no code or weights may be copied yet.

### 3. Cel Android
Repository: https://github.com/MRJOHN5ON/cel-android

Public description indicates:
- On-device ONNX background removal.
- BiRefNet Lite, ISNet General and U²-Net Human options.
- Local processing; model downloads are required initially.
- MIT project license is stated in the public repository description; model licenses/weights must still be tracked separately.

Assessment:
- Best ready-made Android integration reference for ONNX Runtime and model lifecycle.
- Generic subject segmentation may not preserve faint stamp lettering or isolate ink from same-colour document text.
- Do not make the large model bundle mandatory. Prototype an optional downloadable model pack, with offline deterministic colour extraction retained as fallback.
- Confirm exact ONNX Runtime version, ABI support, model size, memory and license notices before integration.

### 4. ImageToolbox
Repository: https://github.com/T8RIN/ImageToolbox

Public description indicates:
- Android image editor with background removal, crop, filters, OCR and PDF utilities.
- Apache-2.0 license is stated by the surfaced project information.

Assessment:
- Useful source of UX and feature patterns; potentially useful isolated utilities.
- Do not import the whole application. Check exact modules, dependency graph and upstream license notices before reuse.

### 5. PDFSeal
Repository: https://github.com/WGRALGO/WGRALGO-PDFSeal

Public description indicates:
- Offline Android PDF viewer/editor with crop, page management, visual signature placement and flattened PDF export.
- AGPL-3.0; MuPDF dependency is also AGPL-3.0.

Assessment:
- Good feature/UX reference for future local PDF import and export.
- AGPL has significant distribution obligations. Do not copy/link its code into StampCleaner unless the project deliberately accepts AGPL compliance. Prefer independently implementing compatible workflows or selecting permissively licensed alternatives.

### 6. Android document-scanner reference
Repository: https://github.com/aylar23/document-scanner

Public description indicates:
- Kotlin/Compose, OpenCV/native page detection, perspective correction, image enhancement, OCR and PDF export.
- Min API 24; StampCleaner currently targets API 26+.

Assessment:
- Useful reference for crop geometry, image enhancement and document import/export architecture.
- Not a stamp-extraction engine; inspect license and modules before reuse.

## Current StampCleaner baseline (inspected on branch)

- Android: Kotlin + Jetpack Compose, minSdk 26, no ONNX/OpenCV dependency currently declared.
- Android editor currently exposes stamp/signature/text modes, colour presets, threshold and ink-only control.
- Web: custom deterministic pixel processor estimates local paper colour and derives alpha; crop interaction exists in UI.
- Existing implementation should remain available as a lightweight offline fallback.

## Recommended integration order

1. **Keep current local algorithm as fallback.** Add regression fixtures for blue, red, black and faint stamps on white, cream, grey and unevenly lit paper.
2. **Prototype HSV blue-stamp assist** from the stamp-detection research. Make it optional and compare alpha masks; do not silently replace the current path.
3. **Prototype ONNX background segmentation** using Cel's Android integration pattern, initially behind an optional model download and explicit “AI cleanup” mode. Measure APK size, model download size, peak memory, latency and edge quality on a mid-range Android device.
4. **Evaluate stamp/signature segmentation model** from Stamp-Signature-Segregate only after license/data provenance and Android conversion are verified. Add as an optional specialist mode only if it demonstrably separates overlapping elements.
5. **Document workflow:** study PDFSeal/document-scanner architecture, but select permissive dependencies; local PDF page import, high-resolution rendering, crop, page navigation, and export are staged features.
6. **Parity:** share algorithm test vectors between Android and web; test PNG alpha numerically and open exported PNG in Microsoft Word/Office to check transparency.

## Acceptance gates before merging

- License and model-weight provenance documented for every third-party component.
- No user document/image leaves the device; model downloads are separate, opt-in and clearly disclosed.
- APK size and peak memory measured; graceful fallback when model is unavailable or device memory is low.
- No loss of fine stamp text, circular borders, or thin signature strokes in regression fixtures.
- Exported PNG has real alpha (not a checkerboard baked into pixels) and opens correctly in Office.
- Same-colour stamp/text overlap is explicitly reported as a limitation unless a dedicated segmentation model passes tests.
- Android and web test suites pass; real-device QA is still required before release.

## Explicitly not done in this checkpoint

- No external source code or model weights copied.
- No new runtime dependency added.
- No claim that any candidate has been integrated or device-tested.
- The dance performer artwork has not yet been added to the APK/repository; this is a separately tracked visual implementation task.
