# ALT — star-list adoption

This document records which repositories from `dbrckk/star-list` are useful to ALT, why they are useful, and whether they should become dependencies or only references.

## Adopt now

### android/nowinandroid
Role: architecture and testing reference.

Use:
- unidirectional state flow;
- clear UI/domain/data boundaries;
- Compose-first patterns;
- testable state holders;
- dependency direction kept explicit.

Decision: reference architecture, not a copied codebase.

### airbnb/lottie-android
Role: lightweight cinematic UI animation.

Use:
- intro/reveal accents;
- loading/generation transitions;
- subtle premium motion where Compose primitives become cumbersome.

Decision: selected dependency, but add it only when the first production animation is implemented. Do not add unused dependencies.

### Fission-AI/OpenSpec
Role: spec-driven feature development.

Use:
- every large feature gets a short acceptance spec before implementation;
- specs define user-visible behavior, failure states, privacy rules and validation.

Decision: adopt the workflow convention through files under `docs/specs/`; no runtime dependency.

### appium/appium
Role: release-level end-to-end Android validation.

Use:
- smoke test install/launch;
- photo selection flow;
- scenario selection;
- timeline reveal;
- export/share;
- purchase/restore flow in a test environment.

Decision: defer until the main flow is stable. Compose UI tests remain the first line of validation.

## AI/media backend candidates

### comfyanonymous/ComfyUI
Role: reproducible image-generation workflow backend.

Why it fits ALT:
- graph-based pipelines;
- easy experimentation with identity/reference conditioning;
- self-hostable;
- suitable for multiple generated life chapters from one input photo.

Decision: preferred first self-hosted AI backend adapter. It remains an external service; it is not embedded in the Android app.

### invoke-ai/InvokeAI
Role: alternative image-generation backend.

Decision: fallback provider if its API/deployment is simpler for a target environment. Do not maintain two providers until the first one is proven.

### Tencent-Hunyuan/HunyuanVideo and calesthio/OpenMontage
Role: true generated video.

Decision: defer. ALT V1 should create video locally from generated stills, motion, text and transitions. True generative video is optional after image consistency, latency and cost are under control.

### facefusion/facefusion
Role: face replacement/identity processing.

Decision: not part of the core plan. Prefer reference-image conditioning and identity-preserving generation. This reduces privacy risk and avoids building the product around face swapping.

## Visual candidates intentionally deferred

### rive-app/rive-android
Excellent for interactive state-machine animation, but ALT should not ship both Rive and Lottie without a concrete need.

Decision: use Lottie first. Introduce Rive only if an interactive hero/reveal cannot be implemented cleanly with Compose + Lottie.

### google/filament
High-quality real-time 3D renderer.

Decision: reject for V1. ALT is primarily a cinematic 2D/media application; Filament would add complexity without improving the core viral loop.

### DanielMartinus/Konfetti
Useful particle effects.

Decision: optional polish only, never a core dependency.

## Production/tooling candidates

### getsentry/sentry
Potential crash/observability layer.

Decision: optional for production. Privacy and telemetry policy must be defined first.

### orhun/git-cliff
Release changelog automation.

Decision: adopt near beta/release, after versioning conventions stabilize.

### nektos/act
Local GitHub Actions runner.

Decision: not important for the current smartphone-first workflow. GitHub-hosted CI remains the source of truth.

## Missing capabilities in star-list

The current catalog does not contain strong direct entries for Android media export/encoding, Android billing, Room/DataStore, or Android-native image loading. For those areas ALT should prefer official AndroidX/Google APIs and narrowly scoped libraries rather than forcing a catalog match.

## Selection rule

A repository in `star-list` is not automatically a dependency. ALT adopts a tool only when it improves one of these priorities:

1. reliability;
2. real end-to-end functionality;
3. privacy/security;
4. maintainable architecture;
5. performance;
6. visual quality;
7. secondary feature speed.

Every added dependency must have a concrete use in production code or tests.
