# Kairos

**A word, a thought and a small good thing, written for you every day.**

Kairos (καιρός) is Greek for *the right moment*. Each day it gives you one word worth knowing,
one thought worth keeping and one small good thing to do. All of it is written by an AI curator
that learns from what you save, skip and practise. There's also a time capsule for letters to
future you.

<p><img src="docs/screenshots/overview.jpg" /></p>

<p>
<img src="docs/screenshots/08_onboarding_2.jpg" width="160" />
<img src="docs/screenshots/04_word.jpg" width="160" />
<img src="docs/screenshots/05d_letter_ready.jpg" width="160" />
<img src="docs/screenshots/07_curator.jpg" width="160" />
<img src="docs/screenshots/02_today_dark.jpg" width="160" />
</p>

The onboarding wake-up, frame by frame: the bloom unfolds and becomes Kai.

<p><img src="docs/screenshots/onboarding_wake_frames.jpg" /></p>

Share cards, each drawn from one of the references in issue #606:

<p><img src="docs/screenshots/10_cards_word.jpg" width="380" /> <img src="docs/screenshots/10b_cards_quote.jpg" width="380" /></p>

## Nothing is bundled: the curator writes everything

Kairos ships with **no word lists and no quote lists**. Every word, quote, small good thing and
capsule prompt is generated for one person, in real time, through [OpenRouter](https://openrouter.ai).
Each person adds their own key in the app (onboarding or You → Curator). It's encrypted on the
device and only ever sent to OpenRouter.

How the recommendation loop works:

1. **Signals.** Kairos records what you save, what you mark *too easy*, *too hard*, *more like this*
   or *not for me*, which words you keep forgetting in practice, which authors and themes you keep,
   your recent moods, your interests and level, and the time of day.
2. **Curator** (`engine/Curator.kt`). Turns those signals into a profile and asks the model
   (structured JSON output) for a batch of new words, quotes, small good things and prompts.
   Anything you've already seen goes into an "avoid" list.
3. **Validation.** Every item is checked before it's stored. Invented-looking words, definitions
   that use the word itself, missing fields, unknown categories, duplicates and bad attributions
   are dropped.
4. **Picker** (`engine/DailyEngine.kt`). Each day it chooses the best word and quote from your
   personal pool, deterministically, and says why it chose them ("You keep saving quiet weather
   words").
5. **Refill** (`work/RefillWorker`). A background job keeps a few unseen items ready, so Today
   opens instantly and works offline.
6. **Ask for words about…** in Learn generates a focused batch on any topic, live.

Three "too easy" votes in a row move your level up (and three "too hard" move it down). Saving a
word enrols it in SM-2 spaced repetition.

## Screens

| | |
|---|---|
| **Onboarding** | The logo wakes up: the bloom's petals unfold and it becomes Kai, the curator, who opens their eyes. Kai and three friends stay on screen and move between steps while a white cloud field rises and falls. Giant dashed-to-solid pills for topics and themes, a word wheel and adaptive slider for level, a key ritual to wake Kai, and an aurora finale that says what Kai learned about you. |
| **Today** | Greeting with Kai in today's mood, an arched week strip with a mood shape for each day, and a swipeable deck of tilted cards: the word, the thought and the small good thing. `←  Save  →` controls with a heart burst, Teach Kai reactions, a mood check-in through the adaptive slider (Kai changes shape as you slide), a review nudge and a peek at the capsule. |
| **Learn** | "Ask Kai for words about…", a practice banner, and word tiles that each carry a little gouache painting seeded from the word. |
| **Word / thought pages** | A full-bleed painting behind the word; a grainy glow with a smiling sun behind a thought. A flower grows as you remember a word. |
| **Practice** | Grainy, deep-coloured flip cards fanned over each other with an orbit ring, petal progress, ✦ points, grade buttons with Kai's faces, and a garden at the end. |
| **Card studio** | Seven share cards drawn from the references: Poster, Bold, Swiss, Glow, Bloom, Painted and Paper; five hues, three formats, exported at 1080px. |
| **Time capsule** | A night sky with glass folders, letters peeking out and a mood sticker on each. Compose on cream paper with an "open in" slider; seal it and a wax bloom stamps it shut while dandelion seeds drift away; open it and the folder tips toward you and the letter slides out. |
| **You** | Kai, stats, your garden (a flower for every word you kept), four weeks of mood shapes, and settings with face toggles. |

## Design: playful sky

The redesign follows the references in issue #606 and the house skill
`.agents/skills/kairos-playful-sky`, which ranks those references above every external design skill.

- **Look:** a bright sky that melts into a cool white canvas, crisp glass, one cobalt action colour,
  and a candy set (sun, coral, bubblegum, mint, lilac, sky) for characters, tiles and celebrations.
  Grain over illustration. The capsule lives under a night sky.
- **Kai:** the curator has a face. Kai is a soft character whose outline is a polar function, so
  every mood (radiant, good, okay, low, rough) is a shape it can morph into. Kai blinks, breathes,
  sleeps without a key and reacts to what you do.
- **Type:** Bricolage Grotesque for the interface and the big playful headlines, Fraunces (soft and a
  little wonky) for words, quotes and letters.
- **Motion:** one signature spring with a little overshoot for characters and pop-ins, quick and quiet
  springs for navigation, and the celebrations saved for rare moments. *Calmer motion* (or the
  system animator scale) keeps expressions and fades but drops travel, bounce and particles.
- **Adaptive slider:** a row of bars that swell around a stretchy thumb and take the colour of the
  value, with a bubble whose content adapts (a word for the level, Kai's face for a mood, a date for a
  letter).
- **Logo:** the moment bloom. The eight rays became eight petals around a golden centre; one coral
  petal points just past twelve like a clock hand, because chronos is clock time and kairos is the one
  moment that matters. On the icon it rises out of clouds like a sunrise.

## Building

```bash
./gradlew :app:assembleDebug                 # debug APK
./gradlew :app:testDebugUnitTest             # curator, OpenRouter client, SM-2, picker, screenshots
./gradlew :app:recordRoborazziDebug          # re-render screenshots into app/screenshots
```

Kotlin 2.0, Jetpack Compose, Room, DataStore, WorkManager, OkHttp, Haze, Coil and
security-crypto. All illustration (Kai, flowers, paintings, clouds, the bloom) is drawn in code. Min SDK 26, target 35. Dependencies are wired by hand in `KairosApp.kt`.

```
app/src/main/java/com/kairos/app/
  data/ai      OpenRouter client, encrypted key store
  data/db      Room entities and DAOs
  data/repo    KairosRepository (curator loop, today, feedback), CapsuleRepository
  engine/      Curator (prompt, schema, validation), DailyEngine, Srs, Mood, Topics
  work/        refill, daily reminder, capsule unlock, notifications
  ui/theme     palette, type, motion
  ui/art       Kai, the bloom, flowers, paintings, clouds, aurora, grain, bursts
  ui/components  design-system kit and icons
  ui/nav       shell, bottom bar, navigation
  ui/feature   today, learn, detail, practice, studio, capsule, you, onboarding
```
