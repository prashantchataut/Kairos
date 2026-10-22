// cinetic:brand-supplied coral, serif: Kairos' Play deck as it ships (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { C, TYPE, U } from '../brand/tokens';
import { Chain } from '../fx/Chain';
import { clamp, E, hitPulse, mix, prog, stagger } from '../lib/anim';
import { FLASH } from '../kit/data';
import { Grain, Kai, Phone } from '../kit/Kit';
import { Flashcard, FeedScreen } from '../kit/Screens';
import { ACT, CUE, f60, H, W } from '../timeline';
import { PH } from './Act3';

// ACT 5 - PLAY. The phone drops away and the words this person kept fan out across the frame as
// flashcards, in a cool duotone until one is played (cl-duotone-until-featured): Apricity turns
// over and takes its full colour, then Sonder. "Play" / "your words." builds above them. Kai peeks
// over the deck. Every card carries the coral petal in its corner: the device.

const CARD = { w: 300, h: 420, k: 1.22 * U };
const N = FLASH.length;
/** Fan pose of card i: centre, rotation. The middle card is highest. */
const fan = (i: number) => {
  const c = i - (N - 1) / 2;
  return { x: W / 2 + c * 318 * U, y: H / 2 + 110 * U + c * c * 26 * U, rot: c * 6.5 };
};
const TURNS = [
  { i: 2, at: CUE.turn1 }, // Sonder
  { i: 0, at: CUE.turn2 }, // Apricity
];

export const Act5: React.FC = () => {
  const f = useCurrentFrame() + ACT.play.from;

  // the phone drops away, accelerating, as the cards burst out of it
  const drop = prog(f, CUE.fan - 1, CUE.fan + f60(26), E.in);
  const origin = { x: W / 2, y: H * 0.78 };

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      {drop < 1 && (
        <Phone w={PH.w} style={{ transform: `translate(${PH.x}px, ${PH.y + drop * H * 1.1}px)` }}>
          <FeedScreen scroll={2 * 620} keep={0} chip={0} />
        </Phone>
      )}
      {/* Kai peeks over the middle card once the second card has turned */}
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${W / 2 + 70 * U}px, ${fan(2).y - CARD.h * CARD.k * 0.5 - 70 * U + (1 - prog(f, CUE.turn2 - f60(30), CUE.turn2, E.out)) * 90 * U}px)`, opacity: prog(f, CUE.turn2 - f60(30), CUE.turn2 - f60(24), E.smooth) }}>
        <Kai size={110 * U} look={[-0.8, 0.6]} blink={hitPulse(f - CUE.turn2 - 20, 2, 4)} />
      </div>
      <div style={{ position: 'absolute', inset: 0, perspective: 1800 }}>
        {FLASH.map((card, i) => {
          const s = CUE.fan + f60(4) + stagger(Math.abs(i - 2), 3, f60(10));
          const p = prog(f, s, s + f60(34), E.out);
          const home = fan(i);
          const breath = 1 + 0.012 * Math.sin(((f - s) / 60) * Math.PI * 2 * 0.45 + i);
          const turn = TURNS.find((t) => t.i === i);
          const turned = turn ? prog(f, turn.at - f60(4), turn.at + f60(22), E.inOut) : 0;
          const lift = turn ? hitPulse(f - turn.at, 4, 14) : 0;
          const mono = turn ? 1 - prog(f, turn.at, turn.at + f60(18), E.smooth) : 1;
          const x = mix(origin.x, home.x, p);
          const y = mix(origin.y, home.y, p) - lift * 40 * U;
          const k = mix(0.4, 1, p) * CARD.k * breath * (1 + 0.06 * lift);
          return (
            <div
              key={card.w.word}
              style={{
                position: 'absolute',
                left: 0,
                top: 0,
                width: CARD.w,
                height: CARD.h,
                transformOrigin: '50% 50%',
                transform: `translate(${x - CARD.w / 2}px, ${y - CARD.h / 2}px) rotate(${mix(0, home.rot, p) - (turn ? lift * 2 : 0)}deg) scale(${k})`,
                opacity: clamp(p * 3),
                zIndex: turn ? 3 : i === 2 ? 2 : 1,
              }}
            >
              <Flashcard w={card.w} hue={card.hue} turned={turned} mono={mono} />
            </div>
          );
        })}
      </div>
      <Chain chain="play" frame={f} top={120 * U} accent="recency" color={C.ink} accentColor={C.cobalt} size={TYPE.line.size * U} />
      <Grain frame={f} opacity={0.035} />
    </AbsoluteFill>
  );
};
