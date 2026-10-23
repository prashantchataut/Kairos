---
name: kairos-playful-sky
description: >-
  The Kairos house skill: art direction, motion language and Compose recipes for the "playful sky"
  redesign. Use for any UI, UX, animation, illustration, icon, share-card or copy work in the Kairos
  Android app (Jetpack Compose). It ranks the owner's own references (issue #606) above every
  external skill, then layers the best of taste-skill, impeccable, Emil Kowalski's animate,
  LottieFiles motion-design, design-motion-principles and Chris Banes' compose-animations on top.
  Trigger on: redesign, new screen, "make it feel alive", onboarding, animation, mascot, logo, icon,
  share card, slider, celebration, empty state, or any review of how Kairos looks or moves.
---

# Kairos: playful sky

Kairos (καιρός) is *the right moment*. Every day it gives one person a word, a thought and a small
good thing, written by an AI curator that learns from them, plus a time capsule for letters to future
you. The app must feel **hopeful and a little playful**: a bright morning, not a productivity tool.

> Owner, in their own words: "I want the app to be a little playful and hopeful. I don't want it to
> look too serious and too minimal. I also don't want those productivity to-do list type AI slop."

## 0. Priority order (read this first)

When two sources disagree, the higher one wins. Always.

1. **The owner's references** — `references/references.md` (34 images and videos from issue #606,
   each noted in detail) and the contact sheet `docs/design/references-sheet.jpg`. If a reference
   shows it, it is allowed, even if a generic skill bans it.
2. **This skill** — the Kairos direction, tokens, components and motion below.
3. **Motion skills** — `motion-design` (LottieFiles), `design-motion-principles`, `animate`,
   `compose-animations`, `compose-performance`.
4. **Taste and craft skills** — `design-taste-frontend`, `impeccable`, `frontend-design`,
   `redesign-existing-projects`, `brandkit`.

What that means in practice:

| Generic rule (taste-skill v2 house rules) | Kairos verdict |
| --- | --- |
| No glass, no backdrop blur | **Overridden.** Frosted glass on sky is core (r01, r05, r06, r11, r12). Keep it crisp: white 18–35% fill, a top-lit rim, never muddy. |
| No gradients | **Overridden** for sky, night, grain-gradient art and share cards (r01, r06, r24, r33). Still banned: gradient *text* on body copy, rainbow buttons. |
| No emoji, no decorative shapes | **Overridden** for characters, stickers, petals and confetti (r03, r07, r09, r11). They carry meaning; they are not filler. |
| No shadows on things at rest | **Softened.** Floating cards in a deck cast one wide soft shadow (r04, r12, r14). Flat tiles on the canvas do not. |
| No eyebrows, no index numbers, no monospace labels, no tiny tracked caps | **Kept.** |
| No rows of identical cards, no icon-topped feature cards | **Kept.** This is exactly the "to-do list AI slop" the owner hates. |
| Two typefaces at most | **Kept.** Bricolage Grotesque + Fraunces. |
| Headlines two lines or fewer | **Kept.** |

## 1. Design read

*Reading this as: a daily companion app for curious, hopeful people, with a playful-sky language
(bright sky glass + characters with eyes + grainy illustration + stacked tilted cards), leaning toward
Jetpack Compose Canvas art, spring motion and two expressive variable typefaces.*

Three words: **bright, tender, alive.**

## 2. Brand

### The mark: the moment bloom

The owner likes the eight-ray logo and its wordmark but called the icon "trash". The fix keeps the
eight rays and gives them a story:

- The eight rays become **eight soft petals** (teardrops, r17) around a warm golden centre: a sun that
  is also a flower. Morning (hope) and growth (learning) in one shape.
- **One petal is coral.** It sits at one o'clock, like the hand of a clock. Chronos is clock time;
  Kairos is the *one* moment that matters. The coral petal is "your moment today". On the icon it is
  the only warm colour, so the eye lands on it.
- On the launcher icon the bloom **rises from a soft cloud** on a cobalt-to-sky gradient: a sunrise.
- In the app the bloom **is the mascot**. Kai (below) is the mark with eyes. The logo literally wakes
  up during onboarding, which makes the brand personal: the thing on your home screen is the friend
  who writes your words.

Drawn in code (`ui/brand/Bloom.kt`) so it scales from 16dp to the 432px icon foreground with no
assets. The wordmark stays "Kairos" in the word face (Fraunces), next to the bloom.

### Kai, the curator

A soft blob character in the r03 / r02 / r04 / r09 family: a body whose outline is a **polar
function** (lobes, bumps, droop), two tall oval white eyes (no pupils, r03) or closed happy arcs
(r09), an optional small mouth. Kai is the curator's face everywhere the AI is involved.

| Mood | Body | Colour | Face |
| --- | --- | --- | --- |
| Radiant | sun, 8 short lobes | Sun yellow | closed happy arcs, big smile |
| Good | flower, 6 round lobes | Mint | open eyes, small smile |
| Okay | pebble, 3 gentle bumps | Sky | open eyes, flat mouth |
| Low | cloud, flat bottom, droop | Lilac | half-closed eyes |
| Rough | wobbly, 5 uneven lobes | Bubblegum | eyes looking down, tiny frown |
| Curator (default) | bloom, 8 lobes | Cobalt | open eyes, blinks |

Every shape is the same function with different parameters, so **any shape morphs into any other**
with one spring (see §8.1). Kai breathes (scale 1.00 ↔ 1.03, 3.2s sine), blinks every 3–6s and looks
toward the thing the user is touching.

### Voice

Warm, short, specific, a little funny (r15: "I Don't Know", "Ahh... for Routine"). Sentence case.
Kai speaks in first person on its own surfaces ("I'll write you something about the sea"). Never
corporate, never exclamation-mark spam, never "Unlock your potential".

## 3. Colour

One sky, one ink, one cobalt, and a **candy set** for characters, tiles and celebrations.

| Token | Light | Dark | Use |
| --- | --- | --- | --- |
| skyTop / skyMid / skyFade | #3D7BFA / #8EB9FF / #E9F2FF | #2148C9 / #1B347E / #0F1833 | hero sky gradient |
| canvas | #F6F8FE | #0A0F1F | page ground |
| surface / surfaceSoft | #FFFFFF / #EEF2FB | #141C31 / #1B2440 | cards, fields |
| ink / inkSoft / inkFaint | #0F1A3A / #55607A / #9AA3B8 | #EFF3FC / #A8B2C8 / #6A7590 | text |
| cobalt (accent) | #2F5BFF | #7898FF | the one action colour |
| night | #1D4BE0 → #0B1440 → #05071A | same | time capsule |
| sun | #FFC83D | | radiant, highlights, the golden centre |
| coral | #FF6A4D | | the moment petal, warmth, hearts |
| bubblegum | #FF79B5 | | playful accents, rough mood |
| mint | #2ECF8B | | good mood, growth, success |
| lilac | #A98CFF | | low mood, thoughts |
| skyCandy | #4DA3FF | | okay mood, words |

Each candy colour has a pale **tint** (tiles) and a deep **ink** (text on that tint) in `Palette`.
Ratio on a screen: ~70% sky/canvas, ~20% white glass and surfaces, ≤10% candy. Candy is for
characters, stickers, tile tints, petals and confetti, never for body text.

## 4. Type

| Role | Face | Spec |
| --- | --- | --- |
| Display (onboarding, big moments, screen titles) | **Bricolage Grotesque** ExtraBold | 34–56sp, tracking −0.03em, leading 0.95, width 85–100 |
| Interface | Bricolage Grotesque | 12–19sp, 400–600, optical size on |
| The word, quotes, letters | **Fraunces** | word 56–84sp opsz 144, SOFT 100, WONK 1; quotes italic 22–30sp |

Two or three sizes do the work on each screen: one very large, one small. Words of the day are the
largest thing on screen. Never set body copy in grey lighter than inkSoft.

## 5. Surfaces, depth and texture

- **Sky hero** fading to canvas (r01), with 2–3 soft cloud shapes drifting slowly behind the headline
  (r03). Clouds are the recurring motif: text rests on clouds.
- **Glass** on sky: white 18–35%, 1dp rim brighter at the top, radius 24–32. Glass on canvas uses Haze
  blur (bottom bar, sheets).
- **Decks**: the main object of a screen is a **stack of 2–3 tilted cards** (r04, r05, r12, r13, r14),
  front card upright, the ones behind rotated −4°/+3° and offset 10–14dp so their edges peek.
- **Grain**: a tiled noise texture at 6–12% over illustration, share cards and the night sky (r10,
  r18, r24, r25, r34). Never over body text areas at more than 6%.
- **Stickers**: icons and characters on a white die-cut rim with a soft shadow (r07, r11).
- **Folders** for the time capsule (r06, r11, r31): glass folders, papers peeking out of the top in
  the status colour, a mood sticker on the front.

Radii: chip 999 (pill), tile 22, card 28, deck card 32, sheet 32.

## 6. Composition patterns (use these, not lists)

| Pattern | Reference | Where |
| --- | --- | --- |
| Arched week strip, centre day lowest and boldest, mood shapes as day badges | r01, r09 | Today |
| Swipeable deck: word / thought / small good thing | r04, r12, r14 | Today |
| Huge fading date or title, second line dissolving into the background | r06, r12 | Capsule, letter |
| Glass folder grid with peeking papers and stickers | r06, r11 | Capsule |
| Dashed-to-solid giant pills with avatars, staggered left/right | r15 | Interests |
| Word wheel: centre word big, neighbours faded above and below | r02 | Level picker |
| Aurora "getting to know you" + reflected sentences + orb greeting | r32 | Onboarding finale |
| Painterly brushstroke background seeded per word | r19 | Word page, word tiles |
| Fanned, textured practice cards with an orbit ring and a ✦ points chip | r07 | Practice |
| Garden of flowers drawn stem-first, one per kept word | r28 | You, celebrations |
| Big reaction headline with ghost text behind ("Nice!" over "7 days") | r09 | Mood, practice summary |

**Banned in Kairos** (the "to-do list AI slop" list): checkbox rows as the main UI, stacks of
identical white cards with a title and a chevron, KPI tiles with a number and a label as decoration,
progress bars as the hero, Material-default switches and sliders, icons in tinted rounded squares in a
list, grey-on-grey settings pages, centred everything.

## 7. Motion

Personality: **Playful** for characters and celebrations, **Premium-soft** for navigation and
reading. One signature spring for 80% of movement.

| Token | Spec | Use |
| --- | --- | --- |
| `Motion.bloom()` | spring damping 0.58, stiffness 380 (≈10% overshoot) | pop-ins, characters, stickers, success |
| `Motion.soft()` | spring damping 0.85, stiffness 380 | layout, sliding indicators, decks |
| `Motion.press()` | spring damping 0.6, stiffness 900 | press-down 0.94, release overshoot |
| `Motion.settle()` | spring no-bounce, medium-low | reveals, page content |
| `Motion.Enter` | cubic (0.05, 0.7, 0.1, 1) | entrances with tween |
| `Motion.Out` | cubic (0.23, 1, 0.32, 1) | strong ease-out for UI |
| Durations | quick 140 · standard 280 · slow 520 · reveal 900 ms | |
| Stagger | 45–70ms per item, total ≤ 500ms | |

**Frequency gate** (Emil): tab switches and list navigation stay quick and quiet (≤ 220ms, no bounce).
Occasional actions (save, check in, open a letter) get a short expressive beat. Rare moments
(onboarding, connecting the key, sealing a letter, finishing practice, first word) get the full
celebration budget.

**Three layers** (LottieFiles): every signature moment has a primary action, a secondary echo
(shadow, sticker, text) and an ambient layer (clouds, grain shimmer, breathing).

**Signature moments** (build all of these, never skip):

1. **Logo wakes up** (onboarding 1): bloom petals unfold one by one (stagger 50ms, bloom spring),
   centre pops, eyes blink open, Kai bounces, friends pop in around it with confetti squiggles (r03).
2. **Hero persists across onboarding pages**: Kai moves, scales and morphs while text slides (r03).
3. **Interest pills**: dashed outline draws solid, fill floods from the tap point, + rotates into −,
   pill pops (bloom), Kai reacts (r15).
4. **Adaptive slider** (§8.4) everywhere a value is chosen.
5. **Key connect**: key glides into a keyhole on Kai's glass card, turns 90°, lock pops open, petals
   burst, check draws, Kai's eyes light up. Failure: firm shake (no overshoot), Kai looks down.
6. **Getting to know you** (r32): aurora rises, three sentences about the person fade in one by one,
   the orb/Kai grows in the centre, "Hi, {name}." The first word drops into the deck.
7. **Save**: heart pops (scale 0.6 → 1.15 → 1), six petals burst, a small sprout appears on the Learn
   tab icon.
8. **Deck swipe**: card follows the finger with rotation (x / 18°), flings off with velocity, slides
   to the back, the next card rises (r32 deck).
9. **Mood check-in**: Kai morphs to the mood's shape and colour, day badge in the week strip pops.
10. **Seal a letter**: letter slides into the folder, flap closes, wax bloom stamps, dandelion seeds
    drift up and away (r25), folder flies to the date.
11. **Open a letter**: folder lifts and flips in 3D (r31), letter slides out, confetti.
12. **Practice done**: big "Nice!" reaction headline over ghost text, a flower grows in the garden.

**Reduced motion** (`K.calm`, also when the system animator scale is 0): keep fades and colour, drop
travel, bounce, particles and ambient loops. Characters still change expression, instantly.

## 8. Compose recipes

### 8.1 Polar blob (Kai and morphing shapes)

```kotlin
data class BlobShape(val lobes: Float, val depth: Float, val droop: Float, val wobble: Float, val phase: Float)
// radius(θ) = 1 + depth·cos(lobes·θ + phase) + wobble·sin(3θ + 1.3) − droop·max(0, sin θ)²
// Sample 96 points, build a smooth closed path with quadratic midpoints.
// Morph = lerp every field, driven by one Animatable<Float> with Motion.bloom().
```

### 8.2 Grain

Generate one 128×128 noise `ImageBitmap` once (`remember`), wrap it in an `ImageShader` with
`TileMode.Repeated`, and draw it with `ShaderBrush` at 6–12% alpha. Works inside the share-card
`GraphicsLayer` export.

### 8.3 Growth (r28)

Stem: build a `Path`, measure it with `PathMeasure`, draw `getSegment(0, len·t)` with a round cap.
Petals: capsules rotated around the tip, each filled with a `Brush.sweepGradient` (pink → violet,
cyan → lilac, yellow → orange) and popped with `Motion.bloom()` after the stem reaches them. Seed
everything from the word id so a word always grows the same flower.

### 8.4 Adaptive slider

`KairosSlider` replaces every Material slider.

- Track is a row of 24–40 rounded bars (r04). Bars near the thumb grow (Gaussian, σ ≈ 2.2 bars) and
  take the colour of the current value; far bars stay pale.
- The thumb is a glossy pill that **stretches with drag velocity** (scaleX up to 1.25, scaleY down to
  0.85) and recovers with `Motion.bloom()`.
- **Adaptive:** the label above the thumb, the colour and an optional live preview (`content` slot)
  adapt to the value: a word example for the level, Kai's face for mood, a date for the capsule.
- Stepped mode: haptic tick and a tiny bar ripple at each step; snaps with `Motion.soft()`.
- Accessible: `progressBarRangeInfo`, `setProgress` semantics, 48dp touch height, keyboard steps.

### 8.5 Bursts

`Burst` draws N particles (petals, dots, squiggles, hearts) from an origin with random angle, speed
and spin, gravity 900dp/s², fade over 700–1100ms. Drive it with one `Animatable` from 0 to 1. Never
run more than one burst at once on a screen.

### 8.6 Implementation rules

- Animate in the draw phase (`graphicsLayer { }`, `drawBehind`, `drawWithCache`) for anything per
  frame; never pass frame-rate state into composition parameters.
- `AnimatedContent` renders from the lambda target. `AnimatedVisibility` when content must leave.
- Every infinite loop checks `K.calm` and pauses.
- Seed art from stable ids so it never flickers between recompositions.

## 9. Share cards

Templates (each 360dp artboard, exported at 1080px):

| Template | Reference | Recipe |
| --- | --- | --- |
| Poster | r18 | grainy blurred sky→meadow gradient, dictionary header top-left, giant word bottom-left, tiny pixel flower |
| Bold | r22 | one saturated colour, white grotesk lowercase, lines staggered to different indents |
| Swiss | r23 | electric blue, two white organic shapes almost touching, a yellow seed dot, small quote top-left, attribution large bottom-left |
| Glow | r24 | periwinkle → peach stipple gradient, a smiling sun, white text |
| Bloom | r16, r17 | warm paper, an inflated gradient flower, small centred text |
| Painted | r19 | brushstroke flowers seeded from the text, centred white Fraunces |
| Paper | r08 | classic dictionary page, kept for people who like quiet |

## 10. Screen blueprints

- **Onboarding**: 5 beats: wake-up (logo blooms into Kai) · what Kairos gives (three friends: word,
  thought, small good thing) · interests (r15 pills) · level (word wheel + adaptive slider) · name and
  key (Kai holds the key) → finale (r32 aurora + orb greeting). Clouds behind every headline.
- **Today**: sky hero with greeting and Kai avatar, arched week strip with mood badges, the deck
  (word / thought / small good thing), action trio (listen · save · share), "teach Kai" reactions,
  mood check-in with Kai morphing, capsule folder peek, review nudge as a fanned mini-deck.
- **Learn**: big title, "Ask Kai for words about…" glass box, review deck banner, masonry of word tiles
  with painted tops and quote tiles in Fraunces.
- **Word page**: painted hero (r19), giant white word, IPA pill with speaker, glass actions, meaning,
  sentence, origin, practice state, teach Kai.
- **Capsule**: night gradient, "Letters to / future you" huge with the second line dissolving (r06),
  ready letters glowing, folder grid, opened letters.
- **Practice**: fanned textured cards, ✦ points chip, flip, quiz, big reaction summary + garden growth.
- **You**: Kai and name, garden of kept words, four weeks of mood shapes, settings in two calm groups.
- **Curator**: Kai with a key on glass, the key ritual, model list as a word-wheel style picker.

## 11. Where things live

| Thing | File |
| --- | --- |
| Palette, candy, tints, night | `ui/theme/Tokens.kt` |
| Type (Bricolage, Fraunces) | `ui/theme/Type.kt` |
| Motion tokens (bloom, soft, press, settle, curves) | `ui/theme/Theme.kt` |
| Kai, looks, mood mapping, sleeping z's | `ui/art/Kai.kt` |
| The moment bloom (mark, icon geometry) | `ui/art/Bloom.kt`, `scripts/generate_icons.py` |
| Grain, bursts, flowers and gardens, paintings, clouds, aurora | `ui/art/*.kt` |
| Adaptive slider | `ui/components/KairosSlider.kt` |
| Giant dashed-to-solid pills | `ui/components/Pills.kt` |
| Key ritual | `ui/feature/you/KeyRitual.kt` |
| Glass folders, night sky, attachments | `ui/feature/capsule/CapsuleArt.kt` |
| Share-card templates | `ui/feature/studio/Cards.kt` |
| Screenshot tests (screens, onboarding frames, design sheet, icons) | `app/src/test/.../screens/` |

## 12. Verification loop

1. Build: `./gradlew :app:compileDebugKotlin`.
2. Render: `./gradlew :app:recordRoborazziDebug` (screens over seeded curator output, light and dark).
3. Look at every PNG. Compare against the references it claims to follow. Fix, re-render.
4. For motion, capture frames at fixed clock times (`compose.mainClock.autoAdvance = false`,
   `advanceTimeBy`) and inspect the sequence; play springs at 4× in your head: does it land, does it
   overshoot the right amount, does it end still?
5. Tests: `./gradlew :app:testDebugUnitTest`.

Nothing ships that has not been looked at.

## 13. Round two (owner feedback, October 2026)

- The painted gouache look is banned: the owner found it AI-made. Use typography as the picture
  (specimen heroes, ghost words, mixed type tiles) and flat drawn art.
- The logo is always the single-colour bloom (no coral petal, no gold centre).
- Share cards: 16 templates from the references; every text block lives in its own zone and uses
  `FitText`. Never place text over art unless the template is built for it (Leaf, Ring, Envelope),
  and then only inside the shape's clear area. Check Pin, Story and Square.
- Writing voice: modern, specific, warm; comfort, rest, love, grief, humour and quiet hope, not only
  motivation. Mix real quotes (with sources), original lines and proverbs. Words include borrowed
  concept words (Greek and Latin first, e.g. akrasia). See `engine/Curator.kt` and its `Slop` list.
- Motion signatures added: kinetic headlines (`KineticText`), the mark-to-Kai morph with a light
  ring, squash and stretch on moving characters, a drifting sparkle field.

## 14. Round three (owner feedback, October 2026)

Trending skills consulted again for this round: Emil Kowalski's *animate* (interruption, springs for
gestures, exit the way you entered), LottieFiles *motion-design* (three motion layers, choreography),
*design-motion-principles* (frequency gate; Jakub for production polish, Jhey for delight).

- **Product shape:** Today (one of each), **Feed** (endless reel), Capsule, You. The library is a
  sub-page of the Feed. Play is a game reached from Today and the + sheet.
- **Feed rules:** one card per page, inside a safe frame clear of header, rail and bar; each item
  always wears the same design (chosen from its key); double tap to keep; the rail is Keep, Share,
  Less. Signals are keeps, shares, "less" and dwell time (under 1.2 s ignored, capped at 60 s); the
  queue re-ranks every five pages. A breathing pause appears after at least five reads.
  Never productivity framing: no streak pressure, no counts of "done".
- **Cards:** every design has layouts for square, pin and story (`CardShape`). Use `ArtAndText` for
  art + words: stacked for pin and story, side by side for square. 24 designs in total; the round-three
  ones are Blur bloom, Halftone, Eclipse, Breathe, Cyanotype, Pixel, Window and Specimen.
- **Copy:** never "written for you". Kai's own lines and affirmations show no author.
- **Play:** random cards (guess the word, finish the line, true or not), flip in 3D, swipe right if
  you knew it; the card follows the finger, tints, and flies off the way it was thrown.
- **Transitions:** push (detail pages slide in from the right over a dimmed, shrunk page), sheet
  (compose, play, practice, studio rise from the bottom), fade-through for the rest; tabs slide in the
  direction of travel. See `ui/nav/NavHost.kt`.
- **Letters:** three steps (Write, Tuck in, When); reading unfolds the paper and each paragraph; the
  reply is a postcard.
- **On-device AI:** Gemini Nano through ML Kit (`data/ai/LocalModel.kt`), only for original lines,
  affirmations, sparks and prompts. Never for quotes, books or etymologies.
