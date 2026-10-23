// cinetic:brand-supplied coral, serif, gradient: the flood, Fraunces and the sky fill are Kairos' own (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { petalPath, Petal, placePetal } from '../brand/Mark';
import { C, TYPE, U, WORD_FACE, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { arrive, blurIn, clamp, depart, E, lmix, mix, pace, prog } from '../lib/anim';
import { FALL } from '../kit/device';
import { Grain } from '../kit/Kit';
import { useLetters } from '../kit/useLetters';
import { ACT, copy, CUE, f60, H, W } from '../timeline';
import { DIAL, HAND } from './Act1';

// ACT 2 - THE NAME. The camera dives into the coral hand until it floods the frame (the film's one
// accent flood). The flood shrinks back into a small petal that lands as the dot of the i in a
// giant "Kairos", whose letters rise around it; "the right moment." answers below. Then the name
// lifts away and the petal falls toward the phone that is about to rise (Act 3).

const NAME = copy('name.1');
const MEANS = copy('name.2');
// The word is set with a dotless i: the coral petal is its dot.
const SPELL = 'Kaıros';
const SIZE = 300 * U;
const BASE = H * 0.56; // baseline of the name
const STYLE: React.CSSProperties = { ...WORD_FACE, fontSize: SIZE, fontWeight: 600, letterSpacing: '-0.03em', lineHeight: 1 };
const DOT = { size: 0.6 * SIZE, lift: 0.7 }; // the petal dot, and its centre height above the baseline in em

/** Where the hand's fat part sits in Act 1's frame: the camera dives at it. */
const HAND_PT = (() => {
  const a = (HAND.angle * Math.PI) / 180;
  const d = DIAL.r * (0.24 * HAND.len + (HAND.len - 0.24 * HAND.len) * 0.55);
  return { x: DIAL.cx + Math.sin(a) * d, y: DIAL.cy - Math.cos(a) * d };
})();

export const Act2: React.FC = () => {
  const f = useCurrentFrame() + ACT.name.from;
  const [boxes, probe] = useLetters(SPELL, STYLE);

  // --- the dive into the hand (Act 1's last pose, then accelerating in) ---------------------
  const dive = prog(f, ACT.name.from - 1, CUE.flood, E.in);
  const k = lmix(1.04, 16, dive);
  const flooded = f >= CUE.flood;

  // --- the name ---------------------------------------------------------------------------
  const total = boxes ? boxes[boxes.length - 1].x + boxes[boxes.length - 1].w : 0;
  const left = (W - total) / 2;
  const iBox = boxes?.[2];
  const dotAt = iBox ? { x: left + iBox.x + iBox.w / 2 + 0.01 * SIZE, y: BASE - DOT.lift * SIZE } : { x: W / 2, y: H / 2 };
  // The name lifts away word-by-word style (whole word) once read, making room for the phone.
  const lift = depart(prog(f, MEANS.out, MEANS.out + f60(28), E.exit));

  // --- the shrinking flood: a petal from frame-filling to the dot --------------------------
  const sh = prog(f, CUE.flood + f60(6), CUE.dot, E.glide);
  const petal = { x: mix(W / 2, dotAt.x, sh), y: mix(H / 2, dotAt.y, sh), size: lmix(16 * W, DOT.size, sh) };
  // ...then it falls toward the phone (FALL), accelerating, from the dot.
  const fall = prog(f, FALL.from, FALL.at, E.contact);
  if (f >= CUE.dot) Object.assign(petal, { x: mix(dotAt.x, FALL.x, fall), y: mix(dotAt.y, FALL.y, fall), size: mix(DOT.size, FALL.size, fall) });

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      {probe}
      {!flooded && (
        // Act 1's dial, frozen in its last pose, pushed into
        <div style={{ position: 'absolute', inset: 0, transformOrigin: `${HAND_PT.x}px ${HAND_PT.y}px`, transform: `scale(${k})` }}>
          <svg width={W} height={H} style={{ position: 'absolute', left: 0, top: 0, overflow: 'visible' }}>
            <circle cx={DIAL.cx} cy={DIAL.cy} r={DIAL.r} fill={C.cobalt} />
            <path d={petalPath(DIAL.r * HAND.len, 1)} fill={C.coral} transform={`translate(${DIAL.cx} ${DIAL.cy}) rotate(${HAND.angle})`} />
            <circle cx={DIAL.cx} cy={DIAL.cy} r={DIAL.r * 0.05} fill={C.white} />
          </svg>
        </div>
      )}
      {flooded && boxes && (
        <>
          <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${left}px, ${BASE - 0.86 * SIZE - lift * 140 * U}px)`, opacity: 1 - lift }}>
            {Array.from(SPELL).map((ch, i) => {
              // the one hero word: letter by letter // lint-ok: hero word
              const s = NAME.in + f60(i * 4);
              const p = prog(f, s, s + f60(pace.wordDur), E.out);
              const b = blurIn(p, 6);
              return (
                <span
                  key={i}
                  style={{
                    ...STYLE,
                    position: 'absolute',
                    left: 0,
                    top: 0,
                    whiteSpace: 'pre',
                    transform: `translate(${boxes[i].x}px, ${((1 - arrive(p)) * 0.32 * SIZE).toFixed(2)}px)`,
                    opacity: p,
                    filter: b > 0.05 ? `blur(${b.toFixed(2)}px)` : undefined,
                    backgroundImage: `linear-gradient(180deg, ${C.skyTop} 10%, ${C.cobalt} 70%)`,
                    WebkitBackgroundClip: 'text',
                    backgroundClip: 'text',
                    color: 'transparent',
                  }}
                >
                  {ch}
                </span>
              );
            })}
          </div>
          <div style={{ position: 'absolute', left: 0, top: 0, width: W, display: 'flex', justifyContent: 'center', transform: `translateY(${BASE + 70 * U - lift * 100 * U}px)` }}>
            <Words text={MEANS.text} id={MEANS.id} frame={f} start={MEANS.in} out={MEANS.out} size={TYPE.line.size * 0.76 * U} style={{ ...typeStyle('line', TYPE.line.size * 0.76 * U), fontWeight: 600, color: C.ink }} />
          </div>
        </>
      )}
      {flooded && <Petal size={petal.size} color={C.coral} style={placePetal(petal.x, petal.y, petal.size, HAND.angle)} />}
      <Grain frame={f} opacity={0.04 * clamp(1)} />
    </AbsoluteFill>
  );
};
