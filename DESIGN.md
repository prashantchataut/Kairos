# Kairos Design System — "Moment Blue"

## Direction
A calm, intelligent, emotionally warm self-growth app. Vivid royal blue is the
brand anchor; surfaces are large, rounded, object-like, and softly layered
(translucent frosted glass, stacked cards, gentle depth). Typography is bold
and confident; every screen has ONE strong visual moment. Premium consumer
polish, distinctly mobile.

## Dials
Variance 6 · Motion 5 · Density 3.

## Design tokens
All screen styling resolves through `KairosTokens` (semantic colors against the
active Material scheme + centralized spacing, radii, icon sizes, motion).
Dark and light share one architecture — no per-screen hard-coded colors.

## Anchors
- The **Kairos mark** — "the moment between": a rounded tile holding a flowing K
  whose strokes converge into a filled dot with a motion trail. One silhouette,
  monochrome-safe, legible at 24px, volumetric at app-icon scale.
- Large rounded "object" cards with stacked depth — not generic dashboard grids.
- Floating frosted-glass navigation with an expanding active tab.
- Bold typography as a design element (huge greetings, short strong headlines).
- Intentional dark mode: charcoal ink-navy, luminous blue highlights, atmospheric glows.

## Color strategy
Royal/electric blue `#2E5BFF` is the single brand anchor. Periwinkle, soft
cyan, mint (progress), and warm coral (emotion) support it. Light mode is
pearl with a blue tint; dark mode is charcoal ink-navy with luminous highlights.

### Light
- Background `#F5F7FC` · Surface `#FBFCFE` · Container `#E7ECF7`
- Ink `#101828` · Soft `#5A6478` · Faint `#8B93A7` · Hairline `#D8DFEC`
- Primary royal blue `#2E5BFF` · On `#FFFFFF` · Wash `#DDE5FF` · On wash `#0A2A75`
- Secondary coral `#E86A5E` · Tertiary cyan `#0090B2` · Success mint `#2E9E6B` · Error `#D64545`

### Dark — charcoal ink-navy
- Background `#0B0E15` · Surface `#11161F` · Container `#1E2636`
- Ink `#E9EDF6` · Soft `#9AA5BE` · Faint `#6C7690` · Hairline `#2A3345`
- Primary periwinkle `#8FA6FF` · On `#0A1E54` · Wash `#1B2F6E`
- Success mint `#7BD9A5` · Error `#FF8A80`

## Typography
- Display/headline/title: bold native sans, tight tracking, generous sizes.
- Lora (bundled serif) reserved for the reflective voice: quotes, prompts,
  journal titles — italic accents inside a bold-sans world.
- Sentence case everywhere. No all-caps eyebrows.

## Shape & surfaces
- Generous radii: controls 14, buttons 18, reading cards 24, feature cards 26,
  floating 30, nav capsule 32.
- Frosted glass: translucent tonal fill, bright top edge, hairline border,
  soft tinted shadow, gentle top sheen. No real blur (platform hacks); 
  translucency carries the effect on every API level.
- Object cards: large radius, soft depth, no hard borders on reading surfaces.
- Stacked-card depth: one layer offset behind the hero card, slight rotation.

## Navigation
- Floating frosted-glass capsule above content, breathing room underneath.
- Inactive destinations are circular icons; the **active tab expands** into a
  bright blue capsule with icon + label (animated width).

## Screens
- **Today**: huge personalized greeting, then ONE dominant blue word card
  (white serif word, stacked depth, white CTA), then the layered thought card,
  then a quiet progress line.
- **Reflect**: one intimate prompt card (serif question, mood orbs), then the
  journal as stacked object cards.
- **Practice (flashcard)**: dominant blue card with white serif word; grading
  via friendly emoji circles (Again/Hard/Good/Easy).
- **Profile**: fully theme-driven (no dark banner in light mode, no white
  sections in dark mode): compact rounded banner card, avatar with primary
  ring, name/handle/bio, inline stats, tag chips, "Choose your banner" row.
- **Settings/Edit profile**: blue-world palettes, grouped rounded surfaces.

## Onboarding
A cinematic 5-page launch sequence on ink-navy with atmospheric blue glows,
each page its own composition (floating layered product fragments, a journey
path, a personalized reveal):
1. **Make space for what matters** — poster with the mark and two drifting
   product fragments (word + quote mini-cards).
2. **Understand yourself** — layered reflection fragments, then idea themes as
   tactile icon cards.
3. **Learn what changes you** — a flashcard fragment with stacked depth, then
   learning-area cards and pace/session controls (labels never wrap).
4. **Turn insight into action** — a vertical journey path (Today → Learn →
   Reflect → Library), no bordered row list.
5. **Your Kairos is ready** — building ring, then a personalized plan preview
   built from the user's actual choices.

## Motion
- 110ms press, 240ms state, 320ms page transitions.
- Logo breathes (3200ms, reduced-motion aware).
- Segmented progress tracks the pager; chips animate color + scale; active nav
  tab expands; cards stack with subtle rotation.

## Accessibility
- 48dp minimum interactive targets.
- WCAG AA contrast in both themes.
- Reduced-motion honored (glows hold static, breathing stops, sweeps freeze).
- Semantics describe state and action; selected nav conveyed by icon+label+state.
