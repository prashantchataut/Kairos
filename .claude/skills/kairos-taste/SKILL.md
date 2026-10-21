---
name: kairos-taste
description: Art direction, motion and craft rules for the Kairos Android app (Jetpack Compose). Use for any UI, illustration, animation, share-card, icon or copy work on Kairos. Built from the owner's references in issue #606 (top priority), then the best rules of taste-skill (leonxlnx), emil-design-eng, motion-design (LottieFiles), design-motion-principles, impeccable, ui-ux-pro-max and the Android/Material guidance. When a generic rule conflicts with the owner's references, the references win.
---

# Kairos taste

Kairos is **the right moment**: one lovely word, one thought worth keeping, one small good thing, and letters to future you — written by an AI curator for one person. It must feel **playful and hopeful**, warm and grown-up at once. The owner rejected two looks already: a busy "kid's chart-paper" watercolour (too childish) and a clean sky/white system (too serious and minimal). The target sits between: **crisp, confident, alive**.

Read `docs/design/REFERENCE_NOTES.md` before designing anything. Cite reference numbers in commits and decisions ("per #606-03").

## 1. The world: "Wish & bloom"

One idea carries every surface:

> **A moment is a seed.** Words and thoughts are seeds you catch; saving one plants it; practice helps it grow; letters are seeds blown into the future; your activity becomes a garden in bloom.

The **dandelion** is the brand mark (a clock of rays, one seed drifting free — #606-25). The **bloom** is the reward (generative, translucent watercolour-glass petals — #606-28/29). The **sky** is home (#606-01/05/34). **Cute flat characters** bring the play (#606-03/02/09). **Grain** makes it warm and tactile (#606-10/18/24/25/32).

Competitor test: swap in any other learning app — "a seed blown into the future" only belongs to an app with a time capsule and a growing vocabulary. Keep it.

## 2. Materials (use all of them, in their place)

| Material | Where | Rules |
|---|---|---|
| **Sky + aura** | Hero zones, onboarding, curator moments | Cobalt→sky gradient with 2–3 soft aura blobs drifting slowly (#606-02/34). Never a flat two-stop gradient alone. |
| **Warm paper** | Canvas below heroes, cards, studio | `#F7F5F0` light / `#0B1026` dark. Optional speckle (#606-30) at 3–5% only on large empty paper. |
| **Film grain** | Every gradient, aura, illustration and share card | Fine monochrome noise, 6–10% opacity, never on body text. This is what separates "airbrushed and warm" from "flat vector". |
| **Liquid glass** | Floating controls only: nav bar, segmented controls, chips on imagery, sheets, folders, orbs | Real backdrop blur (Haze) on API 32+, frosted tint fallback; a bright top rim and a soft bottom rim. Never glass on reading surfaces. |
| **Watercolour-glass petals** | Blooms, rewards, the garden, empty states, share backgrounds | Translucent overlapping petals with multi-hue gradients (coral→butter→cyan→violet) at 35–70% alpha so overlaps mix like glazes (#606-29). |
| **Painterly brushwork** | Vocabulary share backgrounds, special cards | Procedural acrylic strokes: many curved thick strokes, varied width, alpha 70–95%, palette from #606-19. Never round blotches. |
| **Flat characters** | Onboarding cast, moods, empty states, curator | Solid saturated shapes (blob, cloud, flower, hexagon, triangle) with two white pill eyes, no outlines, no gradients except a faint grain (#606-03/09). |

## 3. Colour

- **Sky:** `#3E7BFA` → `#8DB9FF` → `#E6F0FF`. **Ultramarine accent** `#2340E8` (#606-22/23/30) for primary actions and links. **Ink** `#101828`. **Paper** `#F7F5F0`.
- **Pastel tiles** (#606-05): butter `#FFF0A6`, mint `#CDEFDF`, lilac `#E4DAFF`, peach `#FFDCC7`, sky `#D6E7FF`, blush `#FFD5E4` — each with its own deep ink for text on it.
- **Character primaries** (#606-03): blue `#2F6BFF`, coral `#FF5A4E`, pink `#FF4FD8`, green `#22C27A`, orange `#FFB020`, sun `#FFD84A`.
- One accent per screen does the work; pastels and primaries are content colour (tiles, characters, blooms), not UI chrome.
- Text on any colour ≥4.5:1; on pastel tiles use the tile's deep ink, never grey.
- Dark mode is its own palette (night `#0B1026`, aura in indigo/violet/teal), designed, not inverted.

## 4. Type

- **Bricolage Grotesque** (variable wdth/wght/opsz) — interface and playful display. Display at 800 weight, condensed width (~85), tracking −0.03em, leading 0.95 (#606-03). Body at 400–500, opsz text, 16sp/24sp.
- **Instrument Serif** (+ italic) — the words, quotes, letters and big dates (#606-08/13/30). Words are always serif.
- Two families, no more. Five sizes per screen at most. Headline ≤2 lines; the main word scales down to fit, never wraps mid-word.
- **Focus-pull type** (#606-05/12/14): large greetings and section heroes may enter blurred (8–14px) and sharpen into focus; a soft, out-of-focus echo of a word may sit behind content. On API <31 substitute an opacity/scale crossfade.
- Hand-drawn accents allowed once per screen: an underline or circle drawn with a stroke animation (#606-15).
- No eyebrows/kickers, no tracked uppercase labels, no monospace, no 01/02/03 numbering (taste-skill, impeccable).

## 5. Composition patterns

- **Hero on sky, content on paper** (#606-01). The hero holds one thing: today's word in giant serif, or a big greeting.
- **Collage of tilted cards** (#606-14/05/30): Today's secondary items (thought, small good thing, capsule, review) appear as cards tilted −4°…+4°, overlapping slightly, each with its own typographic voice (serif quote card, bold date card, pastel tile, photo/attachment card).
- **Decks** (#606-33/34): a fanned stack with coloured edges peeking; swipe or tap to deal the next card. Used for the word deck, curator suggestions and studio templates.
- **Folders with contents peeking** (#606-06/11): capsule letters show paper corners, photos and stickers peeking out of frosted folders.
- **Selectable pills** (#606-15): dashed outline when off, solid glass when on, with a small illustration or emoji-free glyph and a +/− that morphs.
- **Bento of personality tiles** (#606-05): the library mixes tile voices (pastel word tile, serif quote tile, audio/voice tile, photo tile).
- Material structure stays: bottom navigation on compact width, navigation rail on medium/expanded width (Android guidance); system Back always works; edge-to-edge insets.

## 6. Motion — great, not only subtle

Personality: **Playful** for illustration and celebration (overshoot 10–20%, 200–400ms), **crisp** for UI (Emil: ease-out `cubic-bezier(0.23,1,0.32,1)`, under 300ms, press scale 0.96–0.98). Never ease-in for entrances. Never animate from scale 0 (start ≥0.85 with opacity), except characters that "pop" (start 0.6 with overshoot).

Frequency decides intensity (Emil):
- **Rare / first-time → full choreography**: onboarding scenes, connecting the curator, first bloom, sealing and opening letters, practice completion.
- **Occasional → a signature moment**: saving a word (seed release), sharing a card, logging mood, explore results dealing in.
- **Frequent → quick and quiet**: tab switches, toggles, scrolling, chips.

Every animated moment has three layers (motion-design): **primary** (the thing), **secondary** (shadow, satellites, label), **ambient** (aura drift, grain shimmer, petal sway). Stagger 40–80ms, total ≤500ms. No more than a third of elements moving at once. Exits ~35% faster than entrances.

Signature moves (build these as reusable components):
1. **Scene change** (#606-03): current scene collapses to centre (scale 0.6 + fade, 220ms), next hero pops (0.6→1 with overshoot), satellites burst outward on arcs, headline slides with the page.
2. **Focus pull**: blur 14→0 + scale 1.04→1 + alpha, 520ms emphasized.
3. **Bloom** (#606-28/29): stem draws (path trim 0→1, 450ms), petals pop in sequence with overshoot and slight rotation, translucent petals open (rotation spread), then idle sway.
4. **Seed release**: on save, a dandelion seed lifts from the heart, drifts on a curved path with gentle wobble, and lands on its destination (Learn tab / garden); the destination pulses.
5. **Deck deal** (#606-33/34): cards fan in from the stack with stagger; swipe-away uses velocity (flick ≥ ~1000dp/s dismisses) and the next card rises.
6. **Curator awakening** (#606-34): orb ignites (glow + scale), personal lines crossfade one at a time with focus pull, "Great to meet you", then the first words deal in as a deck.
7. **Liquid control**: sliders and toggles stretch with velocity and settle with a spring; haptic tick on each step.

Reduced motion ("Calmer motion" or animator scale 0): keep fades and colour, remove travel, loops and bursts; blooms appear already open.

## 7. Illustration in code

All art is drawn in Compose (Canvas/Path), so it stays crisp, themable and animatable:
- Characters: blob/cloud/flower/hexagon/triangle bodies built from smooth closed paths; eyes are white rounded pills; blink and bob on loops; squish on tap.
- Flowers: capsule petals with sweep gradients (#606-28) and translucent multi-hue petals (#606-29).
- Grain: a cached noise tile drawn with `ShaderBrush(ImageShader(... Repeated))`.
- Brushwork: deterministic seeded strokes (quadratic curves, round caps, varying widths).
Seed every procedural drawing so the same content always renders the same art.

## 8. Share cards (#606-17/18/19/20/22/23/24)

Each template is a small art piece at 1080px wide, branded with the dandelion mark + "Kairos" small. Templates: **Growth** (grainy blurred sky/meadow, giant word, dictionary micro-text), **Brushwork** (acrylic strokes), **Bloom** (translucent flower on warm paper), **Ultramarine** (staggered Swiss lowercase on `#2A12B8`), **Seed** (electric blue, organic white shapes, yellow seed dot), **Sunrise** (grainy lavender→peach glow with a tiny sun character), **Leaf** (macro-vein texture, acid-lime serif). The word's own colours (from its topic) tint each.

## 9. Copy

Warm, specific, slightly witty, never mystical. Plain verbs. One idea per line. Kind to low moods. Name controls by their action ("Seal letter", "Plant it"). Error messages say what happened and what to do. No exclamation spam; one per screen at most, for real celebration.

## 10. Process (from taste-skill + impeccable)

1. Re-read the notes; name the reference numbers you're following.
2. Write the screen's one-sentence idea: "On this screen, [subject thing] becomes [device]".
3. Build still first (no motion) and render it: Roborazzi screenshots at phone width, light and dark, plus a wide (rail) layout.
4. Critique the render honestly against the references and this file; fix the top three; render again (max three rounds).
5. Add the motion layer; verify reduced-motion still shows everything.
6. Only then commit and push.

## 11. Never

- Never blotchy watercolour "puddles", paper-craft stamps, or thick outlines (rejected look 1).
- Never a sterile white screen with one blue accent and nothing alive on it (rejected look 2).
- Never productivity chrome: checklists, progress rings, streak flames, KPI tiles.
- Never glass on reading surfaces, gradient text, emoji as icons, or invented social proof.
