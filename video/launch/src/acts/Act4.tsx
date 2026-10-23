// cinetic:brand-supplied coral, serif: Kairos' Feed as it ships (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, SAMPLED, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, inertia, lmix, prog } from '../lib/anim';
import { FEED_KEPT } from '../kit/data';
import { FeedReel, KeptOverlay, type ScreenName } from '../kit/App';
import { Aurora, Grain, Phone, phoneSize } from '../kit/Kit';
import { tilt } from '../kit/rig';
import { ACT, copy, CUE, f60 } from '../timeline';
import { PH } from './Act3';

// ACT 4 - THE FEED. Act 3 whipped up out of the word card; the Feed opens mid-flick at the same
// close framing and pulls back to the phone while the reel coasts onto Apricity: the app's own Feed
// pages (feed_2, feed_0, feed_1), whose chip already says why ("You keep saving quiet weather
// words."). "It learns" waits on the left. Keep: the rail's heart turns to the app's coral HeartOn
// and the label to "Kept". "what you keep." answers on the right, a coral loop drawn round "keep" by
// hand (r6). The reel flicks on to the next page, chosen from the calm theme that was kept.

const A = copy('learns.1');
const B = copy('learns.2');
const S = phoneSize(PH.w);
export const PITCH = 620; // scroll units per page of the reel (pos = scroll / PITCH)
/** The reel's pages, the app's own renders; FEED_KEPT (1) is Apricity, the card that gets kept. */
export const FEED_PAGES: ScreenName[] = ['feed_2', 'feed_0', 'feed_1'];
/** The rail label's ground on feed_0, sampled: Kept is set over it. */
export const KEEP_LABEL_BG = SAMPLED.keepLabel;
/** The last flick (to Petrichor): Act 5 keeps it running across the cut. */
export const flick2 = (f: number) => prog(f, CUE.flick2 - 1, CUE.flick2 + f60(34), E.whip);

/** Where the Feed's copy sits: level with the reel card's centre (Act 3's phone pose, unscaled). */
const ANCHOR_Y = PH.y + S.top + 460 * S.k;

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
          <svg width={size * 3.5} height={size * 1.75} viewBox="0 0 320 170" preserveAspectRatio="none" style={{ position: 'absolute', left: size * 3.62, top: -size * 0.36, overflow: 'visible', opacity: 1 - prog(f, B.out, B.out + f60(16), E.smooth) }}>
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
  const anchor = { x: PH.x + S.bezel + 168 * S.k, y: PH.y + S.top + 460 * S.k };

  // --- Keep: the rail's heart turns coral, the app's Kept state ----------------------------
  const keep = prog(f, CUE.keep - 1, CUE.keep + f60(10), E.out);

  // --- copy --------------------------------------------------------------------------------

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      <Aurora frame={f} />
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${anchor.x}px ${anchor.y}px`, transform: `scale(${k})` }}>
        <Phone w={PH.w} x={PH.x} y={PH.y} {...tilt(f)}>
          <FeedReel pages={FEED_PAGES} pos={scroll / PITCH}>
            {(i) => i === FEED_KEPT && <KeptOverlay on={keep} labelBg={KEEP_LABEL_BG} />}
          </FeedReel>
        </Phone>
      </div>
      <LearnsCopy f={f} />
      <Grain frame={f} opacity={0.035} />
    </AbsoluteFill>
  );
};
