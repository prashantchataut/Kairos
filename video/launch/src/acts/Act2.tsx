// cinetic:brand-supplied gradient: the light rising is Kairos' own sky (BRIEF.md).
import React from 'react';
import { AbsoluteFill, Freeze, useCurrentFrame } from 'remotion';
import { C, FONT, U } from '../brand/tokens';
import { NAME2, SIZES } from '../fx/a128Time';
import { E, mix, prog } from '../lib/anim';
import { columnsClip, Lockup, NAME_FACE, PosedPhone } from '../kit/a128Stage';
import { breatheT, KaiArt, Shot } from '../kit/App';
import { Aurora, Grain } from '../kit/Kit';
import { tilt } from '../kit/rig';
import { ACT, f60, H } from '../timeline';
import { Act1 } from './Act1';
import { PH2 } from './Act3';

// ACT 2 - THE NAME. Morning rises over the night in stepped columns (the move Act 6 uses again,
// the film's rhyme). The phone comes up out of the bottom of the frame with weight: it travels
// first and turns into its place after, settling without a bounce, on the app's welcome screen,
// where Kai breathes: "Hi, I'm Kai." Left of it, off centre, a hairline horizon draws across; the
// app's icon rises over it and unfolds its petals the way the app does, one per 32nd from the
// moment petal; "Kairos" rises letter by letter from behind the same line in the app's Display face,
// and "the right moment." comes out of a blur under it. Then all of it sinks back behind the line
// and the phone, alone, glides on into Act 3.

/** The name's face (the app's Display), for acts that set it. */
export const DISPLAY: React.CSSProperties = NAME_FACE;
const SUB: React.CSSProperties = { fontFamily: FONT.text, fontWeight: 600, letterSpacing: '-0.02em', fontVariationSettings: '"wdth" 100, "opsz" 48', color: C.inkSoft };

export const Act2: React.FC = () => {
  const f = useCurrentFrame() + ACT.name.from;
  const { clip, closed } = columnsClip(f, ACT.name.from);

  // --- the phone: it rises first and turns into its place after, as a heavy thing does --------
  const rise = prog(f, ACT.name.from + f60(4), ACT.name.from + f60(96), E.cam);
  const turn = prog(f, ACT.name.from + f60(4), ACT.name.from + f60(132), E.cam);
  const t = tilt(f);
  const pose = { rx: t.rx + 26 * (1 - turn), ry: t.ry - 14 * (1 - turn), rz: t.rz + 5 * (1 - turn), z: -220 * U * (1 - rise) };

  return (
    <AbsoluteFill style={{ background: C.night2, overflow: 'hidden' }}>
      {!closed && (
        <Freeze frame={ACT.chronos.dur - 1}>
          <Act1 />
        </Freeze>
      )}
      <div style={{ position: 'absolute', inset: 0, clipPath: closed ? undefined : clip }}>
        <Aurora frame={f} />
        <PosedPhone w={PH2.w} x={PH2.x} y={mix(H + 60 * U, PH2.y, rise)} {...pose} shadow={mix(0.35, 1, rise)} tone="dark">
          <Shot name="onboarding_hello" />
          {/* Kai on the welcome screen breathes, drawn by the app's own frames (matched on the render: centred at (200, 256) dp, 152 dp); a loop of one bar, home on Act 3's first frame */}
          <div style={{ position: 'absolute', left: 200 - 76, top: 256 - 76 }}>
            <KaiArt size={152} seq="breathe" t={breatheT(f, f60(120))} />
          </div>
        </PosedPhone>
        {/* on the film's copy margin (128 px, where "stay." stood), off centre, beside the phone */}
        <Lockup frame={f} nb={NAME2} id="name.1" x={128 * U} baseline={566 * U} size={SIZES.name2 * U} sub={{ id: 'name.2', size: SIZES.means * U, face: SUB }} lineRight={36 * U} />
        <Grain frame={f} opacity={0.035} />
      </div>
    </AbsoluteFill>
  );
};
