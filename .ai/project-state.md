# ALT project state

## Product
Android solo-first alternate-life generator. Core loop: photo -> What if scenario -> alternate timeline -> cinematic reveal -> 9:16 export -> share.

## Current implementation
- Android Jetpack Compose app uses feature/core package boundaries.
- Photo picker and eight alternate-life scenarios work.
- Timeline reveal, local history and persisted generated scenes are implemented.
- Share-card export produces a real 1080x1920 JPEG, uses FileProvider, Android Sharesheet and MediaStore save.
- Cinematic MP4 export uses Media3 1.6.1 with multi-scene composition, intro/outro, per-scene motion, progress, cancel, share and gallery save.
- Source photos are downsampled for rendering to reduce memory pressure.
- ComfyUI is the first real generation backend.
- ComfyUI configuration is stored locally with DataStore.
- The app uploads a source image, binds workflow placeholders, queues prompts, polls history, surfaces execution failures, downloads outputs and previews generated chapters.
- Required workflow placeholders: __ALT_SOURCE_IMAGE__ and __ALT_PROMPT__.
- Optional workflow placeholder: __ALT_SEED__.
- Generated scenes are persisted privately per timeline and restored without cross-contaminating different lives that share a scenario.
- Scenario-specific prompt profiles and a persisted per-timeline seed improve sequence continuity across partial retries and app restarts.
- Per-chapter ComfyUI generation retries once on transient failure.
- Partial generation keeps successful chapters and supports targeted generation of only missing chapters.
- Full regeneration requires confirmation, uses a fresh seed, and is atomic: the current timeline is replaced only if every requested chapter succeeds.
- AI generation can be cancelled cooperatively; completed chapters are persisted immediately for partial runs.
- Source photos are copied atomically into private app storage with UUID names, validated on import and restore, restored after restart, and associated with history entries.
- Source-photo and generated-scene orphan cleanup prevents private storage from growing indefinitely.
- Generated-scene replacement and timeline-seed writes are rollback-safe and recover after interrupted writes.
- Full AI regeneration commits scenes and seed in one atomic batch transaction; interrupted commits roll back together, completed commits are preserved, and rollback recovery is idempotent.
- Atomic batch recovery rebuilds missing manifests from staged/backed-up files when possible and validates chapter indexes before commit.
- Pure JVM tests cover batch recovery planning, completed-commit preservation, scene batch index validation, and the production scene filename codec.
- Persisted AI scenes are revalidated on load, corrupted files are purged, and duplicate chapter files are deduplicated to the newest valid copy.
- Media export falls back to generated scenes when a source photo is missing, and disables export when no visual asset remains.
- History cards show local private thumbnails when available and fall back to persisted AI scenes when the original photo is missing.
- History photo availability is preflighted only when the History screen is opened, avoiding startup scans across old timelines.
- Individual history entries can be deleted with confirmation; orphaned private photos and AI scenes are cleaned without affecting media still referenced elsewhere.
- Timeline restoration now runs only once at startup, preventing history mutations from unexpectedly restoring another old timeline.
- Generated ALT visuals are prioritized in Reveal and social share cards when available; the source photo remains the fallback.
- Completed MP4 exports are invalidated when timeline visuals change, and AI generation/media export actions are mutually locked to prevent stale mixed-state exports.
- ComfyUI endpoints require HTTPS, support a short-timeout validated connection test, preserve source and output image formats, reject invalid downloaded images, and surface configuration errors in the UI.
- Heavy JPEG/MP4 preparation work runs off the main UI thread.
- Persisted Reveal scenes are restored off the main thread with explicit restore progress, and generation callbacks can persist completed chapters asynchronously without blocking Compose.
- Timeline seed reads/writes and generated-scene persistence are dispatched to IO; AI scene removal and exported MP4 gallery copies are also performed asynchronously.
- CI validates unit tests, Android lint and debug APK assembly.
- Latest confirmed green run: #275. Runs #276–#278 validate the shared production scene filename codec and its tests.

## Current priority
Keep the end-to-end AI generation path reliable, then improve identity continuity, partial-failure recovery and production UX before monetization.

## Selected star-list references
- android/nowinandroid
- airbnb/lottie-android
- Fission-AI/OpenSpec
- comfyanonymous/ComfyUI
- appium/appium (later)
- orhun/git-cliff (later)

See `docs/STAR_LIST_ADOPTION.md` for adoption/defer/reject decisions.
