// cinetic:brand-supplied gradient: the time capsule's night and the icon's dawn grounds are the app's own
// (BRIEF.md).
//
// The film's stage pieces: the night and dawn grounds, film grain, and the phone the app's renders
// sit in (the renders themselves, Kai and the mark are in App.tsx). All geometry is a function of
// props: no state, no CSS animation, so every frame renders the same way twice.
import React from 'react';
import { staticFile } from 'remotion';
import { C } from '../brand/tokens';
import { Device, deviceSize, SCREEN, type Tone } from './Device';
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
 * The phone (kit/Device.tsx: a Nothing Phone (4a)), placed by its top-left corner like a box, so
 * acts lay copy out around it. Children are the screen in the renders' dp (400 x 860), below the
 * status bar.
 */
export const phoneSize = (w: number) => {
  const d = deviceSize(w);
  return { w, h: d.h, k: d.k, bezel: d.bezel, top: d.bezel + SCREEN.status * d.k };
};

export const Phone: React.FC<{ w: number; x: number; y: number; rx?: number; ry?: number; rz?: number; tone?: Tone; children: React.ReactNode }> = ({ w, x, y, rx = 0, ry = 0, rz = 0, tone = 'light', children }) => {
  const d = deviceSize(w);
  return (
    <Device p={{ x: x + w / 2, y: y + d.h / 2, w, rx, ry, rz }} tone={tone}>
      <div style={{ position: 'absolute', left: 0, top: SCREEN.status, width: SCREEN.w, height: 860 }}>{children}</div>
    </Device>
  );
};

/**
 * The light stage: the app's canvas with three soft glows of its sky, lilac and sun drifting very
 * slowly, like morning through a window. Grain on top keeps the gradients from banding.
 */
export const Aurora: React.FC<{ frame: number }> = ({ frame }) => {
  const t = frame / 600;
  const g = (x: number, y: number, r: number, color: string, a: number) =>
    `radial-gradient(circle at ${x.toFixed(2)}% ${y.toFixed(2)}%, ${color}${Math.round(a * 255).toString(16).padStart(2, '0')} 0%, ${color}00 ${r}%)`;
  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        background: [
          g(14 + 6 * Math.sin(t * 2.1), 6 + 4 * Math.cos(t * 1.7), 62, C.skyMid, 0.62),
          g(90 - 5 * Math.sin(t * 1.3), 88 + 4 * Math.sin(t * 2.3), 55, C.lilac, 0.34),
          g(62 + 8 * Math.cos(t * 1.1), 104, 46, C.sun, 0.2),
          C.canvas,
        ].join(', '),
      }}
    />
  );
};
