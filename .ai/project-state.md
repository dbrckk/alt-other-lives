# ALT project state

## Product
Android solo-first alternate-life generator. Core loop: photo -> What if scenario -> timeline -> cinematic reveal -> share.

## Current implementation
- Android Jetpack Compose app uses feature/core package boundaries.
- Photo picker and eight local alternate-life scenarios work.
- Timeline reveal is functional.
- A real 1080x1920 JPEG share-card renderer is implemented.
- Share cards use Android FileProvider and the standard Sharesheet.
- Share cards can also be saved to Pictures/ALT through MediaStore.
- CI validates unit tests, Android lint and debug APK assembly.
- CI through run #28 is green.

## Current priority
Finish the local/shareable product loop: validate gallery saving, then improve preview/polish and begin the durable local history layer before MP4 export.

## Selected star-list references
- android/nowinandroid
- airbnb/lottie-android
- Fission-AI/OpenSpec
- comfyanonymous/ComfyUI
- appium/appium (later)
- orhun/git-cliff (later)

See `docs/STAR_LIST_ADOPTION.md` for adoption/defer/reject decisions.
