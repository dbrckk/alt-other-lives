# ALT — See Your Other Lives

ALT is an Android-first app that turns one photo and a “What if…?” scenario into a cinematic alternate-life timeline designed to be enjoyed solo and shared vertically.

## Product loop

1. Pick a photo.
2. Choose a scenario.
3. Generate an alternate timeline.
4. Reveal the life chapter by chapter.
5. Share the result.

## V1 principles

- Solo-first: no social graph required.
- Privacy-first: the original photo stays local unless a remote generation provider is explicitly enabled.
- Share-first: 9:16 output is a first-class product surface.
- Cost-aware: local composition and animation before expensive video generation.
- Android-first: Jetpack Compose, Material 3, minSdk 26.

## Status

ALT has a working end-to-end Android prototype with photo import, eight alternate-life scenarios, persisted history, ComfyUI image generation, partial retry and cancellation, cinematic 9:16 JPEG/MP4 export, gallery save and Android sharing.

The current engineering phase focuses on production reliability, privacy, release hardening and UX polish before store publication.
