// cinetic:brand-supplied gradient: the light rising is Kairos' own sky (BRIEF.md).
import React from 'react';
import { AbsoluteFill, Freeze, useCurrentFrame } from 'remotion';
import { C, TYPE, U, typeStyle } from '../brand/tokens';
import { Words } from '../fx/Words';
import { E, mix, prog } from '../lib/anim';
import { breatheT, KaiArt, Shot } from '../kit/App';
import { AppIcon } from '../kit/Icon';
import { Aurora, Grain, Phone } from '../kit/Kit';
import { tilt } from '../kit/rig';
import { ACT, copy, f60, H, W } from '../timeline';
import { Act1 } from './Act1';
import { PH2 } from './Act3';

// ACT 2 - THE NAME. Morning rises over the night in stepped columns (the move Act 6 uses again,
// the film's rhyme). On the left, set like the app's own headlines: its icon, then the name in
// Bricolage Display, then what it means. On the right the phone rises, turned toward the name, on
// the app's first screen: "Hi, I'm Kai." Nothing is centred; the name and the product share the
// frame like a sentence and its picture.

const NAME = copy('name.1');
const MEANS = copy('name.2');
const COLS = 12;
/** The name's face: the app's Display (Bricolage ExtraBold at display optical size, a touch condensed). */
export const DISPLAY: React.CSSProperties = { fontFamily: '"Bricolage", system-ui', fontWeight: 800, fontVariationSettings: '"wdth" 88, "opsz" 96', letterSpacing: '-0.04em', lineHeight: 0.9, whiteSpace: 'pre' };

export const Act2: React.FC = () => {
  const f = useCurrentFrame() + ACT.name.from;

  // --- the light rises from the bottom edge in columns, centre first --------------------------
  const cols = Array.from({ length: COLS }, (_, i) => {
    const d = Math.abs(i - (COLS - 1) / 2) * f60(2.4);
    return H * prog(f, ACT.name.from - 1 + d, ACT.name.from - 1 + d + f60(40), E.smooth);
  });
  const cw = W / COLS;
  const clip = `polygon(${cols.map((h, i) => `${(i * cw).toFixed(1)}px ${(H - h).toFixed(1)}px, ${((i + 1) * cw + 0.5).toFixed(1)}px ${(H - h).toFixed(1)}px`).join(', ')}, ${W}px ${H}px, 0px ${H}px)`;
  const closed = cols.every((h) => h >= H - 0.5);

  // --- the phone rises, turned toward the name -------------------------------------------------
  const rise = prog(f, ACT.name.from + f60(14), ACT.name.from + f60(70), E.cam);
  // --- the icon, the name, the meaning: each rises out of a clip, unblurring, no overshoot ------
  const icon = prog(f, NAME.in - f60(16), NAME.in + f60(14), E.out);
  const name = prog(f, NAME.in, NAME.resolved, E.out);
  const leave = prog(f, NAME.out - f60(6), ACT.name.from + ACT.name.dur, E.exit);

  const x0 = 150 * U;
  const NS = 196 * U;
  const iconS = 118 * U;
  const yIcon = H / 2 - 250 * U;
  const yName = yIcon + iconS + 40 * U;
  const ms = TYPE.line.size * 0.62 * U;

  return (
    <AbsoluteFill style={{ background: C.night2, overflow: 'hidden' }}>
      {!closed && (
        <Freeze frame={ACT.chronos.dur - 1}>
          <Act1 />
        </Freeze>
      )}
      <div style={{ position: 'absolute', inset: 0, clipPath: closed ? undefined : clip }}>
        <Aurora frame={f} />
        <Phone w={PH2.w} x={PH2.x} y={mix(H + 80 * U, PH2.y, rise)} {...tilt(f)} tone="dark">
          <Shot name="onboarding_hello" />
          {/* Kai on the welcome screen breathes, drawn by the app's own frames (matched on the render: centred at (200, 256) dp, 152 dp) */}
          <div style={{ position: 'absolute', left: 200 - 76, top: 256 - 76 }}>
            <KaiArt size={152} seq="breathe" t={breatheT(f)} />
          </div>
        </Phone>
        <div style={{ position: 'absolute', inset: 0, opacity: 1 - leave, transform: `translateY(${-leave * 30 * U}px)` }}>
          <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${x0}px, ${yIcon + (1 - icon) * 26 * U}px)`, opacity: icon, filter: icon < 1 ? `blur(${((1 - icon) * 10).toFixed(2)}px)` : undefined }}>
            <div style={{ borderRadius: '50%', boxShadow: `0 ${18 * U}px ${44 * U}px rgba(35,70,216,0.35)` }}>
              <AppIcon size={iconS} petals={(i) => prog(f, NAME.in - f60(10) + i * f60(2), NAME.in + f60(14) + i * f60(2), E.out)} />
            </div>
          </div>
          <div style={{ position: 'absolute', left: x0 - 8 * U, top: yName, height: NS * 1.02, overflow: 'hidden', paddingRight: 40 }}>
            <div style={{ ...DISPLAY, fontSize: NS, color: C.ink, transform: `translateY(${(1 - name) * NS}px)`, filter: name < 1 ? `blur(${((1 - name) * 8).toFixed(2)}px)` : undefined }}>{NAME.text}</div>
          </div>
          <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${x0}px, ${yName + NS * 1.12}px)` }}>
            <Words text={MEANS.text} id={MEANS.id} frame={f} start={MEANS.in} out={MEANS.out} size={ms} style={{ ...typeStyle('line', ms), fontWeight: 600, color: C.inkSoft }} />
          </div>
        </div>
        <Grain frame={f} opacity={0.035} />
      </div>
    </AbsoluteFill>
  );
};
