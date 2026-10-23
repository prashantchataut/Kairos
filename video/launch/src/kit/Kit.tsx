// cinetic:brand-supplied gradient: the time capsule's night and the icon's dawn grounds are the app's own
// (BRIEF.md).
//
// The film's stage pieces: the night and dawn grounds, film grain, and the phone the app's renders
// sit in (the renders themselves, Kai and the mark are in App.tsx). All geometry is a function of
// props: no state, no CSS animation, so every frame renders the same way twice.
import React from 'react';
import { staticFile } from 'remotion';
import { C } from '../brand/tokens';
import { H, W } from '../timeline';

/** A soft, wide drop shadow for floating cards (black only: coloured shadows read as glow). */
export const SHADOW = '0 30px 80px rgba(0,0,0,0.16), 0 6px 18px rgba(0,0,0,0.08)';
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
