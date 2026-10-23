// cinetic:brand-supplied coral, gradient: Kairos' night and its share cards (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, TYPE, U } from '../brand/tokens';
import { Chain } from '../fx/Chain';
import { E, lmix, prog } from '../lib/anim';
import { Grain, nightGradient } from '../kit/Kit';
import { scrollFor, Wall } from '../kit/Wall';
import { ACT, CUE, f60, H, W } from '../timeline';

// ACT 1 - DAYS. Night. A wall of the app's real cards (words, thoughts, the studio's designs)
// rushes upward on a tilted plane: days passing, too fast to read. "Days" / "just pass." on the
// left. "Some moments" - the wall brakes and stops dead on one card, the rest falls back out of
// focus, and that card lifts off the wall to face us: Seneca, "Nothing, Lucilius, is ours, except
// time." - "stay." Act 2 rises over it.

export const WALL1 = { cx: W * 0.64, cy: H / 2 + 150 * U, planeH: 2600, hero: 15 };
const D = scrollFor(WALL1.hero, WALL1.planeH);

/** The wall's scroll at absolute frame f: fastest at the start, braking to rest on CUE.stop. */
export const wallScroll = (f: number) => {
  const t = Math.min(1, Math.max(0, f / CUE.stop));
  return D * (1 - Math.pow(1 - t, 3));
};

export const Act1: React.FC = () => {
  const f = useCurrentFrame() + ACT.chronos.from;
  const lift = prog(f, CUE.stop + f60(6), CUE.stop + f60(56), E.cam);
  const dim = prog(f, CUE.stop, CUE.stop + f60(30), E.smooth);
  const push = lmix(1.06, 1, prog(f, 0, CUE.stop, E.out)) * lmix(1, 1.035, prog(f, CUE.stop, ACT.chronos.dur, E.dolly));
  const top = H / 2 - (TYPE.line.size * U) / 2;
  return (
    <AbsoluteFill style={{ background: nightGradient, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${WALL1.cx}px ${WALL1.cy}px`, transform: `scale(${push})` }}>
        <Wall cx={WALL1.cx} cy={WALL1.cy} planeH={WALL1.planeH} scroll={wallScroll(f)} hero={WALL1.hero} lift={lift} dim={dim} heroScale={1.5} />
      </div>
      {/* a soft shade on the left so the copy reads over the wall */}
      <div style={{ position: 'absolute', left: 0, top: 0, width: W * 0.55, height: H, background: `linear-gradient(90deg, ${C.night3} 0%, ${C.night3}00 100%)`, opacity: 0.85 }} />
      <Chain chain="days" frame={f} align="left" x={128 * U} top={top} accent="none" color={C.white} />
      <Chain chain="stay" frame={f} align="left" x={128 * U} top={top} accent="hold" color={C.white} accentColor={C.coral} />
      <Grain frame={f} opacity={0.05} />
    </AbsoluteFill>
  );
};
