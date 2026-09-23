# ALT project state

## Product
Android solo-first alternate-life generator. Core loop: photo -> What if scenario -> timeline -> cinematic reveal -> share.

## Current implementation
- Android Jetpack Compose scaffold exists.
- Photo picker exists.
- Four local scenarios exist.
- Local five-chapter reveal exists.
- GitHub Actions builds a debug APK.
- First CI run failed because Java targeted 1.8 while Kotlin targeted JVM 17.
- JVM compatibility has been corrected in `app/build.gradle.kts`; CI revalidation is pending.

## Current priority
Phase 0 from `docs/DEVELOPMENT_PLAN.md`: obtain a green build, then split the monolithic prototype into maintainable package boundaries without breaking the working flow.

## Selected star-list references
- android/nowinandroid
- airbnb/lottie-android
- Fission-AI/OpenSpec
- comfyanonymous/ComfyUI
- appium/appium (later)
- orhun/git-cliff (later)

See `docs/STAR_LIST_ADOPTION.md` for adoption/defer/reject decisions.
