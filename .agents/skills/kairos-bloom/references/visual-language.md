# Visual language — implementation notes

## Palette in Compose

`ui/theme/Tokens.kt` defines `Palette` (immutable) + `LocalPalette`. New fields must have a
sensible default so previews don't break. Access only through `K.colors`.

```kotlin
data class Palette(
    val isDark: Boolean,
    val skyTop: Color, val skyMid: Color, val skyHaze: Color, val skyWarm: Color, val skyFade: Color,
    val onSky: Color, val onSkySoft: Color, val glassOnSky: Color, val glassOnSkyRim: Color,
    val canvas: Color, val surface: Color, val surfaceSoft: Color, val line: Color,
    val ink: Color, val inkSoft: Color, val inkFaint: Color,
    val accent: Color, val accentSoft: Color, val onAccent: Color,
    val gold: Color, val mint: Color, val periwinkle: Color, val rose: Color, val leaf: Color,
    val positive: Color, val warning: Color, val negative: Color, val shadow: Color,
    val tints: List<Color>, val tintInk: List<Color>,
)
```

## Sky

```kotlin
fun Modifier.sky(height: Dp? = null): Modifier = composed {
    val p = K.colors
    drawBehind {
        val h = height?.toPx() ?: size.height
        drawRect(Brush.verticalGradient(
            0f to p.skyTop,
            0.38f to p.skyMid,
            0.66f to p.skyHaze,
            0.86f to p.skyWarm,
            1f to p.skyFade,
            startY = 0f, endY = h,
        ), size = size.copy(height = h))
        // two slow orbs, drawn with radial gradients (no RenderEffect)
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = .22f), Color.Transparent), Offset(size.width * .82f, h * .14f), size.width * .5f), size.width * .5f, Offset(size.width * .82f, h * .14f))
        drawCircle(Brush.radialGradient(listOf(p.gold.copy(alpha = .16f), Color.Transparent), Offset(size.width * .1f, h * .62f), size.width * .6f), size.width * .6f, Offset(size.width * .1f, h * .62f))
    }
}
```

The last 64dp (`SkyTail`) must be free of content so the gradient melts into the paper.

## Grain

A deterministic 96×96 tile, generated once per theme and tiled with `ImageShader`; alpha 0.05
light / 0.08 dark. Draw it in `drawBehind` *after* the fill. Do not animate.

```kotlin
fun Modifier.grain(alpha: Float = .055f): Modifier = composed {
    val p = K.colors
    drawBehind {
        drawRect(grainShader(p.isDark), alpha = alpha, blendMode = BlendMode.Overlay)
    }
}
```

`grainShader` builds an `ImageBitmap` from a seeded `Random(7)` (black/white pixels at 8%
density), then `ImageShader(bitmap, TileMode.Repeated, TileMode.Repeated)`.

## Paper

```kotlin
@Composable fun PaperCard(modifier, shape = Radius.card, tint: Color? = null, tab: Color? = null, onClick, content)
```
- `shadow(18.dp, shape, ambientColor = ink.copy(alpha=.10f), spotColor = ink.copy(alpha=.12f))`
- `background(tint ?: surface)`
- `border(1.dp, line.copy(alpha = .9f))`
- optional top highlight `Brush.verticalGradient(white @ .5f → transparent, endY = 1.dp)`
- optional 6dp tab at the top-left (folders) or 12dp tape strip (notes).
- `grain()` clipped to the shape.

Rotate objects with `graphicsLayer { rotationZ = ±2f }`; never rotate text-heavy cards more
than 3°. Overlap by 8–14dp, not by half a card.

## Glass

`Modifier.frosted(shape)` uses Haze when available and falls back to `surface @ 92%`. Add a
1dp vertical gradient rim `white @ 45% → white @ 6%` and a top-left highlight line. Glass is
only legal over the sky or a photo. Never glass-on-paper.

## Type

`ui/theme/Type.kt`: add `eyebrow`, `serifDisplay`, and `number` (tabular). Fonts stay
Instrument Serif/Sans. The mono family is `FontFamily.Monospace` — do not download a new
font just for eyebrows.

## The mark

Implement once in `ui/components/Bloom.kt`:

```kotlin
@Composable fun KairosBloom(size: Dp, modifier: Modifier = Modifier, open: Float = 1f, tinted: Boolean = false, monochrome: Color? = null)
fun DrawScope.drawBloom(center: Offset, radius: Float, open: Float, petal: Brush, seed: Color)
```

Petal path in local space (pointing up), then rotate by `-90 + i*72` around the centre:

```
moveTo(0, -0.16r)
cubicTo(-0.56r, -0.52r, -0.44r, -0.96r, 0, -r)
cubicTo( 0.44r, -0.96r,  0.56r, -0.52r, 0, -0.16r)
close()
```

`open` scales each petal from 0.2→1 and rotates it from -14°→0 with a stagger of 0.12 per
petal (`open = clamp((t - i*0.12) / 0.68)`). The seed appears at `t > 0.75` with a 1.12 pop.

Tinted badge: 44dp coral-to-plum circle, bloom ivory at `inset .24`, seed gold.
Launcher/Splash: see `scripts/make_icon.py` (generates the vector drawables from the same
geometry so the mark can never drift).

## Wordmark

`KairosBloom(18.dp) + 8dp + "Kairos"` in Instrument Serif at 1.25× title size, tracking
-0.01em. On paper the mark is ink; on sky it is ivory; never tint the wordmark coral.

## Share cards

Keep the 1080 export. Each template owns a `background` composable + type layout. New templates
must include grain and the small mark bottom-centre. Palette per template:
Dawn, Paper, Riso (`#1B4AE0` + `#F5F0E4` + coral), Leaf (`#DDE8C8` + chartreuse `#D7E84A` +
leaf ink), Night (`#12102A` + gold).
