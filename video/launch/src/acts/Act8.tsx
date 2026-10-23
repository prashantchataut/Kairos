// cinetic:brand-supplied gradient: Kairos' sky, icon and Today screen (BRIEF.md).
import React from 'react';
import { AbsoluteFill, Freeze, useCurrentFrame } from 'remotion';
import { C, FONT, U } from '../brand/tokens';
import { NAME8, SIZES } from '../fx/a128Time';
import { E, lmix, mix, prog } from '../lib/anim';
import { columnsClip, Lockup, PosedPhone } from '../kit/a128Stage';
import { blinkT, KaiArt, Shot, TODAY as T } from '../kit/App';
import { Aurora, Grain } from '../kit/Kit';
import { tilt } from '../kit/rig';
import { ACT, CUE, f60, H, TOTAL, W } from '../timeline';
import { Act7 } from './Act7';

// ACT 8 - THE MOMENT. Morning rises over the night in columns once more (Act 2 opened the day this
// way, Act 6 closed it). The composition is Act 2's turned around: the phone on the left, the name
// on the right. The phone comes up edge-on and turns to face us on Today, as it is every morning;
// on the right the horizon draws, the app's icon rises over it and blooms petal by petal from the
// moment petal, its core lands, and "Kairos" rises from behind the line on the hit, the phone
// square to us as it does. "Now on Android" comes into focus under the line; the frame holds like
// a poster on a slow push.

/** The phone's place at rest: left of centre, overflowing the bottom edge. */
const PH8 = { w: 600 * U, x: 250 * U, y: 96 * U };
const SUB: React.CSSProperties = { fontFamily: FONT.text, fontWeight: 600, letterSpacing: '-0.02em', fontVariationSettings: '"wdth" 100, "opsz" 48', color: C.ink };

export const Act8: React.FC = () => {
  const f = useCurrentFrame() + ACT.lockup.from;
  const { clip, closed } = columnsClip(f, CUE.scatter);

  // --- the phone turns from edge-on to face us across the bloom, square to us as the name lands --
  const turn = prog(f, ACT.lockup.from + f60(2), CUE.lockup - f60(4), E.smooth);
  const rise = prog(f, ACT.lockup.from + f60(2), CUE.lockup - f60(20), E.cam);
  const t = tilt(f);
  // the rig keeps the phone turned toward the name; here the name is on its right, so it turns the other way
  const pose = { rx: t.rx, ry: -t.ry + 2 + 56 * (1 - turn), rz: -t.rz, z: -160 * U * (1 - rise) };
  // the last push: the phone is nearer the lens than the name, so it grows a little faster
  const hold = prog(f, CUE.core, TOTAL, E.dolly);
  const near = lmix(1, 1.05, hold);
  const far = lmix(1, 1.03, hold);

  return (
    <AbsoluteFill style={{ background: C.night2, overflow: 'hidden' }}>
      {!closed && (
        <Freeze frame={ACT.share.dur - 1}>
          <Act7 />
        </Freeze>
      )}
      <div style={{ position: 'absolute', inset: 0, clipPath: closed ? undefined : clip }}>
        <Aurora frame={f} />
        <div style={{ position: 'absolute', inset: 0, transformOrigin: `${W * 0.5}px ${H * 0.55}px`, transform: `scale(${near.toFixed(5)})` }}>
          <PosedPhone w={PH8.w} x={PH8.x} y={mix(PH8.y + 90 * U, PH8.y, rise)} {...pose} shadow={mix(0.5, 1, rise)}>
            <Shot name="today" />
            <div style={{ position: 'absolute', left: T.kai.x - T.kai.size / 2, top: T.kai.y - T.kai.size / 2 }}>
              <KaiArt size={T.kai.size} seq="blink" t={blinkT(f, CUE.lockup + f60(30), f60(14))} />
            </div>
          </PosedPhone>
        </div>
        <div style={{ position: 'absolute', inset: 0, transformOrigin: `${W * 0.5}px ${H * 0.55}px`, transform: `scale(${far.toFixed(5)})` }}>
          <Lockup frame={f} nb={NAME8} id="wordmark" x={998 * U} baseline={590 * U} size={SIZES.name8 * U} sub={{ id: 'android', size: SIZES.android * U, face: SUB }} lineRight={0} />
        </div>
        <Grain frame={f} opacity={0.035} />
      </div>
    </AbsoluteFill>
  );
};
