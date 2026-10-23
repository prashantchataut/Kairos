// Timing for Acts 1, 2 and 8, shared by the picture and by src/sync.ts: pure numbers, no DOM, so
// the sound events are computed from the same curves the acts draw with. Frames are absolute.
import { arrive, arriveK, clamp, E, type Ease, prog } from '../lib/anim';
import { settleOf } from '../lib/sync';
import { ACT, b, copy, CUE, f60, TOTAL } from '../timeline';

// ------------------------------------------------------------------------------------------
// Act 1: the wall's speed ramp. The film opens mid-rush; the wall eases into a slow glide while
// "Days" lands, surges on "just pass.", cruises fast through "Some moments" and brakes hard to a
// dead stop on CUE.stop with Seneca's card at the plane's centre. The shape is in units of the
// peak speed; scale() turns it into plane px per frame so the scroll ends exactly on the card.

/** Speed keys [frame, share of the peak], eased between (zero slope at every key). */
const RAMP: [number, number][] = [
  [0, 1],
  [f60(30), 0.34], // the glide: "Days" is read over a wall you can see
  [f60(70), 0.3],
  [f60(114), 1], // the surge: "just pass."
  [f60(238), 0.9], // the brake starts
];
/** The brake: the last share of the peak falls to zero on CUE.stop with a power curve (no settle, no bounce). */
const BRAKE = { from: f60(238), pow: 2.4 };

const smooth = (t: number) => t * t * (3 - 2 * t);

/** The ramp's shape at frame f (0..1 of the peak speed). */
export const rampShape = (f: number) => {
  if (f >= CUE.stop) return 0;
  if (f >= BRAKE.from) {
    const t = (f - BRAKE.from) / (CUE.stop - BRAKE.from);
    return RAMP[RAMP.length - 1][1] * Math.pow(1 - t, BRAKE.pow);
  }
  if (f <= RAMP[0][0]) return RAMP[0][1];
  for (let i = 0; i < RAMP.length - 1; i++) {
    const [a, va] = RAMP[i];
    const [c, vc] = RAMP[i + 1];
    if (f <= c) return va + (vc - va) * smooth((f - a) / (c - a));
  }
  return RAMP[RAMP.length - 1][1];
};

// The distance covered from frame f to the stop, in shape units, tabulated at 1/8 frame.
const STEP = 1 / 8;
const N = Math.ceil(CUE.stop / STEP);
const TAIL_SUM: number[] = (() => {
  const t = new Array<number>(N + 1).fill(0);
  for (let i = N - 1; i >= 0; i--) {
    const f0 = i * STEP;
    const f1 = Math.min(CUE.stop, f0 + STEP);
    t[i] = t[i + 1] + ((rampShape(f0) + 4 * rampShape((f0 + f1) / 2) + rampShape(f1)) / 6) * (f1 - f0); // Simpson
  }
  return t;
})();

/** Shape units still to travel at frame f (0 from the stop on). Fractional frames interpolate. */
export const rampLeft = (f: number) => {
  if (f >= CUE.stop) return 0;
  if (f <= 0) return TAIL_SUM[0] + rampShape(0) * -f;
  const x = f / STEP;
  const i = Math.floor(x);
  const a = TAIL_SUM[Math.min(N, i)];
  const c = TAIL_SUM[Math.min(N, i + 1)];
  return a + (c - a) * (x - i);
};

/** The frame the surge peaks (where its whoosh apex goes). */
export const SURGE_PEAK = RAMP[3][0];

// ------------------------------------------------------------------------------------------
// Copy: the film's blur-in words (fx/a128Type). A word rises `rise` em and comes out of a blur over
// the pace's wordDur, `stagger` apart, so a fragment is fully in on COPY's `resolved`.

export const WORD = { dur: f60(26), stagger: f60(6), ease: E.out as Ease, exitDur: f60(14), exitStagger: f60(2) };

/** First frame of word i of a COPY fragment, and its end (the last word ends on `resolved`). */
export const wordSpan = (id: string, i: number) => {
  const c = copy(id);
  const s = c.in + i * WORD.stagger;
  return [s, s + WORD.dur] as const;
};

/** Share of a word's entrance by which its blur has cleared (fx/a128Type draws the blur on this). */
export const FOCUS = 0.55;

/**
 * The frame a fragment reads as landed (its one `text` sound): its last word is in focus (most of
 * the blur gone) and its drawn rise is within 3% of home (through arrive(), as the words are drawn).
 */
export const wordsSettle = (id: string, size: number, rise: number) => {
  const n = copy(id).text.split(/\s+/).filter(Boolean).length;
  const [s, e] = wordSpan(id, n - 1);
  const focus = Math.ceil(s + FOCUS * 0.85 * (e - s));
  const k = arriveK(rise * size, e - s, WORD.ease);
  for (let f = Math.ceil(s); f < e; f++) {
    const p = prog(f, s, e, WORD.ease);
    if (rise === 0 || 1 - arrive(p, k) <= 0.03) return Math.max(f, focus);
  }
  return Math.ceil(e);
};

// ------------------------------------------------------------------------------------------
// The name (Acts 2 and 8): the icon rises over a horizon line and unfolds the app's way, then the
// name's letters rise from behind the same line, the last one home on COPY's `resolved`.

export type NameBeat = {
  line: [number, number]; // the horizon line draws across
  icon: [number, number]; // the icon rises from behind the line
  petal0: number; // the moment petal starts to grow
  petalGap: number; // frames between petals (clockwise from the moment petal)
  petalDur: number;
  core: [number, number]; // the core grows; it lands on core[1]
  letters: { from: number; gap: number; dur: number }; // letter i rises over [from + i * gap, + dur]
  out: number; // the lockup starts to leave (Act 2) or never (Act 8: TOTAL)
};

const lettersFor = (id: string, n: number, dur: number) => {
  const c = copy(id);
  return { from: c.in, gap: (c.resolved - c.in - dur) / Math.max(1, n - 1), dur };
};

/** A frame after the film: for things that never leave (interpolate() needs finite frames). */
export const NEVER = TOTAL + 10000;

/**
 * Act 2. The morning's columns uncover the left of frame last (they rise centre first), so the line
 * draws while they rise, the icon comes up as they close, its petals open on 32nds and its core is
 * home on beat 3 of bar 4; the letters follow the icon left to right, home on COPY's `resolved`.
 */
export const NAME2: NameBeat = {
  line: [ACT.name.from + f60(10), ACT.name.from + f60(44)],
  icon: [ACT.name.from + f60(32), ACT.name.from + f60(58)],
  petal0: ACT.name.from + f60(38),
  petalGap: f60(3.75),
  petalDur: f60(18),
  core: [b(4, 3) - f60(12), b(4, 3)],
  letters: lettersFor('name.1', 6, f60(18)),
  out: copy('name.1').out,
};

/** Act 8: the icon rises as the columns close and blooms on 64ths, its core lands on CUE.core; the name lands on CUE.lockup. */
export const NAME8: NameBeat = {
  line: [ACT.lockup.from + f60(6), ACT.lockup.from + f60(42)],
  icon: [ACT.lockup.from + f60(24), ACT.lockup.from + f60(50)],
  petal0: ACT.lockup.from + f60(26),
  petalGap: f60(2.5),
  petalDur: f60(16),
  core: [CUE.core - f60(12), CUE.core],
  letters: lettersFor('wordmark', 6, f60(20)),
  out: NEVER,
};

/** A petal's growth (XML index i) at frame f, unfolding clockwise from the moment petal. */
export const petalAt = (nb: NameBeat, order: number[], f: number) => (i: number) => {
  const s = nb.petal0 + order.indexOf(i) * nb.petalGap;
  return prog(f, s, s + nb.petalDur, E.outSoft);
};

/** The frame the name reads as landed (its one sound): the last letter's drawn rise within 3% of home. */
export const nameSettle = (nb: NameBeat, letters: number, size: number) => {
  const s = nb.letters.from + (letters - 1) * nb.letters.gap;
  const e = s + nb.letters.dur;
  const k = arriveK(size * 0.88, e - s, E.out); // the rise: from wholly under the horizon, ~0.88 of the size
  return settleOf(s, e, (t) => arrive(E.out(t), k));
};

/** Type sizes of Acts 1, 2 and 8 in px at 1080p (multiply by U): the acts and their sounds read these. */
export const SIZES = { days: 100, name2: 200, means: 50, name8: 226, android: 58 };

// ------------------------------------------------------------------------------------------
// The morning rising in stepped columns (Acts 2 and 8; Act 6 closes the day the same way): the
// columns rise from the bottom edge, centre first, each on E.smooth, and close into one plate.

export const COLUMNS = { n: 12, delay: f60(2.4), dur: f60(40) };

/** Height (0..1 of the frame) of column i at frame f for a rise that starts at `from`. */
export const columnAt = (f: number, from: number, i: number) => {
  const d = Math.abs(i - (COLUMNS.n - 1) / 2) * COLUMNS.delay;
  return prog(f, from - 1 + d, from - 1 + d + COLUMNS.dur, E.smooth);
};

/** The frame the last column closes. */
export const columnsClosed = (from: number) => from - 1 + ((COLUMNS.n - 1) / 2) * COLUMNS.delay + COLUMNS.dur;

export { clamp };
