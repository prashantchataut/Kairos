// cinetic:brand-supplied coral: the moment petal is Kairos' coral (BRIEF.md).
import React from 'react';
import { C } from './tokens';

// The moment bloom, Kairos' mark, ported from app/src/main/java/com/kairos/app/ui/art/Bloom.kt so
// the film draws exactly the app's geometry: eight teardrop petals around a round core. The petal
// at one o'clock (index 1, 45°) is the "moment" petal, full length where the others alternate long
// and short, like the hand of a clock. In the film it is coral, and it is the story device.
export const BLOOM = { PETALS: 8, MOMENT: 1, R: 50, CORE: 0.26 };

export const petalLength = (i: number) => (i % 2 === 0 || i === BLOOM.MOMENT ? 1 : 0.8);

/** One petal pointing straight up from (0,0): narrow at the base, round at the tip (Bloom.petal). */
export const petalPath = (r: number, length = 1) => {
  const r0 = r * 0.24;
  const tip = r0 + (r - r0) * length;
  const w = r * 0.205;
  const l = tip - r0;
  const p = (x: number, y: number) => `${x.toFixed(3)} ${y.toFixed(3)}`;
  return [
    `M${p(0, -r0)}`,
    `C${p(w * 0.42, -r0 - l * 0.18)} ${p(w, -tip + w * 1.9)} ${p(w, -tip + w)}`,
    `C${p(w, -tip + w * 0.45)} ${p(w * 0.55, -tip)} ${p(0, -tip)}`,
    `C${p(-w * 0.55, -tip)} ${p(-w, -tip + w * 0.45)} ${p(-w, -tip + w)}`,
    `C${p(-w, -tip + w * 1.9)} ${p(-w * 0.42, -r0 - l * 0.18)} ${p(0, -r0)}`,
    'Z',
  ].join(' ');
};

/** The visual centre of the moment petal in mark units (0..100 box), for handing the device over. */
export const MOMENT_CENTRE = (() => {
  const r = BLOOM.R;
  const mid = r * 0.24 + (r - r * 0.24) * 0.62; // the fat part of the teardrop
  const a = (BLOOM.MOMENT * 45 * Math.PI) / 180;
  return { x: 50 + Math.sin(a) * mid, y: 50 - Math.cos(a) * mid };
})();

export type BloomProps = {
  size: number; // px on screen for the 100-unit box
  petal?: string;
  moment?: string | null; // null: single-colour mark
  core?: string;
  unfold?: (i: number) => number; // each petal's growth 0..1 (rotates in 28° as it grows, like the app)
  core01?: number; // core scale
  only?: 'moment'; // draw the moment petal alone: the story device before the mark exists
  skipMoment?: boolean; // leave the moment petal out (the device is still travelling to its slot)
  style?: React.CSSProperties;
};

export const Bloom: React.FC<BloomProps> = ({ size, petal = C.white, moment = C.coral, core, unfold = () => 1, core01 = 1, only, skipMoment, style }) => (
  <svg width={size} height={size} viewBox="0 0 100 100" style={{ overflow: 'visible', display: 'block', ...style }}>
    {Array.from({ length: BLOOM.PETALS }, (_, i) => {
      if (only === 'moment' && i !== BLOOM.MOMENT) return null;
      if (skipMoment && i === BLOOM.MOMENT) return null;
      const u = Math.min(1.2, Math.max(0, unfold(i)));
      if (u <= 0.001) return null;
      const rot = i * 45 - (1 - Math.min(1, u)) * 28;
      const fill = i === BLOOM.MOMENT && moment ? moment : petal;
      return <path key={i} d={petalPath(BLOOM.R, petalLength(i))} fill={fill} transform={`translate(50 50) rotate(${rot}) scale(${u})`} />;
    })}
    {only !== 'moment' && core01 > 0.001 && <circle cx={50} cy={50} r={BLOOM.R * BLOOM.CORE * core01} fill={core ?? petal} />}
  </svg>
);

/** A lone petal (the device), pointing along `rot` degrees, its base at (0,0) of the svg's centre. */
export const Petal: React.FC<{ size: number; color?: string; length?: number; style?: React.CSSProperties }> = ({ size, color = C.coral, length = 1, style }) => (
  <svg width={size} height={size} viewBox="-50 -50 100 100" style={{ overflow: 'visible', display: 'block', ...style }}>
    <path d={petalPath(50, length)} fill={color} />
  </svg>
);

/**
 * Style that draws a <Petal> of `size` px so the fat part of the petal (its visual centre) sits at
 * (cx, cy), pointing along `rot` degrees (0 = up). The svg's centre is the petal's base, so the base
 * sits 0.356 * size behind the visual centre (the base is 0.12 above the svg centre, the fat part
 * 0.62 of the way to the tip).
 */
export const placePetal = (cx: number, cy: number, size: number, rot: number): React.CSSProperties => {
  const a = (rot * Math.PI) / 180;
  const bx = cx - Math.sin(a) * 0.356 * size;
  const by = cy + Math.cos(a) * 0.356 * size;
  return { position: 'absolute', left: 0, top: 0, transform: `translate(${(bx - size / 2).toFixed(2)}px, ${(by - size / 2).toFixed(2)}px) rotate(${rot}deg)` };
};

/**
 * Transform that places an svg rendered at `size` so its box's top-left sits at (x, y) scaled by k.
 * Scale by transform instead of changing `size` per frame: no relayout, no pixel snapping.
 */
export const placeMark = (x: number, y: number, k = 1): React.CSSProperties => ({
  position: 'absolute',
  left: 0,
  top: 0,
  transformOrigin: '0 0',
  transform: `translate(${x}px, ${y}px) scale(${Math.max(1e-4, k)})`,
});
