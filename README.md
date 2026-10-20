# Kairos

**Keep something good.**

A local-first Android companion for a new word, a thoughtful pause, and a letter
to the person you’re becoming. Built with Kotlin, Jetpack Compose, Room and Hilt.

## The Garden of Time experience

- **Today:** a stable daily word and attributed thought. Save what speaks to you,
  explore the word garden, or leave a little piece of today for your future self.
- **Words:** searchable offline vocabulary, a saved collection, and recall that
  puts due memories first. Review results persist before the next card appears.
- **Word Prints:** original botanical 1080 × 1350 PNGs in Herbarium, Apricot and
  Night garden styles. Preview and export share one renderer and Kairos branding.
- **Capsules:** letters with a future opening date and up to six privately copied
  images, videos, recordings or PDFs (25 MB each). Keepsakes open with a temporary
  read grant. Letters remain date-gated even without a notification.
- **Your space:** the thought collection and access to existing journal, settings,
  profile and deeper learning tools.

## A little beginning

Start with a private local profile, choose interests and vocabulary difficulty,
and step into the garden. No provider key or network is needed for the core loop.
Setup commits the local catalog/profile before recording completion.

Daily discovery prefers unseen content, then the least recently seen item when the
recent catalog has been covered. Saved feedback influences future ranking. Recall
has its own due-first queue and uses the existing spaced-repetition storage.
Daily picks persist by user and device-local calendar date.

## Visual system

Warm paper, forest ink, sage and apricot. Bundled Lora for the reading voice,
native sans for controls, and an original seed-and-orbit identity. Code-native
painted-style illustrations work offline. Floating navigation uses translucent
surfaces; reading surfaces stay legible. Brief destination fades and interruptible
press/arrival motion respect Android’s disabled animator setting.

## Storage and compatibility

The Android identity remains `com.kairos.app` (`.debug` for debug builds). Existing
Room data and migration history are retained. The new core sits alongside existing
journal and advanced tools; those screens have not all been rewritten.

Capsules and attachments stay on this device. Uninstalling removes them. Existing
backup/sync paths have **not** been certified for the new attachment files. Date
sealing is app behavior, not a cryptographic time lock. Notification timing depends
on Android permissions, battery policy and device settings.

See [implementation and review notes](docs/garden/IMPLEMENTATION.md) for scope,
visual checks, artwork provenance, and public-release recommendations.

## Technical stack

| Area | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| State | ViewModel + StateFlow |
| Dependency injection | Hilt |
| Persistence | Room + SQLCipher, DataStore, encrypted preferences |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation Compose |
| Background work | WorkManager |
| Build | Gradle 8.9, JDK 17, Android SDK 35 |


## Meaningful progression

Kairos retains cosmetics, achievements, skill progression, missions, and streaks, but rewards evidence rather than app opens:

- **Clarity:** completed reflections and revisiting earlier writing.
- **Discipline:** successful recall and natural vocabulary use.
- **Courage:** writing, opening, and replying to future-self letters.
- **Consistency:** balanced practice across days, without streak multipliers or fake social ranks.

Achievement IDs and thresholds come from one canonical catalogue. Cosmetic banners are earned identity surfaces with distinct geometry; animation is reserved for the active preview/header to protect battery, accessibility, and visual calm.

## AI boundary

The app does **not** embed provider credentials in release builds. Legacy direct-provider integrations are available only for local debug development:

```properties
# local.properties — never commit
AI_API_KEY=your_local_development_key
OPENROUTER_API_KEY=your_optional_local_development_key
THERAPIST_API_KEY=your_optional_local_development_key
TTS_API_KEY=your_optional_local_development_key
```

Production generated features require an authenticated server gateway with quotas, abuse controls, schema validation, and provider isolation. Today, Learn, Reflect, and Library do not depend on that gateway.

## Build

Prerequisites:

- JDK 17+
- Android SDK 35
- Android Studio with current Compose tooling

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Run the repository boundary and trust checks without Gradle:

```bash
bash ./scripts/verify_kairos.sh
```

## CI and releases

`.github/workflows/android.yml` runs on every push (and manual dispatch): architecture checks, unit tests, lint, a signed release APK build, artifact upload, and a GitHub Release tagged `build-<sha>-<run>`.

Release signing uses the **public test keystore** checked in at `keystore/Kairos-release.jks` (alias `kairos`, password `kairos-public-test`). This identity is for personal/open-source test builds only — not for Play Store or production distribution. CI does not generate a fallback key at runtime.

## Compatibility invariants

Do not rename these current Kairos identifiers without a dedicated migration and upgrade test:

- `applicationId` and namespace (`com.kairos.app`);
- Room database and table names;
- preference and encrypted-storage keys;
- widget and pending-intent action identifiers;
- externally consumed deep links.

Visible branding and UI implementation names may migrate independently.

## Current migration status

The four primary destinations, local entry flow, onboarding, notification consent, recommendation feedback, release AI boundary, launcher identity, and adaptive navigation use the Kairos system.

Secondary experiment routes (Haven, Missions, Challenges, Collaborative messaging, Learning Paths, Deep Dive, Weekly Digest, Future Messages, Meditation, Micro Journal, Daily Ritual, Locker, Wrapped, and the legacy Home dashboard's Explore section) have been removed from the navigation graph so the app presents one honest product: Today, Learn, Reflect, Library, and Profile/Settings. Their source trees remain for reference until their data can be retired safely; they are unreachable from the UI.

## Verification

A complete verification run should include:

```bash
bash ./scripts/verify_kairos.sh
./gradlew clean :app:testDebugUnitTest :app:lintDebug :app:assembleDebug \
  --no-daemon --no-configuration-cache --stacktrace
```

Then test on at least:

- one compact phone in light and dark mode;
- one large-font configuration;
- one tablet or foldable width;
- Android 13+ notification permission flows;
- an upgrade install over an existing Kairos database.
