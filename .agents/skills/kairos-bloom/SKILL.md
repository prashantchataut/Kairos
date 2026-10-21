---
name: kairos-bloom
description: The Kairos design language — a dawn garden for "the right moment". Use for any Kairos screen, component, brand mark, share card, or motion work. Encodes the palette, type, paper/grain/glass materials, the rhododendron bloom mark, the adaptive slider, the motion system, and the screen blueprints derived from the owner's 29 references in issue #606 plus the best public design/motion skills. Read this before touching Kairos UI.
version: 1.0.0
metadata:
  author: Agent C
  sources: "issue #606 references (primary), design-taste-frontend, impeccable, hallmark, ui-ux-pro-max, motion-design (LottieFiles), logo-design"
---

# Kairos Bloom

> A word, a thought and a small good thing — written for you every day.
> Kairos (καιρός): *the right moment*. The interface should feel like **a dawn garden**:
> hopeful, tactile, a little playful, never a productivity dashboard.

This skill is the single source of truth for Kairos UI. The owner's references in
`references/issue-606-notes.md` outrank every public skill. Where this file and a generic
skill disagree, this file wins.

**Design read:** a personal daily ritual for a reflective, design-literate reader. Warm
editorial + tactile play: dawn sky, paper, riso grain, glossy bloom. Think a poet's desk in
Kathmandu at 6am, not a SaaS dashboard.

**Dials:** `DESIGN_VARIANCE 7`, `MOTION_INTENSITY 8`, `VISUAL_DENSITY 3`.
Always `emotional > minimal`, `warm > clinical`, `object > card`, `one hero > many equal cards`.

---

## 1. The twelve laws

1. **One hero per screen.** One enormous serif word, number or headline. Everything else
   steps back. If two things shout, neither is heard.
2. **Sky is a stage, not wallpaper.** The dawn gradient changes: morning (periwinkle→peach),
   midday (azure), evening (rose→plum), night (indigo). It ends in paper, never in a hard edge.
3. **Objects, not cards.** Paper notes, frosted folders, sealed envelopes, glossy blooms,
   stickers, torn edges. Slight rotations (±1–4°) and overlap are encouraged. A grid of
   identical white cards is a failure.
4. **Colour with feeling.** One saturated object per screen (coral, marigold, rhodo pink,
   mint or periwinkle). Everything else is paper, ink and sky.
5. **A character.** The bloom (see §5) is alive: it opens on welcome, bursts on save, nods on
   success, sleeps in empty states. It is a mark and a mascot, never a cartoon sticker.
6. **Texture is mandatory.** 4–7% grain on every large surface; soft inner rim on glass;
   gradient scrim on imagery. Flat vector white is the enemy.
7. **Motion has weight and breath.** Nothing linear except progress/spinners. Springs for
   movement, 6–10s sine loops for ambient life, 0.9–1.2s for a bloom. Cut motion with the
   system animator scale and the in-app *calmer motion* switch.
8. **Type is the image.** Serif for meaning (words, quotes, dates, headlines); sans for
   doing (labels, controls, body). Mono eyebrows for credibility. Never set a headline in
   all-caps sans with default tracking.
9. **Copy is literary, warm, second person.** Short sentences. No "tasks", "streaks",
   "productivity", "crush your goals", no exclamation-mark hype. A little humour is allowed.
10. **Craft in the small parts.** 10–11px labels with 0.10em tracking, 1px hairlines, status
    dots, real waveforms, hairline dividers that fade at the ends, pressed states with haptics.
11. **Never block delight.** Every animation has a reduced-motion path that still communicates
    the state change (instant cut + opacity, no travel).
12. **Personal.** The mark carries Nepal's rhododendron and the owner's initial; the palette
    carries Himalayan dawn. Personal is specific, not decorative.

---

## 2. Palette (exact tokens)

`ui/theme/Tokens.kt` owns these. Never inline a hex in a screen.

### Light — "Dawn"
| Token | Hex | Use |
|---|---|---|
| `skyTop` | `#4A7FE0` | top of hero sky |
| `skyMid` | `#8FA8EE` | mid sky |
| `skyHaze` | `#D9C4EE` | lilac haze before horizon |
| `skyWarm` | `#F8D6BE` | peach horizon |
| `skyFade` | `#FBF4EA` | melt into paper |
| `canvas` | `#FBF7EF` | app background (warm paper) |
| `surface` | `#FFFDF8` | paper card |
| `surfaceSoft` | `#F2EBDD` | wells, tracks, quiet chips |
| `line` | `#E7DCCB` | hairlines, borders |
| `ink` | `#1E1B2E` | primary text (indigo-ink, never pure black) |
| `inkSoft` | `#5D586C` | secondary |
| `inkFaint` | `#9B93A3` | tertiary, disabled |
| `accent` | `#E4553F` | rhododendron coral — primary action |
| `accentSoft` | `#FBE2D9` | accent wash |
| `gold` | `#E7A93C` | the seed, highlights, saved |
| `mint` | `#6FB79A` | growth, calm, success |
| `periwinkle` | `#8B93E8` | dusk, memory, capsule |
| `rose` | `#D9587C` | tenderness, quotes |
| `leaf` | `#3E7A5C` | nature, depth |
| `positive` | `#3E9B77` | success text |
| `warning` | `#DE9A2D` | caution |
| `negative` | `#D24A52` | error |
| tints | `#DDE3FB`, `#D8F0E5`, `#F9DEE3`, `#FCE5D5`, `#F9EFCC`, `#E9DFF9` | tile backgrounds |
| tintInk | `#3346A8`, `#1D6B50`, `#A32F55`, `#A34D22`, `#7C6210`, `#5B3FA0` | ink on tints |

### Dark — "Dusk"
| Token | Hex |
|---|---|
| `skyTop` | `#151C46` |
| `skyMid` | `#2B2A63` |
| `skyHaze` | `#4C3568` |
| `skyWarm` | `#6B3B57` |
| `skyFade` | `#14121F` |
| `canvas` | `#121019` |
| `surface` | `#1D1A28` |
| `surfaceSoft` | `#272333` |
| `line` | `#373143` |
| `ink` | `#F7F0E4` |
| `inkSoft` | `#B9B0C3` |
| `inkFaint` | `#82798F` |
| `accent` | `#FF8A6B` |
| `accentSoft` | `#3B2328` |
| `gold` | `#F3C066` |
| `positive` | `#57C39A` |
| `negative` | `#FF7A7E` |

**Rules.** Coral is the only CTA colour; gold is a highlight, never a button. Never put
coral text on a coral wash. On sky, all text is `onSky` (ivory `#FFF9F2`), secondary at 78%.

---

## 3. Type

Keep **Instrument Serif** (words, quotes, hero, dates) and **Instrument Sans**
(UI). Add **mono** only for eyebrows/meta (`FontFamily.Monospace`, 10sp, `0.12em`, uppercase).

| Style | Size / line | Family | Use |
|---|---|---|---|
| `hero` | 72–84 / 0.98 | Serif 400 | word of the day, welcome |
| `serifLarge` | 40 / 1.06 | Serif 400 | screen statements, dates |
| `serifDisplay` | 56 / 1.0 | Serif 400 | capsule title, big moment |
| `quote` | 24–30 / 1.22 | Serif italic | quotes |
| `display` | 30 / 1.16 | Sans 600, -0.02em | section titles |
| `title` | 19 / 1.25 | Sans 600 | card titles |
| `body` | 16 / 1.55 | Sans 400 | reading |
| `label` | 14 / 1.4 | Sans 500 | controls |
| `caption` | 12 / 1.3 | Sans 500 | meta |
| `eyebrow` | 10 / 1.2, 0.12em | Mono | labels above a title |

**Rules.** Italic serif only for quotes and the tagline, never for UI labels. No all-caps
serif. Numerals in hero use tabular figures where available. Track display sans tighter
(-0.02em), never loosen.

---

## 4. Shape, space, texture

- Radii: `chip 14`, `tile 22`, `card 30`, `sheet 36`, `pill = full`.
- Spacing: 4dp grid; 20dp side gutter; 24–28dp card padding; 12–16dp gaps.
- Shadow: wide and low, tinted with `ink` at 8–12%, never pure black. Paper sits *on* the
  page; it does not float in space.
- Glass: `surface @ 18–30%` + 1dp inner ivory rim + 20–28dp blur (Haze). Only over sky/photos.
- Grain: draw a deterministic noise tile (see `Modifiers.grain()`) at 4–7% over large
  surfaces. Never animate the grain.
- Edges: a 1px `line` hairline that fades at both ends for dividers; dashed 1.2dp borders for
  scrapbook tiles/choices.
- Photography: warm, soft, low contrast; always add a bottom scrim before text.

---

## 5. The mark — "Kairos Bloom" (rhododendron)

**Meaning.** Nepal's national flower, *laligurans* (rhododendron), blooms across the
Himalaya at dawn — the season's *right moment*. Five petals = the five daily moments
(word, thought, small good thing, letter, reflection). The gold seed at the centre is the
present moment; the negative-space gaps form a subtle star. The mark is drawn from the
owner's initial: the two upper petals and the stem gap hint at a **P** without spelling it.

**Construction (100×100):** centre `(50,52)`, petal length `r = 40`, 5 petals at
`-90° + i·72°`. Each petal is a teardrop: start `(0,-0.16r)`, cubic to `(0,-r)` with
controls `(-0.56r,-0.52r)` and `(-0.44r,-0.96r)`, mirrored back. Centre seed radius `0.15r`.
Gap between petals ≈ 12°.

**Variants.**
- *Full colour (launcher):* indigo→plum dawn squircle, ivory petals with a warm inner
  gradient, gold seed with a tiny white glint, one small 4-point sparkle at 1 o'clock.
- *On sky:* ivory petals, gold seed, no badge.
- *Tinted badge:* indigo circle, ivory petals, gold seed.
- *Monochrome:* single ivory/ink silhouette, seed as a hole.
- *Animated:* petals scale/rotate out from the centre in sequence (0, 60, 120, 180, 240ms),
  seed pops last with a 1.15 overshoot, then a slow 8s breathing scale (1 → 1.015).

Never add a literal clock, hourglass, letter K, or gradient blob behind the flower. The
flower *is* the time mark.

---

## 6. Motion

Read `references/motion.md` and the vendored `motion-design` skill for theory. Kairos rules:

### 6.1 Personality
**Hopeful / tactile.** Not bouncy-toy, not corporate. Overshoot only on blooms/seeds (8–15%);
everything else settles with a soft spring (damping 0.78–0.88). Signature easing
`CubicBezier(0.22, 0.61, 0.16, 1)` for entrances; exits accelerate.

### 6.2 Tokens (in `Motion`)
```
quick    = 140ms   // press, toggle
standard = 260ms   // chips, fades, tabs
slow     = 420ms   // page, sheet
bloom    = 1000ms  // flower open, seal break
ambient  = 8000ms  // breathing, drift
spring.soft   = damping 0.82, stiffness 260
spring.settle = damping 0.9,  stiffness 180
spring.pop    = damping 0.55, stiffness 420   // seed/celebration only
```

### 6.3 Three layers, always
- **Primary:** the state change (card opens, bloom bursts).
- **Secondary:** shadow/scale/adjacent content reacting 40–80ms later.
- **Ambient:** sky drift, grain shimmer, orbit rotation (6–10s, ±2–4%).

### 6.4 Choreography
- Stagger 40–60ms, total < 420ms. Hero first, then actions, then meta.
- 1/3 rule: no element travels more than 1/3 of the screen without a keyframe.
- Entrances from below (12–24dp) + opacity + 0.98 scale; exits fade + 0.99 scale.
- Page transitions: outgoing scales to 0.985 and fades 180ms; incoming rises 16dp/260ms.
- Onboarding page change: cloud/petal wipe left-to-right, content counter-slides 18dp.

### 6.5 Signature patterns (must exist)
1. **Bloom open** (welcome, finish, seal break): petals sequentially unfurl with rotation
   -12°→0 and scale 0.2→1; seed pops last; one soft light ring expands and fades.
2. **Save burst**: heart compresses (0.85) → gold seed flies to the bloom → 10–14 petals
   scatter with gravity and fade over 700ms; a "Kept" paper tag slides up 8dp. Haptic tick.
3. **API key success**: field border draws from grey → mint (400ms), lock icon morphs to a
   check (scale/rotate), a thin ring sweeps around the field, status text crossfades.
4. **Adaptive slider**: thumb grows 1.0→1.25 while dragging, gradient fill follows with a
   60ms lag, value bubble springs above, detents tick with haptics, settle with `spring.pop`.
5. **Card shuffle** (capsule/library): stack fans ±6°, translates ±12dp, springs back; the
   picked card lifts and expands via shared bounds.
6. **Ambient sky**: two large radial orbs drift on 12s/17s sine loops, 2–3% opacity change;
   petals fall slowly (1 per 4–6s) on welcome only.
7. **Number change**: digits roll up 8dp with 30ms stagger; no odometer spinning.

### 6.6 Reduced motion
When `K.calm` is true: no travel > 8dp, no particles, no ambient drift, bloom snaps to open,
celebration becomes a 120ms gold flash on the heart. Everything remains understandable.

---

## 7. Components

### 7.1 AdaptiveSlider (required)
A custom slider, not Material's. Spec:
- 56dp tall touch area; 10dp track (light) / 8dp (dark); fully rounded.
- Track is a **gradient that adapts to the value domain**: mood = rose→butter→mint;
  years = periwinkle→coral; level = mint→periwinkle→gold.
- Thumb: 26dp paper disc, 2dp ivory rim, soft shadow; inner 10dp dot in the current colour;
  grows to 1.22 while dragging; haptic tick on each detent.
- Value bubble: pill above the thumb, `label` type, appears on drag/selection, springs from
  0.8 with `spring.pop`, hides after 900ms.
- Detent ticks: 4dp marks at 25% opacity; active ones take the current colour.
- End labels: `caption` at the two ends, `inkFaint`; the selected label becomes `ink`.
- Supports discrete steps (mood 1–5, level 1–5) and continuous (years 1–10).
- Accessibility: `progressBarRangeInfo`, `setProgress` semantics, 48dp min target.
- API: `AdaptiveSlider(value, onValueChange, range, steps, colors: List<Color>, labels, onSettle)`.

Usage: mood check-in (Today), word level (TastePicker/onboarding), capsule open-in
(Compose), practice confidence.

### 7.2 PaperCard
`surface` + 30dp radius + 1dp `line` + wide tinted shadow + optional 5% grain + inner top
highlight. `padding = 24dp`. Optional `tab` colour strip (folders) or taped corner.

### 7.3 GlassHero / GlassAction
Frosted only over sky. 1dp ivory rim at 40%, 22% white fill, 20dp blur. Actions are 84dp
tall tiles with icon + label; the active one glows gold, not pink.

### 7.4 StickerChip
Pill with 1dp border; selected = ink fill + paper text; optional dashed border for
"choose me" states; leading dot/dot-matrix icon. Press scales 0.97.

### 7.5 BloomBurst
Overlay `Box` with `pointerInput` off; particles are petals (8×14dp teardrops) with random
angle, velocity 180–320dp/s, gravity 480dp/s², rotation ±220°, life 650–850ms. Gold seed
core expands 0→1.2→0. Draw in a single Canvas for performance.

### 7.6 NavDock
Floating frosted pill, 64dp tall, 20dp margins, 4 tabs. Selected tab gets a **petal-shaped
indicator** (not a circle): a soft 18×26dp blob behind the icon, ink at 8%; icon scales 1.08;
label appears only for the selected tab (crossfade). FAB is a 60dp coral disc with the bloom
embossed; it rotates 45° into a close cross and morphs its petals inward when open.

### 7.7 Envelope / folder
Capsule letters are paper envelopes with a wax seal in the capsule tint; sealed = closed flap,
ready = flap open + gold glow, opened = flattened with a date stamp. Folders have a coloured
tab, frosted body, ghost date, and 2–3 stacked photo corners.

### 7.8 KeyField / SuccessSeal
See 6.5.3. Never show a spinner where a drawn progress ring can tell the story.

### 7.9 Share cards
Templates: **Dawn** (sky gradient + grain + serif), **Paper** (cream + ink + tiny mark),
**Riso** (flat two-colour + grain + offset type), **Leaf** (macro-ish green wash + chartreuse
serif), **Night** (indigo + gold + star field). Type is always serif for the word/quote; the
mark + "Kairos" sits bottom-centre at 60% opacity. Export 1080×1350 and 1080×1920.

---

## 8. Screen blueprints

### Onboarding (4 beats)
1. **Welcome** — dawn sky, drifting petals, the bloom opens petal by petal (1.2s), "Kairos"
   rises with a blur-to-sharp, tagline in serif italic. A fanned stack of three real cards
   floats in 3D; a "Begin" pill at the bottom; progress = 4 petals that fill.
2. **Name** — "What should we call you?" serif 44, a large paper input with a caret that
   breathes; the typed name appears in the sentence above ("Good to meet you, Prashant").
3. **Taste** — "What should find you?" topic stickers pop in staggered; the adaptive slider
   sets word level; a live preview line: "We'll start with *luminous* words."
4. **Curator** — the key card, a small orbit of three "what it writes" chips, the key field
   with success animation, and "I'll do this later" as a quiet text link.
Bottom chrome is fixed; only the story moves. Clouds/petals wipe between beats.

### Today
Dawn sky header: greeting + date, a **week arc** of 7 petals (filled = a good moment), a
segmented Word/Thought control, the hero (serif word 72 or quote 26), glass actions with a
**save burst**, then paper sections: "About this word" with a sliding tab pill, mood card with
the adaptive slider, a torn-paper "small good thing" note, review card, capsule envelope.
Content fades under the dock; sky ends in paper with a 64dp tail.

### Learn
Big serif "Learn", a warm subline, a coral "Ask for words about…" card with a glowing search
well and topic stickers, a review banner with a gradient progress, segmented Words/Thoughts,
a 2-column **board** of paper tiles with tinted top tabs, serif word, one-line gloss, and a
tiny status dot; tiles rotate ±1° and have different tints; masonry heights (not equal).

### Capsule
"Letters to future you" in two-line serif 48, a small orbit of envelopes; ready letters glow
gold with a wax seal and "Open" affordance; sealed letters are folder objects with ghost
dates; opened ones are flattened letters with a date stamp. Compose: paper sheet, mood, the
adaptive slider for "open in" (1 month → 5 years) with a live date, tint picker as paint
chips, attachments as stickers, "Seal it" with a wax-stamp animation.

### You
A portrait ring with the bloom behind the initial, name in serif 34, three stat petals (not
tiles), "four weeks of moods" as a garden of small blooms/dots on paper, recent moments as a
timeline with tiny icons, settings as a paper list with mono section labels, and a signed
"Made for the right moment" footer.

---

## 9. Copy voice

- Second person, present tense, short. "A word worth keeping." not "Unlock your vocabulary."
- Warm, never saccharine. "Today is still being written." for loading.
- Use concrete images: "sunlight through leaves", "a letter to future you".
- No productivity jargon, no streak guilt, no "AI-powered" in UI. Empty states are invitations.
- The curator speaks as a person: "I chose this because you keep saving quiet weather words."
- Buttons are verbs: "Begin", "Keep it", "Seal it", "Open when ready".

---

## 10. Anti-patterns (reject on sight)

- Generic glassmorphism on paper; frosted cards on a flat white background.
- Cold grey canvas (`#F6F8FB`), pure white cards, one cobalt accent everywhere.
- Three equal feature cards; equal-height grid; centred everything.
- Inter/Roboto defaults, all-caps sans headlines, gradient text on body copy.
- Purple AI gradient orbs, neon glows, "sparkle" icons sprinkled everywhere.
- Confetti that looks like a party popper; use petals and seeds.
- Bouncy motion on every tap; overshoot on navigation or text.
- Literal clock/hourglass iconography; the bloom carries time.
- Fake metrics, fake testimonials, fake "trusted by" rows.
- To-do list patterns: checkboxes as the main interaction, red overdue counts, streak fire.
- Animating grain, blurring large layers every frame, per-frame allocations in draw loops.

---

## 11. Quality gates (run before every PR)

1. Does each screen have exactly one hero? Squint — is it obvious?
2. Is there grain on every large surface, and a warm tint in every shadow?
3. Are there at least two *objects* (paper/envelope/folder/sticker) on the screen?
4. Does the sky palette match the time of day/mood, and end in paper?
5. Is the bloom present at least once, with correct geometry (5 petals, gold seed)?
6. Do all primary buttons use coral; all highlights gold; no second CTA colour?
7. Do entrances use springs, staggering < 420ms, and reduced-motion paths?
8. Does the save action burst? Does the API key success animate? Does the slider feel alive?
9. Is any text < 11sp, any tap target < 48dp, any contrast < 4.5:1?
10. Is the copy free of productivity clichés and exclamation marks?
11. Do screenshots render cleanly in both light and dark at 400×860?
12. Would this look at home next to references 05, 06, 15, 23, 28 — and *not* like a template?

---

## 12. Workflow

1. Read `references/visual-language.md`, `references/motion.md`, `references/screens.md`, and the
   issue notes. Skim the contact sheet.
2. State the design read for the screen in one line. Pick the hero and the one saturated object.
3. Build tokens/components first; never inline colours or one-off shapes.
4. Implement motion with the three layers; add the reduced-motion branch immediately.
5. Render with `./gradlew :app:recordRoborazziDebug` (or `testDebugUnitTest
   -Proborazzi.test.record=true`) and *look at every screenshot*. Fix what you see, not what
   you imagine.
6. Run `scripts/verify_kairos.sh`, the unit tests, and `assembleDebug`.
7. Update `docs/screenshots`, the README's design section, and this skill if a rule changed.

## References

- `references/issue-606-notes.md` — the owner's 29 images + 4 videos, analysed one by one (primary).
- `references/reference-contact-sheet.jpg` — all stills in one sheet.
- `references/visual-language.md` — palette/type/material recipes in Compose.
- `references/motion.md` — timing tables, recipes, and code sketches.
- `references/screens.md` — per-screen layout and state notes.
- `.agents/skills/motion-design/` — LottieFiles motion principles (MIT), vendored.
- `.agents/skills/design-taste-frontend/` — anti-slop taste rules (Leonxlnx).
- `.agents/skills/impeccable/` — craft, critique, polish, android guidance (pbakaus).
- Public research used: `Nutlope/hallmark`, `nextlevelbuilder/ui-ux-pro-max-skill`,
  `kaankiziltug/logo-design-skill`, `ibelick/ui-skills`.
