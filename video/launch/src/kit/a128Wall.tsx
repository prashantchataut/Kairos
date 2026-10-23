// Act 1's wall through a lens. The shared kit/Wall is drawn three times: sharp; again behind a blur,
// masked to the plane's far and near edges (depth of field: only a band of the wall is in focus);
// and a third time with every card but the kept one invisible (Wall's dim at 1/0.72), so the card
// that stays sits on a layer of its own, above the darkening and out of the rack focus. The sharp
// and soft walls leave the kept card's slot empty (heroScale 0), as Wall does when a card lifts.
import React from 'react';
import { H, W } from '../timeline';
import { PITCH, scrollFor, Wall } from './Wall';

/** Wall's perspective (its outer div): the lens maths below projects plane points the same way. */
const DEPTH = 2000;
/** Wall's dim at which every card but the hero is transparent (opacity 1 - 0.72 * dim). */
const HERO_ONLY = 1 / 0.72 + 0.01;

const rad = (d: number) => (d * Math.PI) / 180;

/** Screen point of a plane point (px, py from the plane's centre) for a Wall at (cx, cy) tilted rx, rz. */
export const projectWall = (px: number, py: number, rx: number, rz: number, cx: number, cy: number) => {
  const x1 = px * Math.cos(rad(rz)) - py * Math.sin(rad(rz));
  const y1 = px * Math.sin(rad(rz)) + py * Math.cos(rad(rz));
  const y2 = y1 * Math.cos(rad(rx));
  const z2 = y1 * Math.sin(rad(rx));
  const s = DEPTH / (DEPTH - z2);
  return { x: cx + x1 * s, y: cy + y2 * s };
};

/** How far down the plane (px from its centre) column HERO_COL holds card `hero` at `scroll`. */
export const heroOffset = (hero: number, scroll: number, planeH: number) => {
  const speed = PITCH / (scrollFor(1, planeH) - scrollFor(0, planeH));
  return (scrollFor(hero, planeH) - scroll) * speed;
};

/** The depth-of-field band, in px along the plane's direction on screen from the plane's centre. */
export const LENS = { far: -430, near: 250, ramp: 190, nearSoft: 0.8 };

export type LensProps = {
  cx: number;
  cy: number;
  planeH: number;
  scroll: number;
  rx: number;
  rz: number;
  hero: number; // index in HERO_COL of the card that stays
  heroName?: string;
  lift: number; // 0..1 the kept card leaves the wall and faces the camera
  heroScale: number;
  band: number; // 0 all soft .. 1 the band in focus
  dark: number; // 0..1 the wall behind the kept card falls into shadow
  blur: number; // px of the soft wall
  shade: string; // the colour the wall falls into
};

/** Gradient geometry of the plane's direction on screen: the CSS angle, and where a point falls on it (0..1). */
const lensAxis = (rx: number, rz: number) => {
  const dx = -Math.sin(rad(rz));
  const dy = Math.cos(rad(rz)) * Math.cos(rad(rx));
  const n = Math.hypot(dx, dy);
  const ux = dx / n;
  const uy = dy / n;
  const L = Math.abs(W * ux) + Math.abs(H * uy);
  return { angle: (Math.atan2(ux, -uy) * 180) / Math.PI, L, at: (x: number, y: number) => 0.5 + ((x - W / 2) * ux + (y - H / 2) * uy) / L };
};

/** The soft wall's opacity at gradient position t (1 soft, 0 sharp) for a band opened `band`. */
const softAt = (t: number, t0: number, L: number, band: number) => {
  const far = t0 + LENS.far / L;
  const near = t0 + LENS.near / L;
  const r = LENS.ramp / L;
  const out = t < far ? Math.min(1, (far - t) / r) : t > near ? Math.min(1, (t - near) / r) * LENS.nearSoft : 0;
  return 1 - band * (1 - out);
};

export const LensWall: React.FC<LensProps> = ({ cx, cy, planeH, scroll, rx, rz, hero, heroName, lift, heroScale, band, dark, blur, shade }) => {
  const axis = lensAxis(rx, rz);
  const t0 = axis.at(cx, cy);
  // the mask: sampled every 4% along the axis (smooth enough for a 10 px blur)
  const stops = Array.from({ length: 26 }, (_, i) => {
    const t = i / 25;
    return `rgba(0,0,0,${softAt(t, t0, axis.L, band).toFixed(3)}) ${(t * 100).toFixed(1)}%`;
  }).join(', ');
  const mask = `linear-gradient(${axis.angle.toFixed(2)}deg, ${stops})`;
  // the kept card's own focus while it rides the wall: the run band's, at its place on screen
  const h = projectWall(0, heroOffset(hero, scroll, planeH), rx, rz, cx, cy);
  const heroSoft = lift > 0 ? 0 : softAt(axis.at(h.x, h.y), t0, axis.L, 1);
  const base = { cx, cy, planeH, scroll, rx, rz, hero, heroName, lift: 1, heroScale: 0 };
  const anySoft = band < 0.999 || blur > 0;
  return (
    <>
      <Wall {...base} />
      {anySoft && (
        <div style={{ position: 'absolute', left: -60, top: -60, width: W + 120, height: H + 120, overflow: 'hidden', filter: `blur(${blur.toFixed(2)}px)`, WebkitMaskImage: mask, maskImage: mask, WebkitMaskSize: `${W}px ${H}px`, maskSize: `${W}px ${H}px`, WebkitMaskPosition: '60px 60px', maskPosition: '60px 60px', WebkitMaskRepeat: 'no-repeat', maskRepeat: 'no-repeat' }}>
          <div style={{ position: 'absolute', left: 60, top: 60, width: W, height: H }}>
            <Wall {...base} />
          </div>
        </div>
      )}
      {dark > 0.001 && <div style={{ position: 'absolute', left: -60, top: -60, width: W + 120, height: H + 120, background: shade, opacity: dark }} />}
      <div style={{ position: 'absolute', inset: 0, filter: heroSoft * blur > 0.4 ? `blur(${(heroSoft * blur).toFixed(2)}px)` : undefined }}>
        <Wall cx={cx} cy={cy} planeH={planeH} scroll={scroll} rx={rx} rz={rz} hero={hero} heroName={heroName} lift={lift} heroScale={heroScale} dim={HERO_ONLY} />
      </div>
    </>
  );
};
