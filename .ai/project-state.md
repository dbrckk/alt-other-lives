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
- Each ComfyUI chapter is queued at most once per attempt to avoid duplicate GPU jobs after ambiguous queue, history or download failures; history and download requests retry transient errors against the original prompt/output.
- Partial generation keeps successful chapters and supports targeted generation of only missing chapters.
- Full regeneration requires confirmation, uses a fresh seed, and is atomic: the current timeline is replaced only if every requested chapter succeeds.
- AI generation can be cancelled cooperatively; completed chapters are persisted immediately for partial runs.
- Source photos are copied atomically into private app storage with UUID names, validated on import and restore, restored after restart, and associated with history entries.
- Photo imports now check coroutine cancellation during bounded copying and at commit boundaries; cancelling after the IO/UI hand-off triggers scoped rollback of the just-created private photo, while true cleanup failures remain diagnostically visible.
- Android instrumentation covers a test-APK-only pipe-backed ContentProvider that stalls mid-JPEG and a 51 MiB oversized source; these checks exercise real ContentResolver and private-file cleanup but do not replace manual third-party-provider validation.
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
- Partial AI generation reports failed chapter indexes in Reveal and keeps existing scenes during failed full-regeneration attempts; failed chapters can be retried explicitly without automatically requeueing a submitted GPU job.
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
- Seed reads are size-bounded and reject invalid/non-positive values before reuse.
- Partial-generation downloads are released even when private persistence fails, avoiding leaked cache files.
- Explicit AI seeds are validated before any ComfyUI source upload; invalid requested chapter indexes and invalid workflow templates also fail before network work.
- ComfyUI history polling surfaces execution errors immediately instead of waiting for a later generic error status.
- ComfyUI history outputs are accepted only after the server marks the prompt completed, preventing early selection of intermediate previews.
- Source-photo interrupted-import recovery deletes only app-owned expired temporary copies, preserving active imports even during concurrent startup and photo selection.
- ComfyUI photo uploads and image downloads check coroutine cancellation between bounded stream chunks, promptly stopping transfer work after the next I/O chunk finishes (socket reads remain subject to network timeouts).
- Generated scenes count as accepted only after the persistence callback succeeds.
- ComfyUI source uploads reject missing or non-image MIME types instead of silently treating them as JPEG.
- Generated-scene chapter indexes are bounded to the supported 0..4 range across batch validation, filename encoding and restore.
- Single-scene backup recovery preserves a valid backup when the current scene is corrupt.
- Atomic batch pre-commit and rollback recovery validate scene images and seed contents semantically before discarding or restoring recovery data.
- History timeline timestamps are allocated collision-free inside the DataStore transaction, and Reveal uses the actual persisted timeline key.
- Restored history is decoded lazily, limited to 50 valid entries and deduplicated by timeline key.
- CI validates unit tests, Android lint, debug APK assembly and APK artifact upload.
- Reliability baseline is now validated through cumulative green runs including #632, with source-photo quality signaling, cleanup isolation and timeline-scoped async state intact.
- Premium pass now covers Home, Scenario, Reveal and History with a consistent editorial/cinematic visual language, subtle screen motion and targeted haptics.
- Reveal chapter expansion is animated, AI generation uses a cinematic progress card, and successful complete generations emit one completion haptic.
- Social exports are localized EN/FR and optimized for fast feed readability; the 9:16 comparison card supports two-life sharing with ALT branding.
- Viral loops now include Create another life, Remix this life and Compare two lives. Remix persists a separate timeline; Compare uses saved timelines without mutating them.
- History prioritizes generated AI previews over source photos when both exist and includes a two-life comparison selection mode with visual previews.
- ComfyUI premium continuity checks warn when workflows omit the shared seed or ALT negative prompt without breaking backward compatibility.
- Identity prompts now explicitly resist beautification/de-aging/pose-expression-angle drift while preserving stable facial landmarks and proportions.
- Source photos receive a non-blocking premium-quality signal; low-resolution portraits trigger a localized recommendation rather than blocking the user.
- EN/FR localization now covers the full primary product flow: Home, Scenario, Reveal, History, advanced ComfyUI settings, social JPEGs, comparison cards and cinematic video framing.
- The scenario catalog has a full French narrative variant with stable IDs/chapter counts; ComfyUI still uses the canonical English scenario text for prompt quality.
- Export text layout uses one shared tested wrapping/ellipsis engine with explicit line budgets, including regression coverage for long French titles, subtitles and narratives.
- Generated-scene acceptance now rejects undersized outputs, social-unfriendly aspect ratios and near-uniform/visually empty frames before persistence.
- ComfyUI settings validation and premium continuity diagnostics use typed issue codes mapped to localized UI copy instead of English-message parsing.
- Reveal now exposes story length and AI-scene readiness in the hero, badges generated chapters, and uses an editorial fallback when no visual asset remains.
- Reveal share/export actions now have clearer visual priority: share is primary, cinematic video creation is a strong secondary action, and gallery saves are visually quieter.
- Home persists the imported source-photo quality signal across startup/history restoration, shows non-blocking inline quality guidance, and no longer relies on a transient low-resolution toast.
- History opening reuses its existing photo preflight cache instead of revalidating the same file synchronously on the UI thread; source quality metadata is preflighted alongside photo URIs.
- Home is vertically scrollable so the full creation path remains reachable on small screens and with larger accessibility text.
- Home now provides a localized Cancel photo import action while an import is in progress, keeps its busy lock until cancellation finishes, and shows a disabled cancelling state to prevent repeated taps.
- Scenario cards now surface the narrative arc from first to last chapter and use indexed list rendering directly.
- Generated-scene acceptance also rejects mostly transparent outputs before persistence, in addition to size/aspect/near-uniform checks.
- Local persistence of generated chapters now checks coroutine cancellation between bounded stream-copy chunks and before changing an existing scene; full-regeneration staging checks cancellation before the atomic commit boundary and never inserts cancellation checkpoints mid-commit. Device/server behavior still requires manual validation.
- ComfyUI JPEG source preparation now checks coroutine cancellation between image inspection, decoding, orientation, cropping and compression, cleans up any completed temporary JPEG before upload if cancelled, and recycles decoded bitmaps when cancellation occurs after decoding. Real content-provider latency still needs device validation.
- Android instrumentation now verifies the actual private generated-scene store retains prior chapter bytes and seed if an update is cancelled during single-chapter copying or after all five fresh scenes have been staged before the full-regeneration commit. This is a controlled emulator rollback check, not a real ComfyUI/device cancellation end-to-end test.

## Current priority
Keep the end-to-end AI generation path reliable while raising ALT to premium consumer-product quality. The active product direction is cinematic, social-first and highly shareable: Reveal, 9:16 image exports and MP4 exports must feel polished enough to publish directly to Reels, Shorts and Stories. Reliability remains the gate: visual work must not regress generation, persistence, cancellation, cleanup or export safety.

## Premium / viral quality bar
- First impression must feel intentional and cinematic from Home through Reveal.
- Reveal should read like an editorial story, not a debug timeline.
- Shared media must be understandable within one second in a social feed.
- 9:16 exports should carry recognizable but restrained ALT branding.
- Hooks, titles and chapter structure should remain readable on a phone screen.
- Avoid dense text blocks in social exports; prioritize visual identity and story beats.
- Continue to improve generated-image quality and perceived cinematic polish before adding broader acquisition mechanics.
- Keep Remix/Compare/share loops simple, local-first and non-intrusive.
- Do not add referrals, streak pressure or monetization before the core creation/share loop feels premium.

## Selected star-list references
- android/nowinandroid
- airbnb/lottie-android
- Fission-AI/OpenSpec
- comfyanonymous/ComfyUI
- appium/appium (later)
- orhun/git-cliff (later)

See `docs/STAR_LIST_ADOPTION.md` for adoption/defer/reject decisions.
