# ALT Privacy Policy — Draft

_Last updated: 2026-09-26_

> This document is a product/legal draft for review before publication. Replace every bracketed placeholder before publishing it as the public Privacy Policy.

## Overview

ALT — See Your Other Lives is an Android application that lets a user choose a source photo, select an alternate-life scenario, optionally generate AI images, create a cinematic timeline, export 9:16 images or MP4 videos, and save local history.

ALT is designed to keep local content on the device by default. Remote AI generation is optional and requires an explicit user opt-in before the selected source photo can be uploaded to the configured remote AI server.

## Data stored on the device

ALT may store the following data locally on the Android device:

- the source photo imported by the user;
- generated alternate-life scene images;
- local timeline/history metadata;
- generation settings, including the configured AI server URL and workflow template;
- a generation seed used to maintain continuity between AI scenes;
- temporary share/export files;
- locally recorded AI-safety flags containing a timeline identifier, scenario identifier, reason and timestamp.

Android system backup is disabled for the ALT application.

## Source photos

A source photo selected by the user is imported into ALT's private app storage.

The original remains local unless the user explicitly enables remote AI generation and starts an AI generation request.

When remote AI generation is disabled, ALT does not upload the source photo for AI processing.

## Optional remote AI generation

ALT currently supports an advanced custom ComfyUI configuration.

Before remote generation is enabled, the user must explicitly confirm that they understand that their selected source photo will be uploaded to the configured remote ComfyUI server.

When AI generation is started, ALT may send:

- the selected source image;
- the generated text prompt for the selected scenario;
- the negative prompt;
- the generation seed;
- the configured workflow.

The operator of the configured remote ComfyUI server may process or retain these inputs according to that operator's own infrastructure and policies. ALT cannot control a third-party or user-operated server.

A future managed ALT backend must be documented separately here before release.

## Generated media

AI-generated scene images are downloaded to the device and stored in app-private storage.

Users can explicitly:

- share a generated 9:16 image;
- save an image to the Android media gallery;
- create a cinematic MP4;
- share the MP4;
- save the MP4 to the Android media gallery.

These actions are initiated by the user.

## AI-safety flags

ALT provides an in-app option to flag an unsafe AI generation.

The current implementation stores the following data locally:

- timeline identifier;
- scenario identifier;
- selected safety reason;
- timestamp.

The current local report does not include the user's source photo or generated images.

Before public launch, the production reporting backend and its retention policy must be documented in this section.

## Network security

Configured AI endpoints must use HTTPS.

ALT rejects plain HTTP ComfyUI endpoints.

## Data deletion

Users can delete an individual saved timeline or clear local history from the application.

ALT also removes unreferenced local source photos and generated scene media as part of its local cleanup process.

Clearing AI settings removes the locally stored ComfyUI configuration and remote-upload consent.

Deletion behavior for a future managed backend must be documented before launch.

## Analytics and crash reporting

ALT currently does not include a production analytics or crash-reporting service.

If analytics or crash reporting is added later, this policy must be updated before those services are enabled for users.

## Advertising

ALT currently does not include advertising SDKs.

## Children

[Add the intended minimum user age and any required child-safety language before publication.]

## Data controller / developer

Developer or company: [LEGAL ENTITY OR DEVELOPER NAME]

Contact: [PRIVACY CONTACT EMAIL]

Country: [COUNTRY]

Public privacy-policy URL: [HTTPS URL]

## Changes to this policy

If ALT changes how it processes personal data, this policy should be updated before the changed processing is released to users.
