# Kairos launch film: brief

**Spec:** `1920x1080@60, ~37s, 120BPM, pace: crisp, audio: earshot kit (crisp) + light score, music: minimal pulse`

## Intake

- **Product:** Kairos, an Android app. Every day it gives one person a word, a thought and a small good
  thing, chosen by Kai, an on-device / built-in AI curator that learns from what they keep, share and
  linger on. It also has the Feed (an endless reel of words, thoughts, affirmations and micro book
  summaries), Play (random flashcards of your own words) and the time capsule (letters to future you).
  Share cards come in pin, story and square.
- **Ask (owner, PR #611):** "a full-on great motion product launch video … a banger … replicate the
  references but with our design and our product … no AI-ish slop, be creative."
- **Format:** launch film, 16:9 master, plus 9:16 and 1:1 re-layouts later (same timeline).
- **Pace:** crisp, opted in by the brief ("banger"). 120 BPM, 30 f per beat, 120 f per bar.
- **Engine:** Remotion 4.0.529 (cinetic starter).
- **Where:** GitHub README, Play Store listing, socials.

## References (PR #611, comment 6341107956), what each one gives this film

| # | Reference | What we take |
|---|---|---|
| r1 | Nookly (11 s, 24 fps): pixel mark grows from a tiny tile, sticker-like cards, grainy pastel gradients, a basket pill punched in at macro scale | A logo that *grows* out of a small object; macro push into one UI pill |
| r2 | Paage (70 s): soft multi-hue gradient fields, giant one-word headlines with gradient-filled letters ("Meet", "Using"), a typed prompt bar, the product sitting in a white window, the brand name typed with a cursor at the end | Giant one-word Fraunces headline filled with the Kairos sky; soft sky fields as stages |
| r3 | Shapes (67 s): big rounded shapes in red/blue/yellow that morph and act as **containers** for the product and for photos; a list of features on a curved path; a face character | Bloom petals and Kai as containers; candy-coloured flashcards as big shapes |
| r4 | Mini-app film (76 s): heavy motion-blurred word swaps ("C̶reate", "Discover", "Connect"), a product card standing in for a word inside a sentence ("See an [app] you love?"), soft grey stage, card fans | The product stands in for a word: "A word [phone] for every feeling"; real motion blur on word swaps |
| r5 | Refine (19 s, 24 fps): a flat bright-blue field, serif text with a selection highlight that rewrites a word, cursor clicks a button | A word rewritten in place inside the app card (Saudade → Apricity → Hiraeth) |
| r6 | Forgetting curve (12 s): pink + cobalt duotone, a hand-drawn circle around key words, a face that fills with water as the line resolves | Hand-drawn coral loop around "keep"; Kai's face reacting |

## Brand (supplied by the app: the brand wins over cinetic's defaults)

Taken from `.agents/skills/kairos-playful-sky/SKILL.md` and `ui/theme/Tokens.kt`.

- **Mark:** the moment bloom: 8 teardrop petals around a round core, drawn in one colour; the petal
  at one o'clock (index 1, 45°) is the "moment" petal. In the film the moment petal is coral.
- **Type:** Bricolage Grotesque (display + interface), Fraunces (the words), Caveat (handwriting on
  letters). `// cinetic:brand-supplied serif` (Fraunces) and `// cinetic:brand-supplied handwriting`.
- **Colour:** cobalt `#2F5BFF` (brand stage and action), sky gradient `#3D7BFA → #8EB9FF → #E9F2FF`,
  canvas `#F6F8FE`, ink `#0F1A3A`, night `#1D4BE0 → #0B1440 → #05071A`, candy set (sun, coral, mint,
  bubblegum, lilac, sky candy). `// cinetic:brand-supplied gradient` (sky, night), `lilac` and `sun`
  are brand candy colours, used only on product UI and cards.
- **The one accent:** coral `#FF6A4D` means **"the moment"**: the hand that stops, the petal that is
  yours. It floods the frame once (Act 2) and otherwise stays under ~8% of pixels.

## Hard bans (QA contract, copied from cinetic, with the brand exceptions above)

- Words: no eyebrow/kicker labels, no stacked taglines, no "Introducing…", no sentence dumped at once
  (fragments of ≤ 5 words, ≤ 10 visible), no filler text, fake metrics or lorem ipsum.
- Owner's own ban: never the phrase "written for you". No productivity-slop words ("unlock",
  "level up", "boost", "seamless", "AI-powered").
- Type: no italic or fake oblique (the app's Fraunces italic is not used in the film).
- Colour: no orange/amber/beige/cream as accent, no neon, no glow/halo/bloom effects, no rainbow text.
  Brand gradients only (sky, night), shipped smooth (grain overlay at ≤ 2% hides banding).
- Decoration: no emoji, no stock icons, no sparkles-for-AI, no particles, confetti, lens flares.
  Overshoot only on the two true landings (the seal, the core of the bloom).
- Transitions: no glitch, light leak, film burn, swirl, ripple, flash through white, no crossfade
  through black.
