# Kairos — Design System: "Watercolor Glass"

Design read: a personal feel-good + learning app for a small community, with a playful-hopeful
liquid-glass × 2D-cute × watercolor language, on Material 3 structure.
Dials (taste-skill): variance 7 · motion 7 · density 3.

## World
A sheet of cool watercolor paper. Soft paint blooms (sky, mint, lilac, peach, butter) drift
very slowly underneath. Floating chrome (the nav bar, top bars, sheets) is **liquid glass**:
real backdrop blur (Haze), a white tint, and a bright specular rim on the top-left edge.
Content cards are **frosted acrylic**: translucent paper with the same rim light, no blur,
so reading surfaces stay crisp.

## Signature
**The liquid droplet tab indicator.** The selected-tab blob stretches toward its destination
(leading edge on a fast spring, trailing edge on a slow one) and settles like a drop of water.
Pip, the watercolor droplet mascot, is the second voice: mirrors your mood, blinks, cheers.

## Colour (light)
| Token | Hex | Role |
|---|---|---|
| paper | #F3F7FB | app background (cool, not cream) |
| ink | #1D2640 | text, primary buttons (navy ink, never pure black) |
| inkSoft | #5A6683 | secondary text (≥4.5:1 on paper) |
| lagoon | #3E8EDE | the one accent: selection, links, progress |
| sky / mint / lilac / peach / butter / rose | #8FC1F7 / #8EDDC0 / #BBA8F6 / #FFB59A / #FFE07A / #FF97AB | paint washes only — backgrounds, mascot, illustrations |

Dark ("night pond"): paper #0C1322, ink #EEF3FF, inkSoft #A6B1CE, washes at lower alpha.

## Type
- **Fredoka** (500/600/700) — display and titles. Rounded, friendly, legible.
- **Nunito** (400–800) — body and labels.
- **Caveat** — only inside capsule letters (handwriting belongs to letters).
Tracking −0.02em on display. All sizes in sp via the Material type scale.

## Shape & depth
Cards 28dp radius, controls are pills, chips 50%. Depth = one soft offset shadow
(y 10dp, blur 24dp, ink @ 8%) **or** a rim — never a border under a wide shadow.

## Motion
- Springs everywhere, `dampingRatio 0.8` for UI, `0.55` for Pip and celebrations.
- Enter = fade + 12dp rise + slight scale (0.96→1). Exits are shorter and smaller.
- Press = scale 0.96 on a stiff spring (Emil: high-frequency, so fast).
- Rare moments get the show: sealing a capsule (wax stamp), opening one (envelope),
  finishing a game, the onboarding bloom.
- System "Remove animations" (animator scale 0) → background drift, idle mascot
  motion and celebrations are disabled; transitions become crossfades.

## Components
`WatercolorBackdrop`, `GlassSurface` (real blur), `AcrylicCard` (no blur), `LiquidNavBar`,
`Pip`, `InkButton`, `SoftChip`, `MoodPicker`, `WeekStrip`, `PaintProgress`.
