// cinetic:brand-supplied coral, serif: Kairos' Feed as it ships (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, hitPulse, inertia, lmix, prog } from '../lib/anim';
import { FEED_KEPT } from '../kit/data';
import { Grain, Phone, phoneSize } from '../kit/Kit';
import { FEED_KEEP, FeedScreen } from '../kit/Screens';
import { ACT, copy, CUE, f60 } from '../timeline';
import { PH } from './Act3';

// ACT 4 - THE FEED. Act 3 whipped up out of the word card; the Feed opens mid-flick at the same
// close framing and pulls back to the phone while the reel coasts onto Apricity. "It learns" waits
// on the left. Keep: the heart on the rail fills coral (the device), and Kai's chip says what it
// noticed. "what you keep." answers on the right, a coral loop drawn round "keep" by hand (r6).
// The reel flicks on to Petrichor, a weather word, because that is what was kept.

const A = copy('learns.1');
const B = copy('learns.2');
const S = phoneSize(PH.w);
export const PITCH = 620; // app units between reel cards (Screens.FeedScreen)
/** The last flick (to Petrichor): Act 5 keeps it running across the cut. */
export const flick2 = (f: number) => prog(f, CUE.flick2 - 1, CUE.flick2 + f60(34), E.whip);

/** Where the Feed's copy sits: level with the reel card's centre (Act 3's phone pose, unscaled). */
const ANCHOR_Y = PH.y + S.bezel + 460 * S.k;

/** "It learns" / "what you keep." with its hand-drawn loop. Act 5 draws it too, to finish its exit. */
export const LearnsCopy: React.FC<{ f: number }> = ({ f }) => {
  const size = TYPE.line.size * 0.74 * U;
  const lineY = ANCHOR_Y - size * 0.55;
  // the hand-drawn loop round "keep" (the last word of B), drawn on over 22 f once B has resolved
  const loop = prog(f, B.resolved - f60(4), B.resolved + f60(22), E.outSoft);
  return (
    <>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x - 56 * U}px, ${lineY}px) translateX(-100%)` }}>
        <Words text={A.text} id={A.id} frame={f} start={A.in} out={A.out} size={size} style={{ ...typeStyle('line', size), color: C.ink }} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x + PH.w + 56 * U}px, ${lineY}px)` }}>
        <div style={{ position: 'relative' }}>
          <Words text={B.text} id={B.id} frame={f} start={B.in} out={B.out} size={size} style={{ ...typeStyle('line', size), color: C.ink }} />
          <svg width={size * 3.2} height={size * 1.7} viewBox="0 0 320 170" style={{ position: 'absolute', left: size * 3.78, top: -size * 0.34, overflow: 'visible', opacity: 1 - prog(f, B.out, B.out + f60(16), E.smooth) }}>
            <path
              d="M40 92 C 30 40, 150 18, 240 34 C 300 46, 312 104, 250 130 C 180 158, 60 150, 30 112 C 18 96, 40 70, 70 62"
              fill="none"
              stroke={C.coral}
              strokeWidth={7}
              strokeLinecap="round"
              pathLength={1}
              strokeDasharray={1}
              strokeDashoffset={1 - loop}
            />
          </svg>
        </div>
      </div>
    </>
  );
};

export const Act4: React.FC = () => {
  const f = useCurrentFrame() + ACT.feed.from;

  // --- the reel: coasting onto the kept card, then one flick to the next -------------------
  const coast = inertia(f, CUE.flick1, 26, 0.9); // starts at Act 3's whip speed, settles ~40 f later
  const s0 = FEED_KEPT * PITCH - coast.total;
  const flick = flick2(f);
  const scroll = s0 + coast.x + flick * PITCH;

  // --- camera: from Act 3's close framing back to the phone -------------------------------
  const pull = prog(f, CUE.flick1 - 1, CUE.flick1 + f60(40), E.out);
  const k = lmix(2.2, 1, pull);
  const anchor = { x: PH.x + S.bezel + 168 * S.k, y: PH.y + S.bezel + 460 * S.k };

  // --- Keep: the heart fills coral, a ring echoes it, Kai's chip follows --------------------
  const keep = prog(f, CUE.keep - 1, CUE.keep + f60(10), E.out);
  const pop = hitPulse(f - CUE.keep, 2, 6);
  const chip = prog(f, CUE.keep + f60(16), CUE.keep + f60(40), E.out);
  const heart = { x: PH.x + S.bezel + FEED_KEEP.x * S.k, y: PH.y + S.bezel + (FEED_KEEP.y + 22) * S.k };

  // --- copy --------------------------------------------------------------------------------

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${anchor.x}px ${anchor.y}px`, transform: `scale(${k})` }}>
        <Phone w={PH.w} style={{ transform: `translate(${PH.x}px, ${PH.y}px)` }}>
          <FeedScreen scroll={scroll} keep={keep * (1 - flick)} chip={chip} />
        </Phone>
        {pop > 0.02 && (
          <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${heart.x}px, ${heart.y}px)` }}>
            <div style={{ position: 'absolute', left: -44 * U, top: -44 * U, width: 88 * U, height: 88 * U, borderRadius: 999, border: `${3 * U}px solid ${C.coral}`, opacity: pop, transform: `scale(${1 + (1 - pop) * 0.9})` }} />
          </div>
        )}
      </div>
      <LearnsCopy f={f} />
      <Grain frame={f} opacity={0.035} />
    </AbsoluteFill>
  );
};
