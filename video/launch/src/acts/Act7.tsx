// cinetic:brand-supplied coral, serif, gradient: Kairos' share cards and night (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, hitPulse, mix, prog, stagger } from '../lib/anim';
import { Grain, nightGradient } from '../kit/Kit';
import { DH, ShareCardAt, ShareDesign, Sticker } from '../kit/ShareCards';
import { SHARE_RATIO, type ShareShape } from '../kit/Screens';
import { ACT, copy, CUE, f60, H, W } from '../timeline';

// ACT 7 - SHARE. The envelope, edge-on at the end of Act 6, keeps turning and comes round as a
// share card: a pin, Saudade on cobalt. "Share" waits on the left. The card changes shape like the
// r3 containers: pin -> story (a thought on the sky) -> square (Komorebi on mint), each its own
// design, not a crop. "what stays." answers on the right. Then the other two shapes rise beside it:
// the three, side by side. Every card wears the coral petal as a die-cut sticker: the device.

const A = copy('share.1');
const B = copy('share.2');
const HERO_H = 740 * U;
/** Each design's ground, so the clip never shows an empty edge mid-morph. */
const BG: Record<ShareShape, string> = { pin: C.cobalt, story: C.skyMid, square: C.mint };

/** The final pose (Act 8 scatters it): three cards side by side, centred, at height TRI_H. */
export const TRI_H = 560 * U;
export const TRIPTYCH = (() => {
  const shapes: ShareShape[] = ['pin', 'square', 'story'];
  const gap = 44 * U;
  const ws = shapes.map((s) => TRI_H * SHARE_RATIO[s]);
  const total = ws.reduce((a, b) => a + b, 0) + gap * 2;
  let x = (W - total) / 2;
  return shapes.map((shape, i) => {
    const c = { shape, x, y: H / 2 - TRI_H / 2 + 30 * U, w: ws[i], h: TRI_H };
    x += ws[i] + gap;
    return c;
  });
})();

export const Act7: React.FC = () => {
  const f = useCurrentFrame() + ACT.share.from;

  // the envelope's turn continues: 90° (edge-on) -> 0 as the pin card
  const turn = 90 * (1 - prog(f, ACT.share.from - 1, ACT.share.from + f60(22), E.out));
  // shape morphs: width tweens on E.inOut; the design swaps at the narrowest/midpoint
  const m1 = prog(f, CUE.story - f60(14), CUE.story + f60(16), E.inOut);
  const m2 = prog(f, CUE.square - f60(14), CUE.square + f60(16), E.inOut);
  const shape: ShareShape = m2 > 0.5 ? 'square' : m1 > 0.5 ? 'story' : 'pin';
  const ratio = mix(mix(SHARE_RATIO.pin, SHARE_RATIO.story, m1), SHARE_RATIO.square, m2);
  // the hero card shrinks to the triptych's height and its centre slot as the others arrive
  const tri = prog(f, CUE.square, CUE.square + f60(30), E.out);
  const h = mix(HERO_H, TRI_H, tri);
  const slot = TRIPTYCH[1];
  const cx = mix(W / 2, slot.x + slot.w / 2, tri);
  const cy = mix(H / 2 + 20 * U, slot.y + slot.h / 2, tri);
  const inset = (DH - DH * ratio) / 2; // design px clipped off each side
  const pulse = 1 + 0.025 * (hitPulse(f - CUE.story, 2, 6) + hitPulse(f - CUE.square, 2, 6));
  // the content cross-fades through the morph's middle, so a design is never squashed
  const swap = (m: number) => 1 - Math.min(1, Math.abs(m - 0.5) * 4);
  const fade = 1 - Math.max(swap(m1), swap(m2)) * 0.85;

  const size = TYPE.line.size * 0.74 * U;
  const lineY = H / 2 + 20 * U - size * 0.55;
  const textOut = prog(f, CUE.square - f60(10), CUE.square + f60(10), E.smooth);

  return (
    <AbsoluteFill style={{ background: nightGradient, overflow: 'hidden' }}>
      {/* the other two shapes rise in beside the hero for the triptych */}
      {[0, 2].map((i, n) => {
        const c = TRIPTYCH[i];
        const s = CUE.square + f60(6) + stagger(n, 2, f60(6));
        const p = prog(f, s, s + f60(34), E.out);
        if (p <= 0) return null;
        return (
          <div key={c.shape} style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${c.x}px, ${c.y + (1 - p) * 260 * U}px)`, opacity: Math.min(1, p * 2.5) }}>
            <ShareCardAt shape={c.shape} h={c.h} />
          </div>
        );
      })}
      <div style={{ position: 'absolute', left: 0, top: 0, perspective: 1600 * U, transform: `translate(${cx - h / 2}px, ${cy - h / 2}px)` }}>
        {/* a DH x DH box, scaled to h; the card's width is a clip, so the morph never re-lays out */}
        <div style={{ position: 'relative', width: DH, height: DH, transformOrigin: '0 0', transform: `scale(${h / DH})` }}>
          <div style={{ position: 'absolute', inset: 0, transformOrigin: '50% 50%', transform: `rotateY(${turn}deg) scale(${pulse})`, filter: 'drop-shadow(0 26px 40px rgba(0,0,0,0.28))' }}>
            <div style={{ position: 'absolute', inset: 0, clipPath: `inset(0 ${inset.toFixed(2)}px round ${DH * 0.045}px)` }}>
              <div style={{ position: 'absolute', inset: 0, background: BG[shape] }} />
              <div style={{ position: 'absolute', left: 0, top: 0, width: DH * SHARE_RATIO[shape], height: DH, transform: `translateX(${(DH - DH * SHARE_RATIO[shape]) / 2}px)`, opacity: fade }}>
                <ShareDesign shape={shape} />
              </div>
            </div>
            <Sticker size={DH * 0.13} style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${DH - inset - DH * 0.1}px, ${-DH * 0.03}px)` }} />
          </div>
        </div>
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${W / 2 - (HERO_H * SHARE_RATIO.pin) / 2 - 80 * U}px, ${lineY}px) translateX(-100%)`, opacity: 1 - textOut }}>
        <Words text={A.text} id={A.id} frame={f} start={A.in} out={A.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${W / 2 + (HERO_H * SHARE_RATIO.pin) / 2 + 80 * U}px, ${lineY}px)`, opacity: 1 - textOut }}>
        <Words text={B.text} id={B.id} frame={f} start={B.in} out={B.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      <Grain frame={f} opacity={0.05} />
    </AbsoluteFill>
  );
};
