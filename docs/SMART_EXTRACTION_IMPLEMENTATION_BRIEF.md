# Implementation brief — smart ink extraction engine

Date: 2026-11-05
Priority: P0 for next engineering iteration
Target branch: feature/smart-extraction-and-stamp-studio

## Problem observed by the user

The current preview of a blue signature still shows grey/white paper texture. A single global luminance threshold is not sufficient. The next build must visibly solve this before adding decorative features.

## Required implementation behaviour

### 1. Separate estimation from extraction
1. Convert source pixels to a perceptual colour representation (Lab or a well-tested approximation).
2. Estimate local paper/background colour from border and neighbourhood samples; do not assume the background is pure white.
3. Calculate per-pixel ink likelihood from colour distance, darkness and local contrast.
4. Smooth the estimated background field at a scale larger than typical pen strokes so the estimator does not absorb the ink itself.
5. Convert confidence to a continuous alpha value to preserve anti-aliased edges.
6. Retain original ink colour by default; recolour only after alpha is estimated.
7. Expose conservative/normal/deep presets as parameter sets, not as unrelated code paths.

### 2. Protect faint marks
- Use local contrast and colour deviation so pale blue/red ink can survive on cream/grey paper.
- Avoid a single global threshold that treats every page location identically.
- Include a warning/preview when background and ink are too similar to separate reliably.
- Never silently claim perfect extraction; the user must inspect the result.

### 3. Keep implementation deterministic and testable
- Put pixel processing in pure functions independent of Compose/DOM UI.
- Keep Android and Web algorithm parameters aligned and document any unavoidable differences.
- Add synthetic fixtures with known background and ink masks.
- Test alpha-channel output directly, not only screenshot appearance.

## Acceptance tests

- Uniform white paper + blue signature: background alpha is 0 (or <= 2) away from anti-aliased edges; signature centre alpha remains high.
- Cream paper + blue ink: background removed without breaking thin strokes.
- Uneven illumination gradient: no broad opaque paper haze remains.
- Soft cast shadow: background alpha is strongly reduced while ink survives.
- Grey paper + dark ink: retain the ink, remove paper.
- Thin one-pixel and anti-aliased curves: no unexpected gaps after processing.
- Recolour mode changes RGB only; it does not make removed pixels opaque.
- Exported PNG decodes with alpha and does not contain a checkerboard pattern.
- Process maximum supported dimensions without UI freeze; return a clear memory-limit message rather than crashing.

## Crop and output geometry acceptance tests

- Auto crop bounds are computed from alpha > 2, with configurable padding.
- Manual crop maps screen coordinates to source pixel coordinates correctly at all preview scales.
- At 300 DPI, a requested 40 mm diameter exports to approximately 472 pixels (40 / 25.4 × 300), with exact target geometry after rounding.
- A requested width and height are independently respected.
- PNG alpha remains intact after crop and resize.

## Exclusions for this P0
- No online AI service or cloud upload.
- No automatic recognition of a stamp as legally valid.
- No generated stamp artwork in the extraction screen.
- No unsupported promise that all forms, shadows or overlapping marks can be perfectly separated.

## Suggested implementation order

1. Extract existing Android and JS pixel loops into testable processing modules.
2. Implement adaptive background estimation and alpha confidence.
3. Add test fixtures and alpha assertions.
4. Add checkerboard preview and before/after.
5. Add crop and mm/DPI geometry.
6. Port parity tests to web.
7. Add constructor only after extraction passes the quality gate.
