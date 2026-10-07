# Play Store Data Safety Worksheet

This file documents the behavior of the current Android app so the Play Console form can be completed consistently. Re-check the Play Console definitions at submission time because category wording can change.

## Current app behavior

- The app requests only the Android `INTERNET` permission.
- Source photos are copied into app-private storage after user selection.
- Remote AI generation is optional and requires explicit consent.
- When enabled, the framed source image and generated ComfyUI workflow request are sent over HTTPS to the ComfyUI endpoint configured by the user.
- The current app does not operate a managed ALT backend.
- No advertising SDK, analytics SDK, or automatic remote crash-reporting SDK is present.
- Diagnostics remain local unless the user explicitly copies and shares them.
- Generated images, timeline history, settings, and diagnostics are stored locally.

## Data types to review in Play Console

### Photos and videos

Source photos can leave the device during user-initiated remote AI generation. The recipient is the ComfyUI server configured by the user. Do not assume this can be answered as "not collected" without checking the current Play definition for user-initiated transfer and ephemeral processing.

### App configuration / user-provided workflow

The custom ComfyUI workflow JSON and generated request parameters are sent to the same configured endpoint when generation runs. They are otherwise stored locally.

### Diagnostics

Local diagnostics contain technical event codes, exception types, internal ALT code locations, timestamps, and app version only. They are not transmitted automatically.

## Security and deletion facts

- HTTPS is mandatory for remote endpoints.
- Android cleartext traffic is disabled.
- Original photos and app data are stored in app-private storage.
- Users can clear AI settings, history, and diagnostics in-app where controls exist.
- Android app-data clearing or uninstall removes the remaining local app data.
- Remote retention is controlled by the configured ComfyUI server operator.

## Submission checklist

Before Play submission, verify the current Play Console wording for:
1. Whether a user-configured third-party ComfyUI transfer must be declared as collection or sharing.
2. Whether the remote processing qualifies as ephemeral processing.
3. Which "purpose" category best fits user-requested AI generation.
4. The public privacy-policy URL points to the latest `main` version of `PRIVACY.md`.
5. Store listing screenshots and descriptions do not imply that remote AI processing is on-device.
