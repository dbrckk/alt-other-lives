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
- Generated scenes are persisted privately under app files and restored when reopening a scenario.
- Scenario-specific prompt profiles and a stable timeline seed improve sequence continuity.
- Per-chapter ComfyUI generation retries once on transient failure.
- Partial generation keeps successful chapters and supports targeted generation of only missing chapters.
- Full regeneration requires confirmation; generated AI scenes can be explicitly removed to return to local rendering.
- CI validates unit tests, Android lint and debug APK assembly.
- Latest confirmed green run: #111.

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
