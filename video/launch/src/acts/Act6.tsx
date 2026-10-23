// cinetic:brand-supplied coral, gradient: the time capsule's night and screens are Kairos' own (BRIEF.md).
import React from 'react';
import { AbsoluteFill, Freeze, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, fd, lmix, prog, spr, SPR, squash } from '../lib/anim';
import { delayTo } from '../lib/sync';
import { CAPSULE, ComposeTyping, SealedStamp, Shot } from '../kit/App';
import { Grain, nightGradient, Phone, phoneSize } from '../kit/Kit';
import { tilt } from '../kit/rig';
import { ACT, copy, CUE, f60, H, W } from '../timeline';
import { PH } from './Act3';
import { Act5 } from './Act5';

// ACT 6 - THE LETTER. Night rises over Play in stepped columns (tr-stepped-column-fill), the time
// capsule's sky, and the phone inside it is already on the app's letter screen (compose_write):
// its own title types itself, "Dear future me...". "Write to" waits on the left. The app pages on
// to choosing the day (compose_when), then the sealed letter (letter_sealed): its coral seal comes
// down onto the envelope (landing 1). "future you." answers on the right. The camera leans into
// the date and the countdown; then the phone turns edge-on, and Act 7 turns it into a share card.

const A = copy('write.1');
const B = copy('write.2');
const DEAR = copy('ui.dear');
const COLS = 15;
const S = phoneSize(PH.w);
/** Contact frame of the seal: the spring starts early by exactly the frames it needs. */
const SEAL_START = CUE.seal - delayTo(SPR.land);

/** Screen px of an app-unit point on the phone. */
const onPhone = (x: number, y: number) => ({ x: PH.x + S.bezel + x * S.k, y: PH.y + S.top + y * S.k });

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

  // --- the screens: writing, choosing the day, sealed -----------------------------------------
  const typed = (fd(f) - DEAR.in) / f60(2); // one character every 2 frames, a key sound each (sync.ts)
  const toWhen = prog(f, CUE.fold - f60(16), CUE.fold + f60(4), E.inOut); // the app's page push
  const toSealed = prog(f, SEAL_START - f60(10), SEAL_START, E.smooth); // the sealed page fades up
  const land = f >= CUE.seal ? 1 : spr(f, SEAL_START, SPR.land);
  const sq = squash(f - CUE.seal);

  // --- the camera: a slow lean into the date once it opens, then the turn to edge-on ----------
  const date = onPhone(200, 450);
  const lean = lmix(1, 1.06, prog(f, CUE.opens - f60(10), CUE.flip, E.inOut));
  const turn = prog(f, CUE.flip, ACT.letter.from + ACT.letter.dur, E.in) * 90;
  const seal = onPhone(CAPSULE.seal.x, CAPSULE.seal.y);

  const size = TYPE.line.size * 0.74 * U;
  const lineY = seal.y + 60 * U - size * 0.55;

  return (
    <AbsoluteFill style={{ background: C.night2, overflow: 'hidden' }}>
      {!closed && (
        <Freeze frame={ACT.play.dur - 1}>
          <Act5 />
        </Freeze>
      )}
      <div style={{ position: 'absolute', inset: 0, background: nightGradient, clipPath: closed ? undefined : clip }} />

      {/* until the night has closed, the phone only exists inside it */}
      <div style={{ position: 'absolute', inset: 0, clipPath: closed ? undefined : clip }}>
        <div style={{ position: 'absolute', inset: 0, transformOrigin: `${date.x}px ${date.y}px`, transform: `scale(${lean})`, perspective: 1800 * U }}>
          <Phone w={PH.w} x={PH.x} y={PH.y} rx={tilt(f).rx} ry={tilt(f).ry + turn} rz={tilt(f).rz}>
            {toWhen < 1 && (
              <div style={{ position: 'absolute', inset: 0, transform: `translateX(${-toWhen * 120}px)`, opacity: 1 - toWhen * 0.5 }}>
                <ComposeTyping typed={typed} total={DEAR.text.length} />
              </div>
            )}
            {toWhen > 0 && toSealed < 1 && (
              <div style={{ position: 'absolute', inset: 0, transform: `translateX(${(1 - toWhen) * 400}px)`, boxShadow: '-20px 0 60px rgba(0,0,0,0.25)' }}>
                <Shot name="compose_when" />
              </div>
            )}
            {toSealed > 0 && (
              <div style={{ position: 'absolute', inset: 0, opacity: toSealed }}>
                <SealedStamp land={land} squash={sq.across - 1 > 0 ? (sq.across - 1) / 0.08 : 0} />
              </div>
            )}
          </Phone>
        </div>
      </div>

      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x - 56 * U}px, ${lineY}px) translateX(-100%)` }}>
        <Words text={A.text} id={A.id} frame={f} start={A.in} out={A.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${PH.x + PH.w + 56 * U}px, ${lineY}px)` }}>
        <Words text={B.text} id={B.id} frame={f} start={B.in} out={B.out} size={size} style={{ ...typeStyle('line', size), color: C.white }} />
      </div>
      <Grain frame={f} opacity={0.05} />
    </AbsoluteFill>
  );
};
