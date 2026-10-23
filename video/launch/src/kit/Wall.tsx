// A wall of the app's real share cards (the studio's renders), on a plane tilted away from the
// camera, its columns sliding up at slightly different speeds. Act 1 runs it fast (days passing)
// and stops it on one card; Act 7 runs it slow (what people keep and share). The hero card can
// lift off the wall and turn to face the camera.
import React from 'react';
import { Img, staticFile } from 'remotion';

export const PINS = [
  'card_pin_bold_sky', 'thought_pin_1_starry_ink', 'card_pin_glow_peach', 'thought_pin_3_blurbloom_sky', 'card_pin_eclipse_ink',
  'thought_pin_4_stage_lavender', 'card_pin_garden_lavender', 'thought_pin_6_window_sky', 'card_pin_bloom_sky', 'thought_pin_2_breathe_peach',
  'card_pin_poster_mint', 'thought_pin_5_eclipse_ink', 'card_pin_swiss_sky', 'thought_pin_7_glow_peach', 'card_pin_window_sky',
  'card_pin_starry_ink', 'card_pin_breathe_peach', 'card_pin_stage_mint', 'card_pin_blurbloom_sky',
];
/** Seneca: "Nothing, Lucilius, is ours, except time." The card the film keeps. */
export const HERO = 'thought_pin_0_poster_mint';

export const WALL = { cols: 5, cw: 270, gap: 30, rx: 34, rz: -16 };
const CH = WALL.cw * 1.5;
export const PITCH = CH + WALL.gap;
const SPEED = [1.0, 1.12, 0.94, 1.06, 0.9];
const OFF = [0, 0.42, 0.13, 0.66, 0.31].map((o) => o * PITCH);
export const HERO_COL = 2;

/** The scroll (px) at which column HERO_COL holds card `i` at the plane's centre. */
export const scrollFor = (i: number, planeH: number) => (i * PITCH + OFF[HERO_COL] + CH / 2 - planeH / 2) / SPEED[HERO_COL];

export const Wall: React.FC<{
  cx: number; // the plane's centre on screen
  cy: number;
  scroll: number;
  hero?: number; // index in HERO_COL that shows the hero card
  lift?: number; // 0..1 the hero leaves the wall and faces the camera
  dim?: number; // 0..1 the rest of the wall drops back (rack focus)
  planeH?: number;
  rx?: number;
  rz?: number;
  heroScale?: number;
  heroName?: string;
}> = ({ cx, cy, scroll, hero, lift = 0, dim = 0, planeH = 2400, rx = WALL.rx, rz = WALL.rz, heroScale = 1.25, heroName = HERO }) => {
  const planeW = WALL.cols * (WALL.cw + WALL.gap) - WALL.gap;
  const cards: React.ReactNode[] = [];
  let heroEl: React.ReactNode = null;
  for (let c = 0; c < WALL.cols; c++) {
    const s = scroll * SPEED[c] - OFF[c];
    const first = Math.floor((s - CH) / PITCH);
    for (let i = first; i <= first + Math.ceil(planeH / PITCH) + 1; i++) {
      const y = i * PITCH - s;
      const isHero = c === HERO_COL && i === hero;
      const name = isHero ? heroName : PINS[(((i + c * 4) % PINS.length) + PINS.length) % PINS.length];
      const x = c * (WALL.cw + WALL.gap);
      const el = (
        <div
          key={`${c}:${i}`}
          style={{
            position: 'absolute',
            left: 0,
            top: 0,
            width: WALL.cw,
            height: CH,
            borderRadius: 14,
            overflow: 'hidden',
            transform: isHero
              ? `translate3d(${x}px, ${y}px, 0) translateZ(${lift * 320}px) rotateZ(${-rz * lift}deg) rotateX(${-rx * lift}deg) scale(${1 + (heroScale - 1) * lift})`
              : `translate3d(${x}px, ${y}px, 0)`,
            boxShadow: isHero ? `0 ${30 + 40 * lift}px ${60 + 60 * lift}px rgba(0,0,0,${0.35 + 0.2 * lift})` : '0 18px 40px rgba(0,0,0,0.3)',
            opacity: isHero ? 1 : 1 - 0.72 * dim,
            filter: !isHero && dim > 0.01 ? `blur(${(dim * 6).toFixed(2)}px)` : undefined,
          }}
        >
          <Img src={staticFile(`app/${name}.jpg`)} style={{ width: '100%', height: '100%', display: 'block' }} />
        </div>
      );
      if (isHero) heroEl = el;
      else cards.push(el);
    }
  }
  return (
    <div style={{ position: 'absolute', inset: 0, perspective: 2000, perspectiveOrigin: `${cx}px ${cy}px` }}>
      <div
        style={{
          position: 'absolute',
          left: cx - planeW / 2,
          top: cy - planeH / 2,
          width: planeW,
          height: planeH,
          transformStyle: 'preserve-3d',
          transform: `rotateX(${rx}deg) rotateZ(${rz}deg)`,
        }}
      >
        {cards}
        {heroEl}
      </div>
    </div>
  );
};
