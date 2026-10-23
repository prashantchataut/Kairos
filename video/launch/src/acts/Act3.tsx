// cinetic:brand-supplied coral, serif, gradient: Kairos' Today screen as it ships (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { Petal, placePetal } from '../brand/Mark';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { clamp, E, fd, hitPulse, lmix, mix, prog } from '../lib/anim';
import { TODAY } from '../kit/data';
import { FALL } from '../kit/device';
import { blinkT, KaiArt, SavedPill, Shot, TODAY as T, TodayWordPatch, todayWordStyle } from '../kit/App';
import { Grain, Phone, phoneSize } from '../kit/Kit';
import { ACT, copy, CUE, f60, H, W } from '../timeline';

// ACT 3 - THE WORD. The phone rises already moving on the downbeat, the app's Today screen on it
// (today.jpg, the real render; the word, the Saved pill and Kai are live overlays, src/kit/App.tsx). The
// coral petal, still falling from the dot of the i, lands in the Save button and becomes its
// heart. "A word" waits on the left; the word of the day rewrites itself in place (Saudade ->
// Apricity, the r5 move); "for every feeling." answers on the right, the phone standing in the
// middle of the sentence (the r4 move); Hiraeth; then the camera dives into the card and whips
// up into the Feed.

const A = copy('word.1');
const B = copy('word.2');

/** The phone at rest: centred, overflowing the bottom edge, as the product should be framed. */
export const PH = { w: 580 * U, x: W / 2 - 330 * U, y: 64 * U, rise: 170 * U };
const S = phoneSize(PH.w);

/** Screen px of an app-unit point on the phone at offset (dx, dy). */
const onPhone = (p: { x: number; y: number }, dy = 0) => ({ x: PH.x + S.bezel + p.x * S.k, y: PH.y + dy + S.bezel + p.y * S.k });

/** In-place rewrite (r5): letters swap left to right, 3 f apart, under a sky selection. */
const rewrite = (f: number, at: number, from: string, to: string) => {
  const n = clamp(Math.floor((fd(f) - at) / f60(3)), 0, Math.max(from.length, to.length));
  return to.slice(0, n) + from.slice(n, Math.max(n, from.length - (to.length > from.length ? 0 : 0)));
};
const REWRITE_LEN = (w: string) => f60(3) * w.length;

export const Act3: React.FC = () => {
  const f = useCurrentFrame() + ACT.word.from;

  // --- the phone rises already moving on the cut (pd-device-rise-on-cut) ---------------------
  const dy = mix(PH.rise, 0, prog(f, CUE.rise - 1, CUE.rise + f60(44), E.out));
  // --- the dive: push into the card, then whip up into the Feed ----------------------------
  const cardC = onPhone({ x: 200, y: 410 });
  const push = lmix(1, 2.3, prog(f, CUE.dive, CUE.through + f60(8), E.in));
  const whip = mix(0, -H * 1.1, prog(f, CUE.through, ACT.word.from + ACT.word.dur, E.in));
  const settle = lmix(1, 0.97, prog(f, CUE.rise, CUE.dive, E.linear)); // a slow pull-back through the act

  // --- the word, rewriting in place --------------------------------------------------------
  const [w0, w1, w2] = TODAY;
  const r1 = CUE.rewrite1;
  const r2 = CUE.rewrite2;
  let text = w0.word;
  let info = w0;
  if (f >= r1) text = rewrite(f, r1, w0.word, w1.word);
  if (f >= r1 + REWRITE_LEN(w1.word) / 2) info = w1;
  if (f >= r2) text = rewrite(f, r2, w1.word, w2.word);
  if (f >= r2 + REWRITE_LEN(w2.word) / 2) info = w2;
  const sel = Math.max(
    prog(f, r1 - f60(8), r1, E.out) * (1 - prog(f, r1 + REWRITE_LEN(w1.word), r1 + REWRITE_LEN(w1.word) + f60(14), E.smooth)),
    prog(f, r2 - f60(8), r2, E.out) * (1 - prog(f, r2 + REWRITE_LEN(w2.word), r2 + REWRITE_LEN(w2.word) + f60(14), E.smooth)),
  );
  const infoFade = Math.max(
    prog(f, r1, r1 + 8, E.smooth) * (1 - prog(f, r1 + REWRITE_LEN(w1.word) / 2, r1 + REWRITE_LEN(w1.word) / 2 + 10, E.smooth)),
    prog(f, r2, r2 + 8, E.smooth) * (1 - prog(f, r2 + REWRITE_LEN(w2.word) / 2, r2 + REWRITE_LEN(w2.word) / 2 + 10, E.smooth)),
  );
  const word = (
    <span style={{ position: 'relative', display: 'inline-block' }}>
      <span style={{ position: 'absolute', left: -6, right: -6, top: 2, bottom: -8, borderRadius: 8, background: C.skyFade, opacity: sel }} />
      <span style={{ position: 'relative', ...todayWordStyle }}>{text}</span>
    </span>
  );

  // --- the device: the falling petal lands as the Save heart -------------------------------
  const heart = onPhone({ x: T.heart.x, y: T.heart.y }, dy);
  const t = prog(f, CUE.rise - 1, CUE.heart, E.contact);
  const landed = f >= CUE.heart;
  const petal = {
    x: mix(FALL.x, heart.x, t),
    y: mix(FALL.y, heart.y, t),
    size: mix(FALL.size, T.heart.size * S.k * 4.6, t), // a petal 4.6x the heart's box is as wide as the heart
    rot: mix(FALL.rot, 0, t),
  };
  const pop = hitPulse(f - CUE.heart, 2, 6);
  // Kai, drawKai's own frames over the header Kai (56 dp): delighted by the save, then a blink
  const delight = prog(f, CUE.heart + f60(4), CUE.heart + f60(20), E.out) * (1 - prog(f, CUE.heart + f60(70), CUE.heart + f60(90), E.smooth));
  const blink = blinkT(f, CUE.rewrite1 + f60(40), f60(14));
  const kai = delight > 0.001 ? <KaiArt size={T.kai.size} seq="delight" t={delight} /> : <KaiArt size={T.kai.size} seq="blink" t={blink} />;

  // --- the copy: "A word" left of the phone, "for every feeling." right of it ----------------
  const size = TYPE.line.size * 0.74 * U;
  const lineY = cardC.y - size * 0.55;

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${cardC.x}px ${cardC.y}px`, transform: `translateY(${whip}px) scale(${push * settle})` }}>
        <Phone w={PH.w} style={{ transform: `translate(${PH.x}px, ${PH.y + dy}px)` }}>
          <Shot name="today" />
          <TodayWordPatch word={word} info={info} infoFade={infoFade} />
          <SavedPill on={landed ? 1 : 0} pop={pop} />
          <div style={{ position: 'absolute', left: T.kai.x - T.kai.size / 2, top: T.kai.y - T.kai.size / 2 }}>{kai}</div>
        </Phone>
        {!landed && <Petal size={petal.size} color={C.coral} style={placePetal(petal.x, petal.y, petal.size, petal.rot)} />}
        {landed && pop > 0.02 && (
          <div style={{ position: 'absolute', left: 0, top: 0, width: 1, height: 1, transform: `translate(${heart.x}px, ${heart.y}px)` }}>
            <div style={{ position: 'absolute', left: -40 * U, top: -40 * U, width: 80 * U, height: 80 * U, borderRadius: 999, border: `${3 * U}px solid ${C.coral}`, opacity: pop, transform: `scale(${1 + (1 - pop) * 0.8})` }} />
          </div>
        )}
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x - 56 * U}px, ${lineY}px) translateX(-100%)`, opacity: 1 - prog(f, CUE.dive, CUE.dive + 20, E.smooth) }}>
        <Words text={A.text} id={A.id} frame={f} start={A.in} out={A.out} size={size} style={{ ...typeStyle('line', size), color: C.ink }} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x + PH.w + 56 * U}px, ${lineY}px)`, opacity: 1 - prog(f, CUE.dive, CUE.dive + 20, E.smooth) }}>
        <Words text={B.text} id={B.id} frame={f} start={B.in} out={B.out} size={size} style={{ ...typeStyle('line', size), color: C.ink }} />
      </div>
      <Grain frame={f} opacity={0.035} />
    </AbsoluteFill>
  );
};
