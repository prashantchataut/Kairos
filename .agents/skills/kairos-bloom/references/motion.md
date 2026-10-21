# Motion — Kairos recipes

Theory lives in `.agents/skills/motion-design/` (LottieFiles, MIT). This file is the
project-specific implementation contract.

## Tokens

```kotlin
object Motion {
    val Ease = CubicBezierEasing(0.22f, 0.61f, 0.16f, 1f)      // entrance
    val EaseExit = CubicBezierEasing(0.4f, 0f, 1f, 1f)          // exit
    fun <T> soft() = spring<T>(dampingRatio = .82f, stiffness = 260f)
    fun <T> settle() = spring<T>(dampingRatio = .9f, stiffness = 180f)
    fun <T> pop() = spring<T>(dampingRatio = .55f, stiffness = 420f)
    fun <T> press() = spring<T>(dampingRatio = .7f, stiffness = 900f)
    fun <T> fade(ms: Int = 260) = tween<T>(ms, easing = Ease)
    const val quick = 140; const val standard = 260; const val slow = 420
    const val bloom = 1000; const val ambient = 8000
}
```

## Reusable modifiers/composables

- `Modifier.pressable(scale = .97f, haptic = true, onClick)` — compress + `TextHandleMove`.
- `Modifier.reveal(index, visible)` — 16dp rise + fade + 0.985 scale, stagger `index * 45ms`.
- `Modifier.float(period = 6000, amplitude = 3.dp)` — sine translationY; off in calm mode.
- `Modifier.breathe(min = .985f, max = 1.015f, period = 8000)` — scale loop.
- `rememberBloom(trigger)` → `Animatable<Float>` 0→1 over 1000ms with `EaseOutCubic`; expose
  `open` and a `Burst` overlay.
- `AnimatedNumber(value)` — `AnimatedContent` with slide-up digits, 30ms stagger.
- `SuccessSeal(visible, label)` — ring sweep + check draw + label crossfade.

## Choreography recipes

### Onboarding welcome (total 2.2s)
| t | event |
|---|---|
| 0 | sky + grain already visible; petals start drifting |
| 120 | bloom petals unfurl (stagger 120ms, 900ms each) |
| 500 | hero word fades/rises 24dp with blur 8→0 (600ms) |
| 750 | tagline serif italic fades 12dp (500ms) |
| 950 | card fan: left -8°, right +7°, centre 0, rise 30dp, stagger 90ms |
| 1300 | Begin pill rises 20dp, progress petals fill |

### Save burst (720ms)
1. `heart` scale 1→0.82 (90ms, press) → 1.18 (140ms, pop).
2. A gold seed dot flies from the heart to the bloom badge (280ms, `Ease`), leaving a 6px trail.
3. 12 petals spawn at the badge: angle random, speed 160–300dp/s, gravity 460, spin ±200°,
   life 620–800ms, colour sampled from coral/gold/rose/mint.
4. "Kept" tag: paper pill 28dp, slides up 8dp + fades, out at 1.6s.
5. Haptic `TextHandleMove` at 0 and `LongPress` at burst. Calm: single 120ms gold flash.

### API key success (900ms)
1. While validating: the field rim rotates a 2dp gradient arc (1200ms loop).
2. Success: rim draws mint from left to right (400ms), lock icon rotates -8° and crossfades to
   a check that draws (280ms), ring expands 0→1.15 and fades (600ms).
3. Status text "Not connected" → "Connected" crossfades 8dp up; a tiny gold seed pops.
4. Failure: 3× horizontal shake ±6dp (320ms), rim coral, message slides down 6dp.

### Adaptive slider
- Thumb scale: `if (dragging) 1.22 else 1`, `spring.pop`.
- Bubble: `AnimatedVisibility` with scale 0.8→1 + 8dp rise; auto-hide after 900ms.
- Fill: `animateFloatAsState(value, spring(damping .9, stiffness 500))`.
- Detents: haptic tick when crossing a step; active tick scales 1.4 and takes the colour.
- Track gradient: interpolate the three-stop domain by value; never hard-cut.
- On release: settle with `pop`; value label stays for 900ms.

### Page transitions
```kotlin
enter = fadeIn(tween(260, easing = Ease)) + slideInVertically(spring(.85f, 260f)) { it / 18 }
exit  = fadeOut(tween(180)) + scaleOut(targetScale = .985f)
popEnter = fadeIn(tween(240)) + scaleIn(initialScale = .99f)
popExit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { it / 20 }
```

### Tab switch
Incoming fades + scales 0.99→1 over 260ms; outgoing fades 160ms. The dock indicator slides
with `spring.soft`, the icon lifts 2dp, and the label crossfades.

### Capsule seal break
Envelope flap rotates -180° (600ms, `Ease`), wax seal scales 1→1.3→0 with a gold flash,
letter rises 40dp and unfolds (scaleY .6→1, 700ms), petals drift out. Haptic at the crack.

## Performance rules
- One `Canvas` per particle system; precompute angles/velocities.
- Never allocate `Brush` inside `draw` per frame; hoist.
- Animate `graphicsLayer` (translation/scale/alpha/rotation) — no relayout per frame.
- Haze blur only on static headers/docks, never on scrolling content.
- Ambient loops pause when `LocalLifecycleOwner` is not RESUMED and when `K.calm`.

## Accessibility
- Honour `Settings.Global.ANIMATOR_DURATION_SCALE == 0`.
- `K.calm` switch in You must kill particles, drift and travel > 8dp.
- Motion never carries the only signal: pair colour/scale with text or icon changes.
