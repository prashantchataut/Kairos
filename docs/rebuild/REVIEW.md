# Painted Kairos rebuild

## What changed

Today, Learn, Reflect, the keeping shelf, onboarding and capsule archive were
rebuilt in Compose. A pond-blue / leaf-green / rose palette, rounded lettering,
Lora reading voice, translucent controls and a native painted seed companion
replace the prior royal-blue dashboard. The seed mark is shared across the app,
launcher and splash. The existing journal editor, profile/settings, learning
session, database, authentication and recommendation infrastructure remain.
This is a substantial product rebuild, not a claim that every legacy source file
was rewritten.

Capsules now have active write/archive/reveal/reply routes. Photo/video selections
retain document permissions; reveal displays photos and opens video/audio using
Android viewers. Both reveal and reply enforce delivery time. Custom calendar
choices arrive at 09:00 device-local time. The archive refreshes delivery while
open; local data is preserved without a destructive schema migration.

The learning-session mode route now sends the selected enum rather than an
escaped string. Daily content rolls over across midnight and failed saves no
longer claim success. Existing curated catalog, daily stable selections,
personalization and spaced repetition are retained.

Word Garden is an original, bundled, offline recall game with nine words,
flowers, saved progress, feedback and reduced-motion transitions. The external
library downloads Shattered Pixel Dungeon v4.0.2 from its official GitHub release
through Android DownloadManager, with status/cancel actions. Pokémon fan games
are creator-discovery links, not a mirrored download collection or emulator.

## Design review

Six rebuilt main screens use zero literal hex colors and zero explicit `sp`
font sizes; they consume Material theme and typography. The measured light-theme
text pairs are:

| Foreground / background | Contrast |
| --- | --- |
| Main ink / background | 11.95:1 |
| Secondary text / background | 5.63:1 |
| White / pond blue | 6.55:1 |
| Deep ink / blue wash | 9.16:1 |

These measurements do not establish contrast compliance for every legacy view.
Android device screenshots, large-font clipping, TalkBack traversal and actual
frame timing remain device-validation items. No emulator/device was available
in this session. Never infer smoothness from compilation alone.

Skills applied: installed `linkxzhou/design-taste`, skills.sh
`anthropics/skills/frontend-design`, and `ivangdavila/android`.
The imagegen guidance was consulted for artwork. A gateway request for original
watercolor/acrylic artwork failed with HTTP 402 insufficient_credits; no raster
art was generated. Native Canvas art ships instead. The stable failed-operation
record and exact prompt are retained in task/art for recovery without creating
replacement paid work.

## Practical limits and recommendations

- Attachment permission survives process/device restarts but cannot preserve a
  file deleted by its owner or revoked by its provider. Keep originals. Existing
  capsules with expired temporary grants cannot be repaired automatically.
- Letters are stored on this installation. Uninstalling destroys local memories;
  Google sign-in is not a cloud-backup implementation.
- Capsule drafts remain in the current ViewModel, not a durable draft store.
  Add encrypted autosave/export before positioning this as archival storage.
- Existing quote attributions need an editorial provenance pass; optional AI
  content generation must not be treated as verified historical authorship.
- Game catalog is deliberately explicit. Add only creator-approved Android
  downloads with release metadata. No ROMs, emulator or Pokemon binaries ship.
- Downloaded APK installation is handled separately by Android. Kairos does not
  request package-install privileges or install automatically.
- External game availability depends on the upstream release; status errors are
  exposed rather than presenting downloads as completed on enqueue.
- Retained legacy feature screens inherit tokens but were not all redesigned.
