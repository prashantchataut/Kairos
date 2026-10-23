// cinetic:brand-supplied coral, gradient: Kairos' night and its share cards (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, lmix, mix, prog } from '../lib/anim';
import { Grain, nightGradient } from '../kit/Kit';
import { scrollFor, Wall } from '../kit/Wall';
import { ACT, copy, CUE, f60, H, W } from '../timeline';

// ACT 7 - SHARE. The phone has turned edge-on into the night; behind it a wall of what people keep
// and share rises up the frame: the studio's real cards, words and great thoughts (Seneca, Marcus
// Aurelius, Horace, Lao Tzu). "Share" / "what stays." on the left. The wall slows and one card
// comes off it toward us: Marcus Aurelius, "Dwell on the beauty of life."

const A = copy('share.1');
const B = copy('share.2');
export const WALL7 = { cx: W * 0.64, cy: H / 2 + 150 * U, planeH: 2600, hero: 9, heroName: 'thought_pin_1_starry_ink' };
const D = scrollFor(WALL7.hero, WALL7.planeH);
const S0 = D - 2200; // where the wall starts: it travels 2200 px up the plane through the act

/** The wall comes to rest on its hero while "what stays." holds. */
const STOP = CUE.story + f60(50);

export const wall7Scroll = (f: number) => {
  const t = prog(f, ACT.share.from - f60(10), STOP, E.linear);
  return S0 + (D - S0) * (1 - Math.pow(1 - t, 2.2));
};

export const Act7: React.FC = () => {
  const f = useCurrentFrame() + ACT.share.from;
  const rise = prog(f, ACT.share.from - 1, ACT.share.from + f60(44), E.cam); // the whole wall comes up from below
  const lift = prog(f, STOP + f60(4), STOP + f60(50), E.cam);
  const dim = prog(f, STOP - f60(6), STOP + f60(24), E.smooth);
  const push = lmix(1, 1.04, prog(f, ACT.share.from, ACT.share.from + ACT.share.dur, E.dolly));
  const size = TYPE.line.size * 0.86 * U;
  const lineY = H / 2 - size / 2;
  return (
    <AbsoluteFill style={{ background: nightGradient, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${WALL7.cx}px ${WALL7.cy}px`, transform: `translateY(${mix(H * 0.9, 0, rise)}px) scale(${push})` }}>
        <Wall cx={WALL7.cx} cy={WALL7.cy} planeH={WALL7.planeH} scroll={wall7Scroll(f)} hero={WALL7.hero} heroName={WALL7.heroName} lift={lift} dim={dim} heroScale={1.5} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, width: W * 0.55, height: H, background: `linear-gradient(90deg, ${C.night3} 0%, ${C.night3}00 100%)`, opacity: 0.85 }} />
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${128 * U}px, ${lineY}px)` }}>
        <Words text={A.text} id={A.id} frame={f} start={A.in} out={A.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${128 * U}px, ${lineY + size * 1.15}px)` }}>
        <Words text={B.text} id={B.id} frame={f} start={B.in} out={B.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      <Grain frame={f} opacity={0.05} />
    </AbsoluteFill>
  );
};
