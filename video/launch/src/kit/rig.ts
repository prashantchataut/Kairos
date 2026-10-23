// One camera for the phone across the acts: a slow, continuous drift of its angle, keyed on
// absolute frames, so the device turns a little through every cut instead of resetting. Values
// are degrees; the moves are eased between keys and never stop dead (the last key holds).
import { ACT, CUE } from '../timeline';

type Key = [f: number, rx: number, ry: number, rz: number];
const KEYS: Key[] = [
  [ACT.name.from, 10, -24, 3], // it arrives turned, on the right of the name
  [ACT.word.from + 60, 6, -12, 1.5],
  [CUE.dive, 3, -5, 0], // nearly square to the camera for the dive into the card
  [ACT.feed.from + 40, 2, 4, -0.5],
  [ACT.play.from, 5, 9, -1],
  [ACT.letter.from + 60, 7, -8, 1],
  [CUE.flip, 4, -4, 0],
  [ACT.lockup.from, 8, -16, 2],
  [ACT.lockup.from + ACT.lockup.dur, 5, -10, 1],
];

const smooth = (t: number) => t * t * (3 - 2 * t);

export const tilt = (f: number) => {
  const i = Math.max(0, KEYS.findIndex((_, n) => f < (KEYS[n + 1]?.[0] ?? Infinity)));
  const a = KEYS[Math.min(i, KEYS.length - 1)];
  const b = KEYS[Math.min(i + 1, KEYS.length - 1)];
  const t = b[0] === a[0] ? 0 : smooth(Math.min(1, Math.max(0, (f - a[0]) / (b[0] - a[0]))));
  const m = (j: 1 | 2 | 3) => a[j] + (b[j] - a[j]) * t;
  return { rx: m(1), ry: m(2), rz: m(3) };
};
