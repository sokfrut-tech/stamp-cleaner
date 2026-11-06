# StampCleaner / Neon Club — Project Handoff
Updated: 2026-11-06. Repository: sokfrut-tech/stamp-cleaner. Known PR #4 on feature/neon-club-cyber-glam is historical; recheck live state.

## Product goal
A polished Android app for removing stamps, signatures, text and unwanted ink from scanned documents/images, preserving the original and exporting genuinely transparent PNG when requested. Visual identity: Neon Club / cyber-glam.

## Non-negotiable requirements
- Real document-cleaning utility; visual theme must not replace core functionality.
- Stamp, signature and text removal modes.
- Ink/color selection, crop, sensitivity and blue-ink HSV assistance.
- Accurate physical output size in millimetres with DPI control.
- True alpha transparency in PNG export, not white pixels posing as transparency.
- High-quality interface and a visible adult female dancer at the side, dancing/turning. Revealing stage costume is acceptable; no nudity.
- The dancer is important to the concept. Do not silently remove her or substitute an abstract silhouette.
- Do not claim full 360-degree animation if implementation only uses a few transforms/sway frames.

## Existing project documents
Read docs/QA_AND_HIVE_REVIEW.md, docs/SMART_EXTRACTION_AND_STUDIO_PLAN.md, docs/SMART_EXTRACTION_IMPLEMENTATION_BRIEF.md, README and current source.
Earlier notes also mentioned NEON_CLUB_THEME.md, NEON_CLUB_QA_CHECKPOINT_2026-11-05.md, OPEN_SOURCE_ENGINE_AUDIT_2026-11-06.md and ANDROID_BUILD_FIX_2026-11-06.md. Verify exact paths/branch; do not assume they are on main.

## Historical visual caveat
A static WEBP asset neon_performer.webp and Compose sway/bob/rotationZ transforms were previously reported. This is not equivalent to a complete dance/turn animation. Inspect current source and PR #4. If incomplete, implement deliberate multi-pose animation/sprite sequence, smooth transitions and reduced-motion option. Keep performer visible without covering controls.

## P0 — product correctness
1. Recheck PR #4, branch tip, default branch and Actions.
2. Clean Android build; record exact Gradle command/result.
3. Test import, crop, each removal mode, sensitivity/color controls and preview.
4. Verify exported PNG alpha pixel-by-pixel and physical dimensions/DPI metadata.
5. Add regression tests for boundaries, transparency, thresholds and output sizing.
6. Test large images, orientation, memory pressure and cancellation/errors.

## P1 — Neon Club presentation
1. Compare actual UI with user direction; retain clear document workspace.
2. Make the dancer visibly present and meaningfully animated, smooth and battery-conscious.
3. Improve stage lighting, neon accents, depth and typography without harming readability.
4. Check portrait phone layout and one-handed use.

## P2 — smart extraction
Use existing smart extraction docs as planning inputs; verify every claimed feature against source. Check license, offline operation, Android compatibility, model size and performance before adopting an engine. No paid cloud processing by default.

## Agent handoff truthfulness
Claude/Gemini inbox tasks were created for implementation/review. The runner historically supplied only task text and may not have checked out this target repository. A successful API response may be analysis only. Do not claim implementation without a repository commit/patch and passing tests. Gemini only for a genuine blocker.

## Definition of done
End-to-end cleaning and correct transparent/high-resolution export work; dancer is visible and animated as requested; build/tests pass; risks/licences are documented; PR/release evidence is recorded.

## First resume action
Inspect PR #4 and source tree, reconcile docs with code, run clean build/tests, then fix the first P0 defect.
