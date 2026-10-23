// cinetic:brand-supplied coral, gradient: Kairos' night and its share cards (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, FONT, U } from '../brand/tokens';
import { rampLeft, SIZES } from '../fx/a128Time';
import { BlurWords } from '../fx/a128Type';
import { E, lmix, mix, prog } from '../lib/anim';
import { LensWall } from '../kit/a128Wall';
import { Grain, nightGradient } from '../kit/Kit';
import { scrollFor } from '../kit/Wall';
import { ACT, CUE, f60, H, W } from '../timeline';

// ACT 1 - DAYS. Night. The film opens mid-rush: a wall of the app's real cards (words, thoughts, the
// studio's designs) streams up a tilted plane, only a band of it in focus. It eases into a glide as
// "Days" comes out of a blur, surges on "just pass.", runs on under "Some moments" and brakes to a
// dead stop on one card. The focus racks off the wall, the wall falls into shadow, and the card
// lifts off it to face us: Seneca, "Nothing, Lucilius, is ours, except time." - "stay.", the only
// coral word, comes into focus without moving. Act 2's morning rises over it.

export const WALL1 = { cx: W * 0.64, cy: H / 2 + 150 * U, planeH: 2600, hero: 24 };
const D = scrollFor(WALL1.hero, WALL1.planeH);
/** Plane px per frame at the ramp's peak (the near edge of the frame moves ~1.2x this on screen). */
const PEAK = 52;

/** The wall's scroll at absolute frame f: the speed ramp of fx/a128Time, landing on the card at CUE.stop. */
export const wallScroll = (f: number) => D - PEAK * rampLeft(f);

/** The plane's tilt: it rolls and lifts a little as it slows, then holds. */
const plane = (f: number) => {
  const t = prog(f, 0, CUE.stop, E.dolly);
  return { rx: mix(37, 34, t), rz: mix(-19.5, -16, t) };
};

const SIZE = SIZES.days * U;
const FACE: React.CSSProperties = { fontFamily: FONT.text, fontWeight: 700, letterSpacing: '-0.035em', fontVariationSettings: '"wdth" 100, "opsz" 96', color: C.white };

export const Act1: React.FC = () => {
  const f = useCurrentFrame() + ACT.chronos.from;
  const { rx, rz } = plane(f);

  // the lens: a focus pull on the first frames, the band held through the run, a rack off the wall after the stop
  const band = mix(0.3, 1, prog(f, 0, f60(22), E.out)) * (1 - prog(f, CUE.stop - f60(2), CUE.stop + f60(26), E.smooth));
  const dark = 0.55 * prog(f, CUE.stop, CUE.stop + f60(34), E.smooth);
  const lift = prog(f, CUE.stop + f60(4), CUE.stop + f60(66), E.cam);

  // the camera: a slow pull back through the run, then a push toward the card as it lifts
  const liftY = WALL1.cy - (320 * Math.sin((34 * Math.PI) / 180) * 2000) / (2000 - 320 * Math.cos((34 * Math.PI) / 180));
  const push = lmix(1.07, 1, prog(f, 0, CUE.stop, E.dolly)) * lmix(1, 1.06, prog(f, CUE.stop, ACT.chronos.from + ACT.chronos.dur + f60(30), E.dolly));

  const top = 486 * U - SIZE / 2;
  return (
    <AbsoluteFill style={{ background: nightGradient, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${WALL1.cx}px ${liftY}px`, transform: `scale(${push.toFixed(5)})` }}>
        <LensWall
          cx={WALL1.cx}
          cy={WALL1.cy}
          planeH={WALL1.planeH}
          scroll={wallScroll(f)}
          rx={rx}
          rz={rz}
          hero={WALL1.hero}
          lift={lift}
          heroScale={1.5}
          band={band}
          dark={dark}
          blur={11}
          shade={C.night3}
        />
      </div>
      {/* night at the far and near edges, and a shade on the left so the copy reads over the wall */}
      <div style={{ position: 'absolute', inset: 0, background: `linear-gradient(180deg, ${C.night3}B3 0%, ${C.night3}00 26%, ${C.night3}00 80%, ${C.night3}99 100%)` }} />
      <div style={{ position: 'absolute', left: 0, top: 0, width: W * 0.6, height: H, background: `linear-gradient(90deg, ${C.night3}F0 0%, ${C.night3}B8 45%, ${C.night3}00 100%)` }} />

      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${128 * U}px, ${top}px)`, display: 'flex', whiteSpace: 'pre' }}>
        <BlurWords id="days.1" frame={f} size={SIZE} face={FACE} />
        <span style={{ ...FACE, fontSize: SIZE, lineHeight: 1 }}> </span>
        <BlurWords id="days.2" frame={f} size={SIZE} face={FACE} exitIndex={1} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${128 * U}px, ${top}px)` }}>
        <BlurWords id="stay.1" frame={f} size={SIZE} face={FACE} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${128 * U}px, ${top}px)` }}>
        {/* "stay." stays: it comes into focus without moving, and leaves so slowly that the frame Act 2 holds under its morning still has it */}
        <BlurWords id="stay.2" frame={f} size={SIZE} face={{ ...FACE, color: C.coral }} rise={0} blur={24} lift={0.1} exitDur={f60(90)} />
      </div>
      <Grain frame={f} opacity={0.05} />
    </AbsoluteFill>
  );
};
