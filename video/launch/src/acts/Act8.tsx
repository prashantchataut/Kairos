// cinetic:brand-supplied gradient: Kairos' sky, icon and Today screen (BRIEF.md).
import React from 'react';
import { AbsoluteFill, Freeze, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, lmix, mix, prog } from '../lib/anim';
import { blinkT, KaiArt, Shot, TODAY as T } from '../kit/App';
import { AppIcon } from '../kit/Icon';
import { Aurora, Grain, Phone } from '../kit/Kit';
import { tilt } from '../kit/rig';
import { ACT, copy, CUE, f60, H, TOTAL, W } from '../timeline';
import { DISPLAY } from './Act2';
import { Act7 } from './Act7';

// ACT 8 - THE MOMENT. Morning rises over the night in columns once more (the film's rhyme: Act 2
// opened the day this way, Act 6 closed it). The app's icon arrives and unfolds its petals one by
// one, clockwise from one o'clock, and its core lands; "Kairos" rises beside the phone, which comes
// up on Today as it is every morning; "Now on Android". A slow push to the last frame.

const NAME = copy('wordmark');
const ANDROID = copy('android');
const COLS = 12;
const PH8 = { w: 540 * U, x: W * 0.57, y: 120 * U };

export const Act8: React.FC = () => {
  const f = useCurrentFrame() + ACT.lockup.from;

  const cols = Array.from({ length: COLS }, (_, i) => {
    const d = Math.abs(i - (COLS - 1) / 2) * f60(2.4);
    return H * prog(f, CUE.scatter - 1 + d, CUE.scatter - 1 + d + f60(40), E.smooth);
  });
  const cw = W / COLS;
  const clip = `polygon(${cols.map((h, i) => `${(i * cw).toFixed(1)}px ${(H - h).toFixed(1)}px, ${((i + 1) * cw + 0.5).toFixed(1)}px ${(H - h).toFixed(1)}px`).join(', ')}, ${W}px ${H}px, 0px ${H}px)`;
  const closed = cols.every((h) => h >= H - 0.5);

  const icon = prog(f, CUE.unfold - f60(8), CUE.unfold + f60(22), E.out);
  // the petals open clockwise from the moment petal (index 1, one o'clock), the core on CUE.core
  const order = [1, 2, 3, 4, 5, 6, 7, 0];
  const petal = (i: number) => {
    const s = CUE.unfold + order.indexOf(i) * f60(4);
    return prog(f, s, s + f60(22), E.out);
  };
  const name = prog(f, NAME.in, NAME.resolved, E.out);
  const rise = prog(f, CUE.unfold, CUE.unfold + f60(64), E.cam);
  const push = lmix(1, 1.05, prog(f, CUE.core, TOTAL, E.dolly));

  const x0 = 150 * U;
  const iconS = 150 * U;
  const NS = 210 * U;
  const yIcon = H / 2 - 290 * U;
  const yName = yIcon + iconS + 44 * U;
  const ms = TYPE.line.size * 0.62 * U;

  return (
    <AbsoluteFill style={{ background: C.night2, overflow: 'hidden' }}>
      {!closed && (
        <Freeze frame={ACT.share.dur - 1}>
          <Act7 />
        </Freeze>
      )}
      <div style={{ position: 'absolute', inset: 0, clipPath: closed ? undefined : clip }}>
        <Aurora frame={f} />
        <div style={{ position: 'absolute', inset: 0, transformOrigin: `${W * 0.4}px ${H / 2}px`, transform: `scale(${push})` }}>
          <Phone w={PH8.w} x={PH8.x} y={mix(H + 80 * U, PH8.y, rise)} {...tilt(f)}>
            <Shot name="today" />
            <div style={{ position: 'absolute', left: T.kai.x - T.kai.size / 2, top: T.kai.y - T.kai.size / 2 }}>
              <KaiArt size={T.kai.size} seq="blink" t={blinkT(f, CUE.lockup + f60(30), f60(14))} />
            </div>
          </Phone>
          <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${x0}px, ${yIcon + (1 - icon) * 26 * U}px)`, opacity: icon, filter: icon < 1 ? `blur(${((1 - icon) * 10).toFixed(2)}px)` : undefined }}>
            <div style={{ borderRadius: '50%', boxShadow: `0 ${22 * U}px ${54 * U}px rgba(35,70,216,0.38)` }}>
              <AppIcon size={iconS} petals={petal} />
            </div>
          </div>
          <div style={{ position: 'absolute', left: x0 - 8 * U, top: yName, height: NS * 1.02, overflow: 'hidden', paddingRight: 40 }}>
            <div style={{ ...DISPLAY, fontSize: NS, color: C.ink, transform: `translateY(${(1 - name) * NS}px)`, filter: name < 1 ? `blur(${((1 - name) * 8).toFixed(2)}px)` : undefined }}>{NAME.text}</div>
          </div>
          <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${x0}px, ${yName + NS * 1.1}px)` }}>
            <Words text={ANDROID.text} id={ANDROID.id} frame={f} start={ANDROID.in} size={ms} style={{ ...typeStyle('line', ms), fontWeight: 600, color: C.inkSoft }} />
          </div>
        </div>
        <Grain frame={f} opacity={0.035} />
      </div>
    </AbsoluteFill>
  );
};
