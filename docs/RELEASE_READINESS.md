# ALT — Android release readiness and Play internal track

## Artifact provenance and CI gates

There are two distinct workflows:

1. **Android CI** (`.github/workflows/android.yml`) runs for pull requests and pushes to `main`. It compiles and tests the Android app, runs debug and release lint, exercises real Android integration through the emulator, and produces an APK plus an AAB diagnostic artifact. Signing secrets are provided to this workflow **only for push events on `main`**; PR runs are intentionally unsigned even when signing secrets have been configured.
2. **Tagged Android Release** (`.github/workflows/tagged-release.yml`) is the publication candidate. It runs when a `vMAJOR.MINOR.PATCH` Git tag points to a commit reachable from `main`, checks a monotonically increasing version name/code against earlier release tags, runs existing JVM checks and release lint, exercises the Android integration suite **and** generates a baseline profile on emulator, then builds and strictly verifies a **signed** AAB. If integration or signing fails, no signed artifact is uploaded.

The release workflow uses the same `.github/scripts/run-instrumentation-tests.sh` script as ordinary Android CI; it records emulator diagnostics as an artifact even on failure. The Android 35 emulator is a verification gate, not evidence of compatibility with every supported Android version or every device.

**Do not upload** the `alt-release-aab-unsigned` artifact to Play as a publishable release. A green ordinary Android CI build is not equivalent to a signed tagged release, a Play upload, or a physical-device test.

## Signing prerequisites

Configure the **upload key** used for Google Play App Signing, not the Play-managed app signing private key, and retain secure offline backups. For a previously published package, reuse its registered upload certificate rather than creating an unrelated key.

Required GitHub Actions secrets for signed release workflows:

| Secret | Purpose |
| --- | --- |
| `ALT_ANDROID_KEYSTORE_BASE64` | Base64-encoded upload keystore |
| `ALT_KEYSTORE_PASSWORD` | Keystore password |
| `ALT_KEY_ALIAS` | Upload-key alias |
| `ALT_KEY_PASSWORD` | Upload-key password |

The main Android CI optionally uses `ALT_VERSION_CODE` and `ALT_VERSION_NAME` repository variables when signing. Tagged releases deliberately derive the version from the tag and Git history and do not use those variables. Check that the derived versionCode exceeds **the highest versionCode already uploaded to Play**, not only the previously tagged version. Changing a Git tag after publication cannot update a versionCode already accepted by Play.

**Security:** keep keystore bytes and passwords out of tracked files, PRs, logs, screenshots and diagnostic uploads. Environment scoping in a workflow reduces accidental exposure but is not a substitute for branch protection, minimal repository write access, reviews of workflow changes, and credential rotation when needed.

## Release candidate steps

1. Check that the exact `main` commit has a successful full Android CI run and no unexplained failures. Review the source changes and the Android integration diagnostics.
2. Decide the next semantic version and check all previously published Play versionCodes. Create a new immutable release tag `vMAJOR.MINOR.PATCH` on the validated `main` commit, and push the tag.
3. Inspect the **Tagged Android Release** workflow for that exact SHA. Confirm the JVM tests, release lint, instrumentation, baseline profile, signed AAB build and strict signature verification all succeeded. Download only the `alt-vMAJOR.MINOR.PATCH-signed-aab` artifact.
4. Inspect the signing certificate against the **registered upload certificate** in Play Console and verify the final `applicationId`, versionCode and versionName. Confirm the AAB is signed and that the versionCode is new to Play.
5. Upload the signed AAB to the **Play internal testing track**. Install it through Google Play on a real device and verify that an update from the previous installed Play version works without loss of local timelines or photos.
6. Manually exercise importing from the Android photo picker (including Google Photos / Drive providers where applicable), optional HTTPS ComfyUI consent and complete five-chapter generation, restart/restoration, JPEG gallery save/share, Media3 MP4 export, gallery save/share, cancellation/retry and cleanup after errors.
7. Review the current public `PRIVACY.md`, `docs/play-store-data-safety.md`, store screenshots, age/rating declarations and the configured third-party ComfyUI retention model. Do not claim that cloud processing is on-device.

## Known gaps

- No signed Play Store internal-track install/update has been performed by this repository's automated CI.
- No physical Android device or real HTTPS ComfyUI server is exercised by the existing GitHub Actions emulators.
- Signing credentials and Play Console access must be supplied by the release owner; successful unsigned CI alone cannot prove these are configured.
- Google Play's current form questions and upload/signing rules must be checked when actually submitting the release.

References: [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756), [uploading an Android App Bundle](https://developer.android.com/studio/publish/upload-bundle), [GitHub Actions secrets](https://docs.github.com/en/actions/concepts/security/secrets).
