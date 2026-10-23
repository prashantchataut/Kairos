// cinetic:brand-supplied coral, serif, gradient: Kairos' Today screen as it ships (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { clamp, E, fd, lmix, mix, prog } from '../lib/anim';
import { TODAY } from '../kit/data';
import { blinkT, KaiArt, SavedPill, Shot, TODAY as T, TodayWordPatch, todayWordStyle } from '../kit/App';
import { Aurora, Grain, Phone, phoneSize } from '../kit/Kit';
import { tilt } from '../kit/rig';
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
export const PH = { w: 580 * U, x: W / 2 - 290 * U, y: 64 * U };
/** Where Act 2 holds the phone: right of the name, a little smaller and lower. */
export const PH2 = { w: 520 * U, x: W * 0.54, y: 150 * U };
const S = phoneSize(PH.w);

/** Screen px of an app-unit point on the phone at offset (dx, dy). */
const onPhone = (p: { x: number; y: number }, dy = 0) => ({ x: PH.x + S.bezel + p.x * S.k, y: PH.y + dy + S.top + p.y * S.k });

/** In-place rewrite (r5): letters swap left to right, 3 f apart, under a sky selection. */
const rewrite = (f: number, at: number, from: string, to: string) => {
  const n = clamp(Math.floor((fd(f) - at) / f60(3)), 0, Math.max(from.length, to.length));
  return to.slice(0, n) + from.slice(n, Math.max(n, from.length - (to.length > from.length ? 0 : 0)));
};
const REWRITE_LEN = (w: string) => f60(3) * w.length;

export const Act3: React.FC = () => {
  const f = useCurrentFrame() + ACT.word.from;

  // --- the phone glides in from Act 2's place on the right to the centre ---------------------
  const move = prog(f, CUE.rise - 1, CUE.rise + f60(40), E.cam);
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

  // Kai, drawKai's own frames over the header Kai (56 dp): delighted by the save, then a blink
  const delight = prog(f, CUE.heart + f60(4), CUE.heart + f60(20), E.out) * (1 - prog(f, CUE.heart + f60(70), CUE.heart + f60(90), E.smooth));
  const blink = blinkT(f, CUE.rewrite1 + f60(40), f60(14));
  const kaiArt = delight > 0.001 ? <KaiArt size={T.kai.size} seq="delight" t={delight} /> : <KaiArt size={T.kai.size} seq="blink" t={blink} />;

  // --- the copy: "A word" left of the phone, "for every feeling." right of it ----------------
  const size = TYPE.line.size * 0.74 * U;
  const lineY = cardC.y - size * 0.55;

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      <Aurora frame={f} />
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${cardC.x}px ${cardC.y}px`, transform: `translateY(${whip}px) scale(${push * settle})` }}>
        <Phone tone={move < 0.5 ? 'dark' : 'light'} w={mix(PH2.w, PH.w, move)} x={mix(PH2.x, PH.x, move)} y={mix(PH2.y, PH.y, move)} {...tilt(f)}>
          {/* the app pushes from its welcome screen to Today as the phone glides in */}
          {move < 1 && (
            <div style={{ position: 'absolute', inset: 0, transform: `translateX(${-move * 120}px)`, opacity: 1 - move * 0.6 }}>
              <Shot name="onboarding_hello" />
            </div>
          )}
          <div style={{ position: 'absolute', inset: 0, transform: `translateX(${(1 - move) * 400}px)`, boxShadow: move < 1 ? '-20px 0 60px rgba(0,0,0,0.2)' : undefined }}>
          <Shot name="today" />
          <TodayWordPatch word={word} info={info} infoFade={infoFade} />
          <SavedPill on={prog(f, CUE.heart - 2, CUE.heart + f60(10), E.ui)} />
          <div style={{ position: 'absolute', left: T.kai.x - T.kai.size / 2, top: T.kai.y - T.kai.size / 2 }}>{kaiArt}</div>
          </div>
        </Phone>
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
