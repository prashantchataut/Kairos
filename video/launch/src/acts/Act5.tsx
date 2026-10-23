// cinetic:brand-supplied coral, serif: Kairos' Play as it ships (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, mix, prog } from '../lib/anim';
import { FEED_KEPT } from '../kit/data';
import { breatheT, FeedReel, KaiArt, KeptOverlay, PlayCard } from '../kit/App';
import { Grain, Phone, phoneSize } from '../kit/Kit';
import { ACT, copy, CUE, f60 } from '../timeline';
import { PH } from './Act3';
import { FEED_PAGES, flick2, KEEP_LABEL_BG, LearnsCopy } from './Act4';

// ACT 5 - PLAY. The Feed's last flick carries across the cut; on the downbeat the app pushes from
// the Feed to Play (the real play_front render: "Finish the line"). "Play" waits on the left. The
// top card turns over to its answer, the real play_back ("unplug"), and "your words." answers on
// the right. On the second beat Kai, drawKai's own frames, rises beside the phone, delighted.

const A = copy('play.1');
const B = copy('play.2');
const S = phoneSize(PH.w);

export const Act5: React.FC = () => {
  const f = useCurrentFrame() + ACT.play.from;

  // the page push: the Feed slides off to the left, Play comes in from the right
  const push = prog(f, CUE.fan - 1, CUE.fan + f60(30), E.inOut);
  const turned = prog(f, CUE.turn1 - f60(4), CUE.turn1 + f60(22), E.inOut);

  // Kai rises beside the phone on the second turn, delighted, then breathes
  const kaiIn = prog(f, CUE.turn2 - f60(12), CUE.turn2 + f60(20), E.out);
  const delight = prog(f, CUE.turn2, CUE.turn2 + f60(16), E.out);
  const kaiSize = 190 * U;
  const kai = { x: PH.x + PH.w - 40 * U, y: PH.y + S.bezel + 560 * S.k + mix(140 * U, 0, kaiIn) };

  const size = TYPE.line.size * 0.74 * U;
  const lineY = PH.y + S.bezel + 432 * S.k - size * 0.55; // level with the card's centre

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      <Phone w={PH.w} style={{ transform: `translate(${PH.x}px, ${PH.y}px)` }}>
        {push < 1 && (
          <div style={{ position: 'absolute', inset: 0, transform: `translateX(${-push * 120}px)`, opacity: 1 - push * 0.5 }}>
            <FeedReel pages={FEED_PAGES} pos={FEED_KEPT + flick2(f)}>
              {(i) => i === FEED_KEPT && <KeptOverlay on={1} labelBg={KEEP_LABEL_BG} />}
            </FeedReel>
          </div>
        )}
        {push > 0 && (
          <div style={{ position: 'absolute', inset: 0, transform: `translateX(${(1 - push) * 400}px)`, boxShadow: '-20px 0 60px rgba(0,0,0,0.25)' }}>
            <PlayCard turned={turned} />
          </div>
        )}
      </Phone>
      {kaiIn > 0.001 && (
        <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${kai.x}px, ${kai.y - kaiSize / 2}px) rotate(${mix(14, -6, kaiIn)}deg)`, opacity: Math.min(1, kaiIn * 2) }}>
          {delight < 1 ? <KaiArt size={kaiSize} seq="delight" t={delight} /> : <KaiArt size={kaiSize} seq="breathe" t={breatheT(f - CUE.turn2)} />}
        </div>
      )}
      <LearnsCopy f={f} />
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x - 56 * U}px, ${lineY}px) translateX(-100%)` }}>
        <Words text={A.text} id={A.id} frame={f} start={A.in} out={A.out} size={size} style={{ ...typeStyle('line', size), color: C.ink }} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x + PH.w + 56 * U}px, ${lineY}px)` }}>
        <Words text={B.text} id={B.id} frame={f} start={B.in} out={B.out} size={size} style={{ ...typeStyle('line', size), color: C.ink }} />
      </div>
      <Grain frame={f} opacity={0.035} />
    </AbsoluteFill>
  );
};
