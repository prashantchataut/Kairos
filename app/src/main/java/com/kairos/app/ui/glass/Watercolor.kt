package com.kairos.app.ui.glass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import com.kairos.app.ui.theme.Kairos
import kotlin.math.cos
import kotlin.math.sin

/**
 * The living pond behind every screen: slow pigment blooms on cold-press paper.
 *
 * API 33+ renders a domain-warped AGSL shader with granulation and the darker "coffee ring"
 * edge real watercolor gets as it dries. Older devices get soft radial washes that drift.
 * Time advances only ~20 times a second and stops entirely under reduced motion.
 */
@Composable
fun WatercolorBackground(
    modifier: Modifier = Modifier,
    washes: List<Color> = Kairos.colors.washes,
    intensity: Float = 1f,
    animate: Boolean = true,
) {
    val colors = Kairos.colors
    val reduced = Kairos.reducedMotion
    val tween = tween<Color>(1200)
    val c0 by animateColorAsState(washes.getOrElse(0) { colors.sky }, tween, label = "w0")
    val c1 by animateColorAsState(washes.getOrElse(1) { colors.peach }, tween, label = "w1")
    val c2 by animateColorAsState(washes.getOrElse(2) { colors.mint }, tween, label = "w2")
    val c3 by animateColorAsState(washes.getOrElse(3) { colors.lilac }, tween, label = "w3")
    val c4 by animateColorAsState(washes.getOrElse(4) { colors.butter }, tween, label = "w4")
    val paper by animateColorAsState(colors.paper, tween, label = "paper")

    val time = remember { mutableFloatStateOf(17f) }
    if (animate && !reduced) {
        LaunchedEffect(Unit) {
            var last = 0L
            var acc = 0L
            while (true) {
                withFrameNanos { now ->
                    if (last != 0L) acc += now - last
                    last = now
                    if (acc > 48_000_000L) { // ~20 fps is plenty for paint that moves this slowly
                        time.floatValue += acc / 1_000_000_000f
                        acc = 0L
                    }
                }
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { WatercolorShader() }
        Canvas(modifier.fillMaxSize()) {
            shader.update(size.width, size.height, time.floatValue, paper, listOf(c0, c1, c2, c3, c4), intensity, colors.isDark)
            drawRect(shader.brush)
        }
    } else {
        Canvas(modifier.fillMaxSize()) {
            drawRect(paper)
            val t = time.floatValue * 0.05f
            val spots = listOf(
                Triple(c0, Offset(0.15f, 0.12f), 0.75f),
                Triple(c1, Offset(0.92f, 0.22f), 0.6f),
                Triple(c2, Offset(0.1f, 0.78f), 0.7f),
                Triple(c3, Offset(0.85f, 0.7f), 0.65f),
                Triple(c4, Offset(0.5f, 0.45f), 0.45f),
            )
            spots.forEachIndexed { i, (c, o, r) ->
                val center = Offset(
                    size.width * (o.x + 0.06f * sin(t + i * 1.7f)),
                    size.height * (o.y + 0.04f * cos(t * 0.8f + i)),
                )
                val radius = size.width * r
                drawCircle(
                    Brush.radialGradient(
                        listOf(c.copy(alpha = 0.75f * intensity), c.copy(alpha = 0.25f * intensity), Color.Transparent),
                        center = center, radius = radius,
                    ),
                    radius = radius, center = center,
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class WatercolorShader {
    private val shader = RuntimeShader(SKSL)
    val brush = ShaderBrush(shader)

    fun update(w: Float, h: Float, t: Float, paper: Color, c: List<Color>, intensity: Float, dark: Boolean) {
        shader.setFloatUniform("uRes", w, h)
        shader.setFloatUniform("uTime", t)
        shader.setFloatUniform("uIntensity", intensity)
        shader.setFloatUniform("uDark", if (dark) 1f else 0f)
        shader.setColorUniform("uPaper", paper.toArgb())
        shader.setColorUniform("uC0", c[0].toArgb())
        shader.setColorUniform("uC1", c[1].toArgb())
        shader.setColorUniform("uC2", c[2].toArgb())
        shader.setColorUniform("uC3", c[3].toArgb())
        shader.setColorUniform("uC4", c[4].toArgb())
    }

    companion object {
        private const val SKSL = """
uniform float2 uRes;
uniform float uTime;
uniform float uIntensity;
uniform float uDark;
layout(color) uniform half4 uPaper;
layout(color) uniform half4 uC0;
layout(color) uniform half4 uC1;
layout(color) uniform half4 uC2;
layout(color) uniform half4 uC3;
layout(color) uniform half4 uC4;

float hash(float2 p) { return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453); }

float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + float2(1.0, 0.0)), u.x),
               mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
}

float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = p * 2.03 + float2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

half3 wash(half3 base, half3 pigment, float2 uv, float2 c, float r, float seed, float2 warp) {
    float d = length(uv + (warp - 0.5) * 0.42 - c) / r;
    float body = 1.0 - smoothstep(0.15, 1.0, d);
    float ring = smoothstep(0.72, 0.93, d) * (1.0 - smoothstep(0.93, 1.06, d));
    float gran = 0.72 + 0.56 * noise(uv * 38.0 + seed);
    float a = clamp((body * 0.62 + ring * 0.28) * gran * uIntensity, 0.0, 1.0);
    half3 multiplied = base * mix(half3(1.0), pigment, half(a));
    half3 mixed = mix(base, pigment, half(a * 0.85));
    return mix(multiplied, mixed, half(uDark));
}

half4 main(float2 fc) {
    float2 uv = fc / uRes.y;
    float aspect = uRes.x / uRes.y;
    float t = uTime * 0.035;
    float2 warp = float2(fbm(uv * 1.7 + float2(t, -t * 0.6)), fbm(uv * 1.7 + float2(4.3 - t * 0.7, 1.9 + t)));

    half3 col = uPaper.rgb;
    col = wash(col, uC0.rgb, uv, float2(aspect * (0.12 + 0.05 * sin(t * 1.3)), 0.10 + 0.03 * cos(t)), 0.46, 1.0, warp);
    col = wash(col, uC1.rgb, uv, float2(aspect * (0.95 + 0.04 * cos(t * 0.9)), 0.24 + 0.04 * sin(t * 1.1)), 0.38, 7.0, warp);
    col = wash(col, uC2.rgb, uv, float2(aspect * (0.05 + 0.05 * sin(t * 0.7 + 2.0)), 0.80 + 0.03 * sin(t)), 0.44, 13.0, warp);
    col = wash(col, uC3.rgb, uv, float2(aspect * (0.92 + 0.04 * sin(t * 0.8 + 1.0)), 0.74 + 0.04 * cos(t * 1.2)), 0.40, 21.0, warp);
    col = wash(col, uC4.rgb, uv, float2(aspect * (0.50 + 0.08 * cos(t * 0.6)), 0.47 + 0.05 * sin(t * 0.9 + 3.0)), 0.24, 34.0, warp);

    float grain = hash(floor(fc * 0.75)) - 0.5;
    float fibre = noise(uv * float2(220.0, 30.0)) - 0.5;
    col += half3((grain * 0.018 + fibre * 0.012) * (1.0 - uDark * 0.5));
    return half4(col, 1.0);
}
"""
    }
}
