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
- Generated timeline directory keys are validated and canonically confined to private storage; invalid history timeline keys are ignored during preview loading.
- Persisted history rows are validated on both read and write, including scenario IDs, timestamps, and canonical source-photo filenames.
- Source-photo filenames use one shared validator compatible with legacy numeric IDs and current UUID imports.
- Persisted ComfyUI settings are revalidated on read, bounded in size, and invalid saved settings remain editable with a visible validation warning.
- Atomic batch recovery now restores already-moved backups even when a crash occurs before the commit marker is written.
- Persisted AI scenes are revalidated on load, corrupted files are purged, and duplicate chapter files are deduplicated to the newest valid copy.
- Media export falls back to generated scenes when a source photo is missing, and disables export when no visual asset remains.
- History cards show local private thumbnails when available and fall back to persisted AI scenes when the original photo is missing.
- History photo availability is preflighted only when the History screen is opened, avoiding startup scans across old timelines.
- Individual history entries can be deleted with confirmation; orphaned private photos and AI scenes are cleaned without affecting media still referenced elsewhere.
- Timeline restoration now runs only once at startup, preventing history mutations from unexpectedly restoring another old timeline.
- Timeline creation now waits for History persistence before opening Reveal, blocks duplicate scenario taps, and treats orphan cleanup as best-effort after logical creation.
- Photo import and full-history clearing use explicit busy states and always recover UI state even when media cleanup fails.
- Startup restoration tolerates partial local-data failures instead of aborting the entire initialization path.
- Generated ALT visuals are prioritized in Reveal and social share cards when available; the source photo remains the fallback.
- Completed MP4 exports are invalidated when timeline visuals change, and AI generation/media export actions are mutually locked to prevent stale mixed-state exports.
- ComfyUI endpoints require HTTPS, support a short-timeout validated connection test, preserve source and output image formats, reject invalid downloaded images, and surface configuration errors in the UI.
- Cross-chapter prompts now use an explicit immutable identity anchor, stronger anti-drift constraints, and optional __ALT_NEGATIVE_PROMPT__ injection for workflows with a negative conditioning path.
- Partial AI generation reports failed chapter indexes in Reveal, keeps existing scenes during failed full-regeneration attempts, and retries transient chapter failures after a short deterministic backoff.
- AI cancellation keeps generation locked until the coroutine actually finishes, preventing overlapping generation mutations.
- ComfyUI source uploads and generated-image downloads are size-bounded; generated images also enforce shared dimension/pixel limits before persistence or rendering.
- Repeated generation from the same private source reuses a deterministic opaque ComfyUI upload name instead of accumulating UUID-named duplicate uploads on the server.
- ComfyUI polling absorbs transient GET failures without requeueing the prompt, generation-wide timeouts do not auto-requeue, queue POST I/O failures are treated as ambiguous, and image downloads retry the same remote output instead of creating a new prompt.
- ComfyUI prompt IDs, image reference types, remote filenames/subfolders, base URL structure, and text response sizes are validated/bounded before use.
- Share JPEGs and timeline scene JPEGs use unique atomic cache writes; cancelled MP4 exports delete their partial output immediately; stale share cache files are purged after 24 hours.
- Partial AI generation no longer re-persists successful chapters at completion, and missing failed chapters have an explicit targeted retry action.
- ComfyUI remote filenames/subfolders and local cache image extensions are normalized and validated before use.
- Heavy JPEG/MP4 preparation work runs off the main UI thread.
- Bitmap decoding now enforces a post-sampling pixel budget, validates actual decoder output size, and recycles oversized or failed render bitmaps to reduce OOM risk.
- Video preparation returns a cleanup-aware render session; intermediate scene JPEGs are deleted on success, failure, cancellation, navigation, or Media3 startup failure.
- Gallery saves reject empty copies and verify MediaStore finalization; share actions revalidate cache URIs at share time and fail gracefully if media disappeared.
- Cross-chapter identity prompts now explicitly preserve nose/lip/jaw/face proportions and natural asymmetries while allowing clothing, grooming, hairstyle and environment to evolve.
- ComfyUI generation cache is explicitly exposed through FileProvider, downloaded chapter images are deleted immediately after private persistence, and cancelled full-regeneration downloads are cleaned with cancellation-safe cleanup.
- Gallery saves expire unfinished pending MediaStore rows after 24 hours, share-image gallery copies delete their temporary cache immediately, and Media3 late callbacks are suppressed after explicit cancellation.
- Startup restoration keeps the active photo and active timeline context consistent; a newly imported loose photo no longer inherits an older timeline key.
- Persisted Reveal scenes are restored off the main thread with explicit restore progress, and generation callbacks can persist completed chapters asynchronously without blocking Compose.
- Timeline seed reads/writes and generated-scene persistence are dispatched to IO; AI scene removal and exported MP4 gallery copies are also performed asynchronously.
- Fresh full-regeneration seeds use SecureRandom rather than monotonic clock values, remain positive for ComfyUI, and interrupted/corrupt seed writes recover the last valid backup before a new seed is created.
- CI validates unit tests, Android lint and debug APK assembly.
- Latest confirmed green run: #456. Runs #448, #451, #454 and #456 validate deterministic ComfyUI source upload reuse, secure positive seed generation, and interrupted/corrupt seed recovery in addition to unit tests, lint and debug APK assembly.

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
