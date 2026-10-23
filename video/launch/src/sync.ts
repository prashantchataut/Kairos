// The sound events of the film, computed from the picture code. scripts/export-cues.ts calls the
// default export and writes out/cues.json; scripts/audio/score.py voices each event with the
// film's sound kit (audio/kit.json) on its exact frame (references/sound.md in the cinetic skill).
//
// The rules this file follows:
// - Every sound has a visible cause, but not every picture event gets one: discrete state changes
//   are sounded (the hand stopping, a heart filling, a card turning, the seal, the core, the
//   lockup); continuous motion, scrolls and most cuts are left to the music.
// - Impacts on the contact frame, pops on the 50% frame, whooshes with their apex on the velocity
//   peak of the move they belong to.
// - Text gets one soft `text` sound per fragment, on the frame it comes to rest, never per word.
// - The lockup is a stop-down and ONE clean element on the wordmark.
import { TYPE, U } from './brand/tokens';
import { chainResolves } from './fx/Chain';
import { E } from './lib/anim';
import { peak } from './lib/sync';
import { ACT, copy, CUE, f60, FPS, TOTAL, W } from './timeline';

export type Kind =
  | 'tick' | 'tock' | 'hit' | 'land' | 'pop' | 'whoosh' | 'riser'
  | 'drop' | 'key' | 'click' | 'snap' | 'swell' | 'bell' | 'suck' | 'text';

export type SoundEvent = {
  f: number; // absolute frame the sound belongs on (fractional is fine)
  kind: Kind;
  weight?: number; // 0..2, 1 = normal
  pan?: number; // -1 left .. +1 right
  pitch?: number | string;
  apexFrac?: number; // whoosh / suck: where in its length the loudest point sits (lands on f)
  dur?: number; // frames: whoosh, riser, swell and suck length
  variant?: string;
  id?: string;
};

const sec = (s: number) => Math.round(s * FPS);
const panX = (x: number) => Math.max(-0.8, Math.min(0.8, ((x - W / 2) / (W / 2)) * 0.8));
const actOf = (f: number) => Object.entries(ACT).find(([, a]) => f >= a.from && f < a.from + a.dur)?.[0] ?? 'tail';

/** A move from frame a to b on `ease`: a whoosh as long as the move, its apex on the velocity peak. */
const move = (from: number, to: number, ease: (t: number) => number, weight: number, pan: number, id: string): SoundEvent => {
  // an ease that peaks at an end (E.in, E.out) puts the apex just inside the move, never on its edge
  const apex = Math.min(to - 2, Math.max(from + 2, peak(from, to, ease)));
  const dur = Math.max(sec(0.3), to - from);
  const start = apex - Math.min(apex - from, dur * 0.85);
  return { f: apex, kind: 'whoosh', weight, pan, dur, apexFrac: Math.min(0.9, Math.max(0.1, (apex - start) / dur)), id };
};

/** A fragment drawn by <Words>: it reads as landed a few frames before its last word is fully in. */
const landed = (id: string) => copy(id).resolved - f60(4);

export default function cues(): SoundEvent[] {
  const ev: SoundEvent[] = [];
  const text = (f: number, id: string, pan = 0) => ev.push({ f, kind: 'text', weight: 0.8, pan, id });

  // 1 Chronos: the fragments on the left; the hand stops dead (the music stops with it).
  const line = TYPE.line.size * U;
  chainResolves('days', { size: line }).forEach((f, i) => text(f, `days${i + 1}`, -0.4));
  chainResolves('stay', { size: line }).forEach((f, i) => text(f, `stay${i + 1}`, -0.4));
  ev.push({ f: CUE.stop, kind: 'snap', weight: 1.2, pan: 0.35, id: 'hand-stops' });

  // 2 Name: the dive into the hand, the dot landing on the i, the meaning.
  ev.push(move(CUE.pushIn - 1, CUE.flood, E.in, 1.1, 0.3, 'dive'));
  ev.push({ f: CUE.dot, kind: 'pop', weight: 0.8, pan: 0.05, id: 'dot-on-i' });
  text(landed('name.2'), 'right-moment');

  // 3 Word: the petal lands as the Save heart; the word rewrites twice; the dive up.
  ev.push({ f: CUE.heart, kind: 'pop', weight: 1, pan: -0.05, id: 'save-heart' });
  text(landed('word.1'), 'a-word', -0.45);
  text(landed('word.2'), 'every-feeling', 0.45);
  ev.push({ f: CUE.rewrite1, kind: 'tick', variant: 'crisp', weight: 0.8, id: 'rewrite1' });
  ev.push({ f: CUE.rewrite2, kind: 'tick', variant: 'crisp', weight: 0.8, id: 'rewrite2' });
  ev.push(move(CUE.through, ACT.word.from + ACT.word.dur, E.in, 0.9, 0, 'whip-up'));

  // 4 Feed: Keep fills coral.
  text(landed('learns.1'), 'it-learns', -0.45);
  ev.push({ f: CUE.keep, kind: 'pop', weight: 1, pan: 0.15, id: 'keep' });
  text(landed('learns.2'), 'what-you-keep', 0.45);

  // 5 Play: the cards burst out of the phone; two turn over.
  ev.push(move(CUE.fan, CUE.fan + f60(34), E.out, 0.8, 0, 'fan'));
  chainResolves('play', { size: line }).forEach((f, i) => text(f, `play${i + 1}`));
  ev.push({ f: CUE.turn1 + f60(9), kind: 'click', weight: 0.9, pan: 0, id: 'turn1' });
  ev.push({ f: CUE.turn2 + f60(9), kind: 'click', weight: 0.9, pan: -0.5, id: 'turn2' });

  // 6 Letter: the greeting is written (one soft key per letter), the seal lands, it opens later.
  const dear = copy('ui.dear');
  for (let i = 0; i < dear.text.length; i++) if (dear.text[i] !== ' ') ev.push({ f: dear.in + i * f60(2), kind: 'key', weight: 0.6, pan: -0.1, id: `dear${i}` });
  text(landed('write.1'), 'write-to', -0.45);
  ev.push({ f: CUE.seal, kind: 'land', weight: 1.2, pan: 0, id: 'seal' });
  text(landed('write.2'), 'future-you', 0.45);
  ev.push({ f: CUE.opens + f60(13), kind: 'tick', weight: 0.6, id: 'opens' });

  // 7 Share: the card changes shape twice.
  text(landed('share.1'), 'share', -0.45);
  ev.push({ f: CUE.story, kind: 'snap', weight: 1.5, id: 'to-story' });
  text(landed('share.2'), 'what-stays', 0.45);
  ev.push({ f: CUE.square, kind: 'snap', weight: 1.5, id: 'to-square' });

  // 8 Lockup: the cards fly off, the core lands, then the stop-down and one clean hit.
  ev.push(move(CUE.scatter - 1, CUE.scatter + f60(30), E.contact, 1, 0, 'scatter'));
  ev.push({ f: CUE.core, kind: 'land', variant: 'light', weight: 0.9, pan: panX(W / 2), id: 'core' });
  ev.push({ f: CUE.lockup, kind: 'hit', weight: 1, pan: 0.05, id: 'lockup' });
  text(landed('android'), 'android', 0.1);

  for (const e of ev) {
    if (!(e.f >= 0 && e.f < TOTAL)) throw new Error(`sync: ${e.id} at f=${e.f} is outside the film (0..${TOTAL - 1})`);
    e.id = `${actOf(e.f)}:${e.id}`;
  }
  return ev.sort((a, b) => a.f - b.f);
}
