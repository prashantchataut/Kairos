// cinetic:brand-supplied coral, serif, gradient: the bloom, the Fraunces wordmark and the icon's
// cobalt sky are Kairos' own (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { Bloom, MOMENT_CENTRE, Petal, placePetal } from '../brand/Mark';
import { C, TYPE, U, WORD_FACE, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { clamp, E, hitPulse, lmix, mix, prog, rand, spr, SPR } from '../lib/anim';
import { delayTo } from '../lib/sync';
import { dawnGradient, Grain, nightGradient } from '../kit/Kit';
import { useLetters } from '../kit/useLetters';
import { ACT, copy, CUE, f60, H, TOTAL, W } from '../timeline';
import { ShareCardAt } from '../kit/ShareCards';
import { TRIPTYCH } from './Act7';

// ACT 8 - THE MOMENT. The three cards fly off toward the edges (tr-subtract-reveal), but the coral
// sticker of the middle card stays where it was: the moment petal, alone. Night turns to the
// app icon's cobalt sky. The petal glides to its slot and seven white petals unfold around it, one
// after another, clockwise from one o'clock; the core lands (landing 2). The bloom steps aside and
// "Kairos" slides out from behind it; "Now on Android" follows. The lockup keeps building on a slow
// push to the last frame.

const NAME = copy('wordmark');
const ANDROID = copy('android');
const NS = 196 * U; // the wordmark size
const NAME_STYLE: React.CSSProperties = { ...WORD_FACE, fontSize: NS, fontWeight: 600, letterSpacing: '-0.03em', lineHeight: 1 };
const BIG = { size: 440 * U, x: W / 2, y: H / 2 - 10 * U }; // the bloom, centred, before the lockup
const CORE_START = CUE.core - delayTo(SPR.land);
const SLIDE = { from: NAME.in, to: CUE.lockup }; // the bloom steps aside as the name slides out

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
  const [boxes, probe] = useLetters(NAME.text, NAME_STYLE);
  const nameW = boxes ? boxes[boxes.length - 1].x + boxes[boxes.length - 1].w : 0;

  // --- the stage: night to the icon's cobalt sky ------------------------------------------
  const dawn = prog(f, CUE.scatter, CUE.core, E.smooth);

  // --- the scatter: each card along the line from the centre through its own centre --------
  const scatter = prog(f, CUE.scatter - 1, CUE.scatter + f60(30), E.contact);

  // --- the lockup geometry ----------------------------------------------------------------
  const LB = NS * 1.02; // bloom size in the lockup
  const gap = NS * 0.2;
  const total = LB + gap + nameW;
  const left = (W - total) / 2;
  const base = H / 2 + NS * 0.34; // the name's baseline; the bloom centres on its x-height
  const lock = prog(f, SLIDE.from, SLIDE.to, E.inOut);
  const bloom = { size: lmix(BIG.size, LB, lock), x: mix(BIG.x, left + LB / 2, lock), y: mix(BIG.y, base - NS * 0.36, lock) };
  const bloomLeft = bloom.x - bloom.size / 2;
  const bloomTop = bloom.y - bloom.size / 2;
  const slot = { x: bloomLeft + (MOMENT_CENTRE.x / 100) * bloom.size, y: bloomTop + (MOMENT_CENTRE.y / 100) * bloom.size };

  // --- the device: from the sticker to the moment petal's slot -----------------------------
  const go = prog(f, CUE.scatter + f60(6), CUE.unfold + f60(26), E.glide);
  const seated = go >= 1;
  const dev = { x: mix(STICKER.x, slot.x, go), y: mix(STICKER.y, slot.y, go), size: lmix(STICKER.size, bloom.size, go) };

  // --- the bloom unfolds clockwise from the moment petal; the core lands ------------------
  const order = [2, 3, 4, 5, 6, 7, 0]; // the moment petal (1) is already there
  const unfold = (i: number) => {
    const n = order.indexOf(i);
    if (n < 0) return 1;
    const s = CUE.unfold + f60(14) + n * f60(4);
    return prog(f, s, s + f60(28), E.out);
  };
  const core = f >= CUE.core ? 1 + 0.12 * hitPulse(f - CUE.core, 1, 7) : clamp(spr(f, CORE_START, SPR.land));

  // --- camera: a slow push through the lockup to the last frame ---------------------------
  const push = lmix(1, 1.07, prog(f, SLIDE.from, TOTAL, E.dolly)) * (1 + 0.015 * hitPulse(f - CUE.lockup, 2, 6));

  // the name rides out from behind the bloom's moving right edge, clipped by it
  const edge = bloomLeft + bloom.size;
  const nameX = left + LB + gap - (1 - lock) * (nameW + gap);

  return (
    <AbsoluteFill style={{ background: nightGradient, overflow: 'hidden' }}>
      {probe}
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
              <ShareCardAt shape={c.shape} h={c.h} sticker={i !== 1} />
            </div>
          );
        })}
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${W / 2}px ${H / 2}px`, transform: `scale(${push})` }}>
        <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${bloomLeft}px, ${bloomTop}px)` }}>
          <Bloom size={bloom.size} petal={C.white} moment={C.coral} core={C.white} unfold={unfold} core01={core} skipMoment={!seated} />
        </div>
        {!seated && <Petal size={dev.size} color={C.coral} style={placePetal(dev.x, dev.y, dev.size, 45)} />}
        {lock > 0 && boxes && (
          <div style={{ position: 'absolute', left: 0, top: 0, width: W, height: H, overflow: 'hidden', clipPath: `inset(0 0 0 ${edge + gap * 0.4}px)` }}>
            <span style={{ ...NAME_STYLE, position: 'absolute', left: 0, top: 0, whiteSpace: 'pre', color: C.white, transform: `translate(${nameX}px, ${base - NS * 0.86}px)` }}>{NAME.text}</span>
          </div>
        )}
        <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${left + LB + gap + 4 * U}px, ${base + 46 * U}px)` }}>
          <Words text={ANDROID.text} id={ANDROID.id} frame={f} start={ANDROID.in} size={TYPE.secondary.size * U} style={{ ...typeStyle('secondary'), color: C.white, opacity: 0.88 }} />
        </div>
      </div>
      <Grain frame={f} opacity={0.045} />
    </AbsoluteFill>
  );
};
