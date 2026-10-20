# Garden of Time rebuild

Kairos now opens into a native Compose experience centered on daily discovery,
a word garden, time capsules and a personal space. The new welcome and interest
setup work without an account. Existing journal, profile, settings and study tools
remain reachable from Your space; they are retained rather than silently removed.
This is a rebuilt core experience, not a claim that every legacy feature has been
rewritten or independently validated.

## Visual language
- Warm paper, forest ink, sage and apricot; Lora reading voice and native sans controls.
- Original seed-and-orbit vector logo, including adaptive and monochrome launcher assets.
- Native painted-style seed/sprout and envelope illustrations; no network image dependency.
- Translucent floating navigation with bright edge and tonal depth. This is a cross-version
  glass treatment, not an OS-level refractive liquid-glass shader.
- Brief, interruptible press feedback; fading destinations and one-shot letter arrivals.
  Android's disabled animator setting removes spatial arrival/press motion.
- Main tested text pairs: ink/paper 10.60:1, muted/paper 5.42:1, forest/sage 6.05:1,
  ink/peach 8.08:1, night text/surface 11.25:1, night secondary/surface 7.74:1.

## Engines
Daily selection remains persisted per user and local calendar day. The new discovery
policy chooses unseen items first and the least recently seen item when recent
history covers the catalog. Interest/difficulty ranking still orders fresh choices.
Saved/unsaved feedback feeds future rankings. Foreground refresh handles midnight
rollover without restarting; it stops when the app is not resumed.

Recall has a separate deterministic queue: due reviews, then saved new words, then
other discoveries. Future reviews are excluded. Review results use existing SM-2
storage; the UI advances only after successful persistence. No remote AI is required.

## Capsules
- Future dates use the device's local calendar zone, including DST boundaries.
- Opening is gated by the timestamp, independent of a notification worker.
- Media is copied into private app storage while its picker grant is valid, capped
  at six files and 25 MB each, with MIME validation and partial-file cleanup.
- Draft attachment references and sealed completion state survive saved-state recreation.
- Delivered media opens through a temporary FileProvider read grant; images preview inline.
- Lists filter deleted letters and other profiles. Direct reads/reveal also check ownership.
- A notification scheduling failure cannot undo an already saved letter.

These are local letters. Uninstalling removes local data and attachments. Existing
backup/sync features have not been certified to preserve the new attachment files.
Date sealing is an app behavior, not a cryptographic or tamper-proof time lock.

## Word Prints
Three original 1080 × 1350 botanical compositions: Herbarium, Apricot and Night garden.
Preview and export use the same renderer. Long words shrink to fit; definitions wrap
with a bounded line count. System sharing receives a PNG and readable word/definition.
The FileProvider authority uses the actual application ID, including the debug suffix.
The API returns whether the share chooser opened, not whether a recipient received it.

## Skills and artwork provenance
Applied installed skills: `linkxzhou/design-taste` 1.1.0 and `cindypapa/apple-design` 1.0.1.
Image-generation skill was consulted for a richer watercolor hero. The authorized
`openai/gpt-image-2` gateway request was rejected with `insufficient_credits` before
an image was produced. No alternate provider or replacement paid request was made.
All shipped artwork in this change is original native vector/Canvas code.

## Review guide
Start with `ui/garden/GardenScreen.kt`, `GardenViewModel.kt`, `GardenTheme.kt`, and
`WordPrintRenderer.kt`. Date/attachment policy lives in `domain/futuremessage`;
recall and discovery policies are pure Kotlin with tests. Room schema 24 is an export
of the existing database version under its current package, not a destructive migration.

## Recommendations
Before a public release, validate the APK on physical low/mid-range Android devices,
TalkBack, large text, system dark/light, and disabled animations. Exercise backup and
restore before promising cross-device keepsakes. Add a user-controlled backup/export
flow for capsule media before treating capsules as an irreplaceable long-term archive.
Have quote source attribution editorially reviewed before expanding the catalog.
