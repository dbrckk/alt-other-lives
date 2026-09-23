# ALT — development plan

## Product target

ALT is a solo-first Android app that transforms one photo plus a “What if…?” scenario into a cinematic alternate-life timeline that is enjoyable privately and naturally shareable as vertical content.

Primary loop:

`photo -> scenario -> alternate timeline -> cinematic reveal -> 9:16 export -> share`

The app must remain useful without friends, followers, accounts or a built-in social network.

## Development rules

- A phase is not complete until its user-visible flow works end to end.
- Fix build/test blockers before adding features.
- Keep the Android client responsive even when generation is remote.
- Do not ship mocks, dead buttons or placeholder generation in a release build.
- Add dependencies only when they are immediately used.
- Preserve the original photo locally unless the user explicitly starts remote generation.
- Remote generation must expose progress, cancellation, retry and failure recovery.
- Every share/export must work without requiring a social account.
- Premium visual quality is part of acceptance, not a final cosmetic pass.

## Phase 0 — Stable engineering base

Goal: make the current prototype a reliable foundation.

Tasks:
- keep GitHub Actions green;
- split the current monolithic activity into packages for app shell, model, data and feature UI;
- introduce a single navigation/state model;
- centralize colors, typography, spacing, shapes and motion tokens;
- add unit tests for scenario/timeline generation;
- add Compose UI smoke tests for the local flow;
- add structured error states and no-op-safe back navigation;
- document build and release commands;
- adopt repository standards and maintain resumable project state.

Exit criteria:
- debug APK builds in CI;
- app launches;
- photo can be selected;
- scenario can be selected;
- local timeline renders without crash;
- core tests pass.

## Phase 1 — Complete local solo loop

Goal: a useful app even before remote AI generation exists.

Tasks:
- production photo picker with persisted read access when needed;
- photo crop/framing step optimized for portrait faces;
- scenario catalog with categories, featured scenarios and search;
- deterministic timeline engine with 4–6 chapters;
- scenario prompt metadata separated from UI copy;
- timeline history stored locally;
- delete/reset history and source photos;
- robust loading/empty/error screens.

Exit criteria:
- user can complete the full loop offline;
- restarting the app preserves finished local timelines;
- user can delete personal content.

## Phase 2 — Premium cinematic reveal

Goal: make the result feel like a product worth sharing.

Tasks:
- motion system using Compose first and Lottie where justified;
- chapter-to-chapter animated transitions;
- progressive text reveal;
- Ken Burns/parallax treatment for stills;
- haptic timing;
- optional local soundtrack;
- reduced-motion accessibility mode;
- polished loading/generation states;
- dark cinematic design system with high contrast and large imagery.

Exit criteria:
- reveal runs smoothly on a mid-range Android device;
- no jarring layout jumps;
- reduced-motion mode is usable;
- visual review passes every core screen.

## Phase 3 — Share renderer

Goal: every completed timeline can become high-quality vertical media.

Stage A:
- generate 1080x1920 share cards;
- watermark/brand treatment kept subtle;
- Android Sharesheet integration;
- save to device.

Stage B:
- render a short 9:16 MP4 locally from stills + motion + text + audio;
- progress notification;
- cancellation;
- deterministic output;
- recoverable export failures.

Exit criteria:
- exported image/video opens correctly outside ALT;
- export works without a social app installed;
- output is suitable for TikTok/Reels/Shorts/Stories.

## Phase 4 — AI alternate-life generation

Goal: replace repeated source-photo chapters with genuinely transformed scenes while preserving identity.

Architecture:
- define a `GenerationProvider` contract in the app;
- first external adapter targets a self-hosted ComfyUI workflow;
- generation request contains scenario, chapter, style and reference-image data;
- app never depends on a single model implementation;
- provider responses are cached per timeline;
- jobs survive navigation and temporary app backgrounding.

Pipeline:
1. validate source image;
2. build structured chapter prompts;
3. generate first keyframe;
4. generate later chapters with identity/style consistency;
5. run quality checks;
6. retry only failed chapters;
7. return compressed production assets.

Exit criteria:
- one real scenario produces multiple distinct, coherent chapter images;
- identity remains recognizably consistent;
- failure of one chapter does not destroy the entire timeline;
- source and generated assets can be deleted.

## Phase 5 — Retention without social dependency

Goal: create reasons to return while keeping the app solo-first.

Features:
- Daily What If;
- weekly alternate-life prompt;
- favorites;
- rare scenario drops;
- timeline collection;
- optional reminder notification;
- “remix this life” from a previous result;
- compare two of your own timelines.

Avoid:
- mandatory account creation;
- follower/friend systems;
- streak pressure that harms the simple experience.

Exit criteria:
- returning users have new content without needing another person.

## Phase 6 — Freemium monetization

Free:
- core scenarios;
- limited daily generations;
- standard export;
- sensible ads;
- rewarded ad for an extra generation where appropriate.

One-time premium purchase:
- permanent ad removal;
- premium scenarios/styles;
- higher export quality;
- additional daily generation allowance;
- premium reveal themes.

Implementation:
- Google Play Billing;
- entitlement persisted locally and restored from Play;
- purchase state never blocks access to owned content;
- ads must never interrupt the cinematic reveal itself.

Exit criteria:
- purchase, restore and offline entitlement behavior tested;
- free path remains fully understandable and usable.

## Phase 7 — Reliability, privacy and performance

Tasks:
- instrument crash reporting only after telemetry policy is defined;
- benchmark cold start, photo load, reveal and export;
- test memory pressure with large photos;
- downsample images early;
- encrypted handling for sensitive remote-generation tokens/config;
- explicit privacy screen explaining local vs remote processing;
- network retry/backoff;
- WorkManager for durable generation/export jobs;
- accessibility pass;
- Android low-memory/process-death recovery;
- Appium release smoke suite.

Exit criteria:
- no known blocker/high crash;
- large-photo path does not OOM common devices;
- process death during background work is recoverable.

## Phase 8 — Play Store release

Tasks:
- signed AAB;
- adaptive icon and launch assets;
- screenshots and store video;
- privacy policy;
- Data Safety form;
- content rating;
- billing product setup;
- internal testing track;
- closed beta;
- staged production rollout;
- changelog automation.

Exit criteria:
- install/update path verified from Play testing;
- release checklist is reproducible.

## Code organization

Start simple; do not prematurely create many Gradle modules.

Initial package boundaries:

```
com.alt.otherlives
├── app/
├── core/
│   ├── model/
│   ├── designsystem/
│   ├── media/
│   ├── data/
│   └── generation/
└── feature/
    ├── home/
    ├── photo/
    ├── scenarios/
    ├── timeline/
    ├── history/
    ├── share/
    └── premium/
```

Move boundaries into separate Gradle modules only when build time, ownership or dependency isolation provides a measurable benefit.

## Work order

Do not develop all phases in parallel.

Current strict order:

1. Phase 0 until CI is green.
2. Phase 1 until the complete local loop is stable.
3. Phase 3 Stage A share-card export.
4. Phase 2 premium reveal polish.
5. Phase 4 real AI generation for one scenario.
6. Phase 3 Stage B MP4 export.
7. Expand AI scenarios.
8. Retention.
9. Monetization.
10. Production hardening and release.

This order ensures that ALT becomes testable and shareable before expensive AI/video complexity is added.

## Definition of done for every feature

A feature is done only when:
- implementation is complete;
- loading/empty/error states exist;
- build passes;
- relevant unit/UI tests pass;
- manual flow is reproducible;
- no dead control remains;
- privacy implications are documented when personal media is involved;
- performance is acceptable on Android;
- project state and next priority are updated.
