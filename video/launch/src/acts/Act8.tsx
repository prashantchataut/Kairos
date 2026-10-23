// cinetic:brand-supplied coral, serif, gradient: the bloom, the wordmark and the icon's cobalt sky
// are Kairos' own (BRIEF.md): the bloom is drawBloom's own unfold frames, the lockup the app's
// Wordmark render.
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { MOMENT_CENTRE, Petal, placePetal } from '../brand/Mark';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, hitPulse, lmix, mix, prog, rand } from '../lib/anim';
import { BLOOM_R, BloomArt, CardArt, WORDMARK, WordmarkArt } from '../kit/App';
import { dawnGradient, Grain, nightGradient } from '../kit/Kit';
import { ACT, copy, CUE, f60, H, TOTAL, W } from '../timeline';
import { TRIPTYCH } from './Act7';

// ACT 8 - THE MOMENT. The three cards fly off toward the edges (tr-subtract-reveal), but the coral
// sticker of the middle card stays where it was: the moment petal, alone. Night turns to the
// app icon's cobalt sky. The petal glides to its slot and the app's own bloom unfolds around it
// (drawBloom's frames: clockwise from one o'clock, then the core, landing 2); the coral petal
// whitens into it, so the mark ends exactly as the app draws it. The bloom steps aside and shrinks
// into the app's Wordmark, the name sliding out from behind it; "Now on Android" follows. The
// lockup keeps building on a slow push to the last frame.

const NAME = copy('wordmark');
const ANDROID = copy('android');
const WH = 220 * U; // the Wordmark's height on screen: its mark is WH across
const WW = (WH * WORDMARK.w) / WORDMARK.h;
const BIG = { size: 440 * U, x: W / 2, y: H / 2 - 10 * U }; // the bloom (2 x its radius), centred, before the lockup
const SLIDE = { from: NAME.in, to: CUE.lockup }; // the bloom steps aside as the name slides out
/** drawBloom's unfold frames, timed so the core lands on CUE.core (it starts at t 0.62 of 40 frames). */
const UNFOLD = { from: CUE.unfold + f60(20), to: CUE.unfold + f60(20) + (CUE.core - CUE.unfold - f60(20)) / 0.72 };

/** Where the middle card's sticker petal is at the start of the act (screen px, Act 7's last pose). */
const STICKER = (() => {
  const c = TRIPTYCH[1];
  const s = c.h * 0.13;
  const left = c.x + c.w + c.h * 0.03 - s;
  const top = c.y - c.h * 0.03;
  return { x: left + s / 2 + (3.96 * s) / 100, y: top + s / 2 - (3.96 * s) / 100, size: s };
})();

export const Act8: React.FC = () => {
  const f = useCurrentFrame() + ACT.lockup.from;

  // --- the stage: night to the icon's cobalt sky ------------------------------------------
  const dawn = prog(f, CUE.scatter, CUE.core, E.smooth);

  // --- the scatter: each card along the line from the centre through its own centre --------
  const scatter = prog(f, CUE.scatter - 1, CUE.scatter + f60(30), E.contact);

  // --- the lockup geometry: the Wordmark centred; its mark is where the bloom ends ---------
  const left = (W - WW) / 2;
  const top = H / 2 - WH / 2 - 24 * U;
  const lock = prog(f, SLIDE.from, SLIDE.to, E.inOut);
  const bloom = { size: lmix(BIG.size, WH, lock), x: mix(BIG.x, left + WH / 2, lock), y: mix(BIG.y, top + WH / 2, lock) };
  const bloomLeft = bloom.x - bloom.size / 2;
  const bloomTop = bloom.y - bloom.size / 2;
  const slot = { x: bloomLeft + (MOMENT_CENTRE.x / 100) * bloom.size, y: bloomTop + (MOMENT_CENTRE.y / 100) * bloom.size };

  // --- the device: from the sticker to the moment petal's slot, then it whitens into the mark --
  const go = prog(f, CUE.scatter + f60(6), CUE.unfold + f60(26), E.glide);
  const dev = { x: mix(STICKER.x, slot.x, go), y: mix(STICKER.y, slot.y, go), size: lmix(STICKER.size, bloom.size, go) };
  const whiten = prog(f, CUE.core - f60(6), CUE.core + f60(24), E.smooth);

  // --- the bloom: drawBloom's own frames; the core lands on CUE.core -----------------------
  const t = prog(f, UNFOLD.from, UNFOLD.to, E.linear);
  const art = bloom.size * (0.5 / BLOOM_R) * (1 + 0.04 * hitPulse(f - CUE.core, 1, 7));

  // --- camera: a slow push through the lockup to the last frame ---------------------------
  const push = lmix(1, 1.07, prog(f, SLIDE.from, TOTAL, E.dolly)) * (1 + 0.015 * hitPulse(f - CUE.lockup, 2, 6));

  // the name rides out from behind the bloom's moving right edge, clipped by it
  const edge = bloomLeft + bloom.size;
  const textX = (WH * WORDMARK.text) / WORDMARK.h; // where the name starts in the Wordmark
  const nameShift = -(1 - lock) * (WW - WH * 0.6);

  return (
    <AbsoluteFill style={{ background: nightGradient, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0, background: dawnGradient, opacity: dawn }} />
      {scatter < 1 &&
        TRIPTYCH.map((c, i) => {
          const cx = c.x + c.w / 2 - W / 2;
          const cy = c.y + c.h / 2 - H / 2 + 0.01;
          const len = Math.hypot(cx, cy) || 1;
          const d = scatter * (1300 * U) * (1 + i * 0.1);
          const dx = i === 1 ? 0 : (cx / len) * d;
          const dy = i === 1 ? d * 0.9 : (cy / len) * d + d * 0.25;
          const rot = scatter * (rand(`card${i}`) - 0.5) * 30;
          return (
            <div key={c.shape} style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${c.x + dx}px, ${c.y + dy}px) rotate(${rot}deg)` }}>
              <CardArt shape={c.shape} design={c.design} h={c.h} />
            </div>
          );
        })}
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${W / 2}px ${H / 2}px`, transform: `scale(${push})` }}>
        <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${bloom.x - art / 2}px, ${bloom.y - art / 2}px)` }}>
          <BloomArt size={art} ink="white" t={t} />
        </div>
        {whiten < 1 && <Petal size={dev.size} color={C.coral} style={{ ...placePetal(dev.x, dev.y, dev.size, 45), opacity: 1 - whiten }} />}
        {lock > 0 && (
          <div style={{ position: 'absolute', left: 0, top: 0, width: W, height: H, overflow: 'hidden', clipPath: `inset(0 0 0 ${Math.max(edge + 6 * U, left + textX - 4 * U)}px)` }}>
            <WordmarkArt h={WH} ink="white" style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${left + nameShift}px, ${top}px)` }} />
          </div>
        )}
        <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${left + textX + 4 * U}px, ${top + WH + 26 * U}px)` }}>
          <Words text={ANDROID.text} id={ANDROID.id} frame={f} start={ANDROID.in} size={TYPE.secondary.size * U} style={{ ...typeStyle('secondary'), color: C.white, opacity: 0.88 }} />
        </div>
      </div>
      <Grain frame={f} opacity={0.045} />
    </AbsoluteFill>
  );
};
