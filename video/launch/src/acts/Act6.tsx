// cinetic:brand-supplied coral, serif, handwriting, gradient: the time capsule's night, letter and
// envelope are Kairos' own (BRIEF.md).
import React from 'react';
import { AbsoluteFill, Freeze, useCurrentFrame } from 'remotion';
import { Bloom } from '../brand/Mark';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, fd, hitPulse, lmix, mix, prog, spr, SPR, squash } from '../lib/anim';
import { delayTo } from '../lib/sync';
import { LETTER } from '../kit/data';
import { Chip, Grain, nightGradient } from '../kit/Kit';
import { Envelope, ENVELOPE, LETTER_SIZE, LetterPaper } from '../kit/Screens';
import { ACT, copy, CUE, f60, H, W } from '../timeline';
import { Act5 } from './Act5';

// ACT 6 - THE LETTER. Night rises over the deck in stepped columns (tr-stepped-column-fill), the
// time capsule's sky. A letter slides up already being written: "Dear future me,". "Write to"
// waits on the left. The letter folds down into its envelope, the flap closes, and the coral petal
// drops onto it as the wax seal (landing 1). "future you." answers on the right; "Opens in 6
// months" rolls in below. Then the envelope turns edge-on, and Act 7 turns it into a share card.

const A = copy('write.1');
const B = copy('write.2');
const DEAR = copy('ui.dear');
const OPENS = copy('ui.opens');
const COLS = 15;

/** The envelope at rest (centre), and its turn to edge-on for the hand-off to Act 7. */
export const ENV = { x: W / 2, y: H / 2 + 10 * U, k: 1.12 * U };
/** While it is written the letter is centred in the frame (envelope units, relative to the envelope's top). */
const WRITE_TOP = (H / 2 - (LETTER_SIZE.h * 0.92 * ENV.k) / 2 - (ENV.y - (ENVELOPE.h * ENV.k) / 2)) / ENV.k;
/** Contact frame of the seal: the spring starts early by exactly the frames it needs. */
const SEAL_START = CUE.seal - delayTo(SPR.land);

export const Act6: React.FC = () => {
  const f = useCurrentFrame() + ACT.letter.from;

  // --- night columns rise from the bottom edge, centre first, and close into one plate --------
  const columns = Array.from({ length: COLS }, (_, i) => {
    const d = Math.abs(i - (COLS - 1) / 2) * f60(2);
    return H * prog(f, CUE.night - 1 + d, CUE.night - 1 + d + f60(40), E.smooth);
  });
  const cw = W / COLS;
  const clip = `polygon(${columns.map((h, i) => `${(i * cw).toFixed(1)}px ${(H - h).toFixed(1)}px, ${((i + 1) * cw + 0.5).toFixed(1)}px ${(H - h).toFixed(1)}px`).join(', ')}, ${W}px ${H}px, 0px ${H}px)`;
  const closed = columns.every((h) => h >= H - 0.5);

  // --- the letter: up already moving, then folding into the envelope ----------------------
  const up = prog(f, CUE.paper - 1, CUE.paper + f60(40), E.out);
  const typed = (fd(f) - DEAR.in) / f60(2); // 30 characters a second
  const lines = prog(f, DEAR.resolved, DEAR.resolved + f60(56), E.linear);
  const fold = prog(f, CUE.fold - f60(26), CUE.fold, E.inOut); // the letter sinks into the pocket
  const envIn = prog(f, CUE.fold - f60(40), CUE.fold - f60(10), E.out);
  // the letter's offset in envelope units (it rides free of the envelope until the envelope has arrived)
  const letterY = mix(WRITE_TOP + (1 - up) * 900, 40, fold) - ((1 - envIn) * 1150 * U) / ENV.k;
  const letterSY = mix(0.92, 0.42, fold);
  const flap = prog(f, CUE.fold - f60(2), CUE.fold + f60(14), E.inOut);

  // --- the seal: the coral petal drops and lands, squashing against the flap ---------------
  const sl = f >= CUE.seal ? 1 : spr(f, SEAL_START, SPR.land);
  const sq = squash(f - CUE.seal);
  const sealY = mix(-420 * U, 0, sl);

  // --- the turn to edge-on (Act 7 continues it) and a slow push through the act ------------
  const turn = prog(f, CUE.flip, ACT.letter.from + ACT.letter.dur, E.in) * 90;
  const push = lmix(1, 1.05, prog(f, CUE.seal, CUE.flip, E.linear)) * (1 + 0.02 * hitPulse(f - CUE.seal, 2, 6));
  const opens = prog(f, OPENS.in - 1, OPENS.resolved, E.out);

  const size = TYPE.line.size * 0.74 * U;
  const lineY = ENV.y - size * 0.55;
  const env = { w: ENVELOPE.w * ENV.k, h: ENVELOPE.h * ENV.k };

  return (
    <AbsoluteFill style={{ background: C.night2, overflow: 'hidden' }}>
      {!closed && (
        <Freeze frame={ACT.play.dur - 1}>
          <Act5 />
        </Freeze>
      )}
      <div style={{ position: 'absolute', inset: 0, background: nightGradient, clipPath: closed ? undefined : clip }} />

      {/* until the night has closed, the letter only exists inside it */}
      <div style={{ position: 'absolute', inset: 0, clipPath: closed ? undefined : clip }}>
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${ENV.x}px ${ENV.y}px`, transform: `scale(${push})` }}>
        {/* the envelope, behind the letter until the letter is in */}
        <div style={{ position: 'absolute', left: 0, top: 0, perspective: 1600 * U, transform: `translate(${ENV.x - env.w / 2}px, ${ENV.y - env.h / 2 + (1 - envIn) * 1150 * U}px)` }}>
          <div style={{ transformOrigin: '50% 50%', transform: `scale(${ENV.k}) rotateY(${turn}deg)`, width: ENVELOPE.w, height: ENVELOPE.h }}>
            <Envelope flap={flap} color={C.skyCandy}>
              {/* the letter, folding down into the pocket */}
              {fold < 1 && (
                <div
                  style={{
                    position: 'absolute',
                    left: (ENVELOPE.w - LETTER_SIZE.w * 0.92) / 2,
                    top: 0,
                    width: LETTER_SIZE.w,
                    height: LETTER_SIZE.h,
                    transformOrigin: '50% 0',
                    clipPath: `inset(-60px -60px ${Math.max(0, LETTER_SIZE.h - (ENVELOPE.h - 6 - letterY) / letterSY).toFixed(1)}px -60px)`,
                    transform: `translateY(${letterY}px) scale(${mix(0.92, 0.9, fold)}, ${letterSY})`,
                  }}
                >
                  <LetterPaper typed={typed} lines={lines} />
                </div>
              )}
            </Envelope>
            {/* the seal: the moment petal in coral, on the flap's tip */}
            {f >= SEAL_START - 2 && (
              <div style={{ position: 'absolute', left: ENVELOPE.w / 2 - 50, top: ENVELOPE.h * 0.6 - 50, width: 100, height: 100, transformOrigin: '50% 100%', transform: `translateY(${sealY}px) scale(${sq.across}, ${sq.along})` }}>
                <div style={{ position: 'absolute', inset: 0, borderRadius: 50, background: C.coral }} />
                <div style={{ position: 'absolute', left: 14, top: 14 }}>
                  <Bloom size={72} petal={C.white} moment={C.coral} core={C.white} />
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
      </div>

      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${ENV.x - env.w / 2 - 64 * U}px, ${lineY}px) translateX(-100%)` }}>
        <Words text={A.text} id={A.id} frame={f} start={A.in} out={A.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${ENV.x + env.w / 2 + 64 * U}px, ${lineY}px)` }}>
        <Words text={B.text} id={B.id} frame={f} start={B.in} out={B.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      {opens > 0 && (
        <div data-ui="opens" style={{ position: 'absolute', left: 0, top: 0, width: W, display: 'flex', justifyContent: 'center', transform: `translateY(${ENV.y + env.h / 2 + 48 * U + (1 - opens) * 30 * U}px)`, opacity: opens * (1 - prog(f, OPENS.out, OPENS.out + 12, E.smooth)) }}>
          <Chip bg="rgba(255,255,255,0.14)" color={C.white} size={32 * U}>
            {LETTER.opens}
          </Chip>
        </div>
      )}
      <Grain frame={f} opacity={0.05} />
    </AbsoluteFill>
  );
};
