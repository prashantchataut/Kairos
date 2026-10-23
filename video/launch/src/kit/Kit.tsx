// cinetic:brand-supplied coral, serif, gradient, lilac: Kairos' palette, Fraunces and its sky and
// night gradients are the app's own (BRIEF.md).
//
// The film's building blocks, drawn from the app's real components: the sky and night stages, Kai,
// the phone, and a few pieces of UI chrome. All geometry is a function of props: no state, no CSS
// animation, so every frame renders the same way twice.
import React from 'react';
import { staticFile } from 'remotion';
import { C, FONT, WORD_FACE } from '../brand/tokens';
import { H, W } from '../timeline';

/** A soft, wide drop shadow for floating cards (black only: coloured shadows read as glow). */
export const SHADOW = '0 30px 80px rgba(0,0,0,0.16), 0 6px 18px rgba(0,0,0,0.08)';
export const SHADOW_SOFT = '0 18px 50px rgba(0,0,0,0.12)';

/** The app's hero sky, top to bottom, one hue family. `shift` 0..1 slides the light band. */
export const skyGradient = (shift = 0) =>
  `linear-gradient(180deg, ${C.skyTop} ${(-20 + shift * 20).toFixed(1)}%, ${C.skyMid} ${(42 + shift * 10).toFixed(1)}%, ${C.skyFade} ${(88 + shift * 8).toFixed(1)}%)`;
/**
 * The time capsule's night and the icon's dawn, as dithered PNGs (backdrops.json, baked with
 * scripts/dither-gradient.py): a CSS gradient over the whole frame would band in 8 bits.
 */
export const nightGradient = `url(${staticFile('fx/night.png')}) center / 100% 100% no-repeat, ${C.night2}`;
export const dawnGradient = `url(${staticFile('fx/dawn.png')}) center / 100% 100% no-repeat, ${C.cobalt}`;

/**
 * Film grain at ~2%, keyed to the frame (the seed changes every 2 frames), which also hides 8-bit
 * banding in the gradients. Rendered as an SVG turbulence: deterministic for a given seed.
 */
export const Grain: React.FC<{ frame: number; opacity?: number }> = ({ frame, opacity = 0.05 }) => {
  const seed = Math.floor(frame / 2) % 97;
  return (
    <svg width={W} height={H} style={{ position: 'absolute', inset: 0, opacity, mixBlendMode: 'overlay', pointerEvents: 'none' }}>
      <filter id={`grain${seed}`}>
        <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves={2} seed={seed} stitchTiles="stitch" />
        <feColorMatrix type="saturate" values="0" />
      </filter>
      <rect width={W} height={H} filter={`url(#grain${seed})`} />
    </svg>
  );
};

/**
 * Kai, the curator: a soft blob whose outline is a polar function (8 round lobes for the curator
 * bloom), two oval eyes and a small smile. `look` moves the pupils (-1..1), `blink` 0..1 closes
 * the eyes, `breath` scales the body (the app breathes it 1.00-1.03).
 */
export const Kai: React.FC<{ size: number; color?: string; look?: [number, number]; blink?: number; breath?: number; lobes?: number; style?: React.CSSProperties }> = ({
  size,
  color = C.cobalt,
  look = [0, 0],
  blink = 0,
  breath = 1,
  lobes = 8,
  style,
}) => {
  const pts: string[] = [];
  const n = 96;
  for (let i = 0; i <= n; i++) {
    const t = (i / n) * Math.PI * 2;
    const r = 40 * (1 + 0.075 * Math.cos(lobes * t)) * breath;
    pts.push(`${(50 + Math.sin(t) * r).toFixed(2)},${(52 - Math.cos(t) * r).toFixed(2)}`);
  }
  const eyeH = 9 * (1 - 0.92 * Math.min(1, Math.max(0, blink)));
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" style={{ overflow: 'visible', display: 'block', ...style }}>
      <polygon points={pts.join(' ')} fill={color} />
      {[-11, 11].map((dx) => (
        <g key={dx}>
          <ellipse cx={50 + dx} cy={47} rx={5.4} ry={eyeH} fill={C.white} />
          {eyeH > 3 && <circle cx={50 + dx + look[0] * 1.8} cy={48 + look[1] * 2.4} r={2.6} fill={C.ink} />}
        </g>
      ))}
      <path d="M44 61 Q50 66 56 61" stroke={C.ink} strokeWidth={2.4} strokeLinecap="round" fill="none" />
    </svg>
  );
};

/** A heart outline / fill, the app's Save and Keep glyph (drawn, not a stock icon). */
export const Heart: React.FC<{ size: number; fill?: number; color?: string; stroke?: string; style?: React.CSSProperties }> = ({ size, fill = 0, color = C.coral, stroke = C.ink, style }) => {
  const d = 'M12 20.5 C6 16.4 2.5 13 2.5 8.9 C2.5 6 4.7 3.8 7.4 3.8 C9.3 3.8 11 4.9 12 6.6 C13 4.9 14.7 3.8 16.6 3.8 C19.3 3.8 21.5 6 21.5 8.9 C21.5 13 18 16.4 12 20.5 Z';
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" style={{ display: 'block', overflow: 'visible', ...style }}>
      <path d={d} fill="none" stroke={stroke} strokeWidth={1.9} strokeLinejoin="round" opacity={1 - fill} />
      <g transform={`translate(12 12) scale(${Math.max(0, fill)}) translate(-12 -12)`}>
        <path d={d} fill={color} />
      </g>
    </svg>
  );
};

/**
 * The phone: a thin navy bezel around a screen. `w` is the outer width in px; the screen is the
 * app's 400 x 860 dp canvas (the size FilmAssetsTest renders every screen at) scaled to fit, so
 * renders fill it exactly and overlays are placed in dp.
 */
export const PHONE = { sw: 400, sh: 860, bezel: 12, radius: 64 };
export const phoneSize = (w: number) => {
  const k = (w - 2 * PHONE.bezel * (w / 424)) / PHONE.sw;
  return { w, h: (PHONE.sh * k) + 2 * PHONE.bezel * (w / 424), k, bezel: PHONE.bezel * (w / 424) };
};

export const Phone: React.FC<{ w: number; children: React.ReactNode; style?: React.CSSProperties; shadow?: boolean }> = ({ w, children, style, shadow = true }) => {
  const s = phoneSize(w);
  return (
    <div
      style={{
        position: 'absolute',
        left: 0,
        top: 0,
        width: s.w,
        height: s.h,
        borderRadius: PHONE.radius * (w / 424),
        background: C.navy,
        boxShadow: shadow ? SHADOW : undefined,
        ...style,
      }}
    >
      <div
        style={{
          position: 'absolute',
          left: s.bezel,
          top: s.bezel,
          width: PHONE.sw,
          height: PHONE.sh,
          borderRadius: ((PHONE.radius - PHONE.bezel) * (w / 424)) / s.k,
          overflow: 'hidden',
          transformOrigin: '0 0',
          transform: `scale(${s.k})`,
          background: C.canvas,
        }}
      >
        {children}
        {/* the camera punch-hole */}
        <div style={{ position: 'absolute', left: PHONE.sw / 2 - 7, top: 14, width: 14, height: 14, borderRadius: 7, background: C.navy }} />
      </div>
    </div>
  );
};

/** Text in the app's interface face. */
export const ui = (size: number, weight = 500, color: string = C.ink, extra: React.CSSProperties = {}): React.CSSProperties => ({
  fontFamily: FONT.text,
  fontSize: size,
  fontWeight: weight,
  color,
  letterSpacing: '-0.01em',
  lineHeight: 1.25,
  fontVariationSettings: '"wdth" 100, "opsz" 14',
  whiteSpace: 'pre',
  ...extra,
});

/** A word in the app's word face (Fraunces, opsz 144, soft). */
export const wordStyle = (size: number, color: string = C.ink, weight = 500): React.CSSProperties => ({
  ...WORD_FACE,
  fontSize: size,
  fontWeight: weight,
  color,
  letterSpacing: '-0.02em',
  lineHeight: 1,
  whiteSpace: 'pre',
});

/** A pill chip. */
export const Chip: React.FC<{ children: React.ReactNode; bg?: string; color?: string; size?: number; style?: React.CSSProperties }> = ({ children, bg = C.surfaceSoft, color = C.ink, size = 14, style }) => (
  <div style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: `${size * 0.42}px ${size * 0.85}px`, borderRadius: 999, background: bg, ...ui(size, 500, color), ...style }}>{children}</div>
);

/** Absolute placement by transform only (never left/top driven by the frame). */
export const at = (x: number, y: number, extra = ''): React.CSSProperties => ({ position: 'absolute', left: 0, top: 0, transform: `translate(${x}px, ${y}px) ${extra}` });
