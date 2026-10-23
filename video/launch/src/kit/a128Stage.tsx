// Stage pieces for Acts 2 and 8: the morning rising in stepped columns (the film's rhyme), the
// phone posed in depth (kit/Device with a z and a shadow), and the name's lockup: the app's icon and
// "Kairos" in the app's Display face rising over a hairline horizon, with a line of copy under it.
import React from 'react';
import { C, U } from '../brand/tokens';
import { columnAt, COLUMNS, type NameBeat, petalAt } from '../fx/a128Time';
import { BlurWords, letterSlots, nameMetrics, RisingName } from '../fx/a128Type';
import { arrive, arriveK, clamp, depart, E, prog } from '../lib/anim';
import { inkBox } from '../lib/measure';
import { copy, f60, H, W } from '../timeline';
import { Device, deviceSize, SCREEN, type Tone } from './Device';
import { AppIcon, UNFOLD_ORDER } from './Icon';

// ------------------------------------------------------------------------------------------
// The columns: a clip-path for whatever is rising over the last shot, and whether it has closed.

export const columnsClip = (f: number, from: number) => {
  const cw = W / COLUMNS.n;
  const hs = Array.from({ length: COLUMNS.n }, (_, i) => H * columnAt(f, from, i));
  const clip = `polygon(${hs.map((h, i) => `${(i * cw).toFixed(1)}px ${(H - h).toFixed(1)}px, ${((i + 1) * cw + 0.5).toFixed(1)}px ${(H - h).toFixed(1)}px`).join(', ')}, ${W}px ${H}px, 0px ${H}px)`;
  return { clip, closed: hs.every((h) => h >= H - 0.5) };
};

// ------------------------------------------------------------------------------------------
// The phone placed by its top-left corner like kit/Kit's Phone, with depth (z) and its shadow's
// strength, for arrivals with weight. Children are the screen below the status bar (400 x 860 dp).

export const PosedPhone: React.FC<{ w: number; x: number; y: number; rx?: number; ry?: number; rz?: number; z?: number; shadow?: number; tone?: Tone; children: React.ReactNode }> = ({ w, x, y, rx = 0, ry = 0, rz = 0, z = 0, shadow = 1, tone = 'light', children }) => {
  const d = deviceSize(w);
  return (
    <Device p={{ x: x + w / 2, y: y + d.h / 2, w, rx, ry, rz, z }} tone={tone} shadow={shadow}>
      <div style={{ position: 'absolute', left: 0, top: SCREEN.status, width: SCREEN.w, height: 860 }}>{children}</div>
    </Device>
  );
};

// ------------------------------------------------------------------------------------------
// The lockup. The icon sits on the name's cap height (a circle drawn a touch larger than the caps,
// so it reads the same size), left of the name; a hairline horizon runs under both. The line draws
// first, the icon rises over it and unfolds the app's way, the letters rise from behind it, and the
// line of copy blurs in under it. Leaving (Act 2), all of it sinks back behind the horizon.

/** "Kairos" as the app sets its headlines: Display, Bricolage ExtraBold at display optical size, a touch condensed. */
export const NAME_FACE: React.CSSProperties = {
  fontFamily: '"Bricolage", system-ui',
  fontWeight: 800,
  fontVariationSettings: '"wdth" 88, "opsz" 96',
  letterSpacing: '-0.03em',
  color: C.ink,
  whiteSpace: 'pre',
};

export type LockupGeometry = { iconX: number; iconY: number; iconS: number; nameX: number; nameW: number; lineY: number; cap: number };

/** Where the lockup's parts sit for a name of `size` px whose baseline is at `baseline`, starting at x. */
export const lockupGeometry = (id: string, x: number, baseline: number, size: number): LockupGeometry => {
  const cap = inkBox('K', size, 800, String(NAME_FACE.fontFamily)).ascent;
  const iconS = cap * 1.16;
  const nameX = x + iconS + size * 0.15;
  return { iconX: x, iconY: baseline - cap / 2 - iconS / 2, iconS, nameX, nameW: letterSlots(copy(id).text, size, NAME_FACE).width, lineY: baseline + size * 0.12, cap };
};

export const Lockup: React.FC<{
  frame: number;
  nb: NameBeat;
  id: string; // the name's COPY id
  x: number; // the icon's left edge
  baseline: number; // the name's baseline
  size: number; // the name's size
  sub?: { id: string; size: number; face: React.CSSProperties; gap?: number }; // the copy under the line
  lineRight?: number; // px the horizon runs past the name
}> = ({ frame: f, nb, id, x, baseline, size, sub, lineRight = 0 }) => {
  const g = lockupGeometry(id, x, baseline, size);
  const lineW = g.nameX + g.nameW + lineRight - x;
  const hair = Math.max(1.5, 2 * U);

  // the horizon: draws left to right; leaving, it retracts to the right
  const drawn = prog(f, nb.line[0], nb.line[1], E.out);
  const gone = prog(f, nb.out + f60(2), nb.out + f60(16), E.in);

  // the icon: up from wholly under the line, then its petals and core, the app's unfold
  const travel = g.lineY - g.iconY + 4;
  const rise = prog(f, nb.icon[0], nb.icon[1], E.out);
  const sink = prog(f, nb.out - f60(2), nb.out + f60(13), E.in);
  const iconDy = (1 - arrive(rise, arriveK(travel, nb.icon[1] - nb.icon[0], E.out))) * travel + depart(sink) * travel;
  const petals = petalAt(nb, UNFOLD_ORDER, f);
  const core = prog(f, nb.core[0], nb.core[1], E.outSoft);
  const shadow = Math.pow(rise, 3) * (1 - sink);

  // the copy under the line: its baseline a cap and a gap below the horizon
  const subTop = sub ? g.lineY + (sub.gap ?? sub.size * 0.42) : 0;

  return (
    <>
      {/* the icon's shadow, outside the horizon's clip (it fades in only once the icon is up) */}
      {shadow > 0.002 && (
        <div style={{ position: 'absolute', left: 0, top: 0, width: g.iconS, height: g.iconS, borderRadius: '50%', transform: `translate(${g.iconX}px, ${g.iconY + iconDy}px)`, boxShadow: `0 ${0.14 * g.iconS}px ${0.36 * g.iconS}px ${C.night3}38`, opacity: shadow }} />
      )}
      {/* everything that rises lives above the horizon */}
      <div style={{ position: 'absolute', left: 0, top: 0, width: W, height: g.lineY, overflow: 'hidden', pointerEvents: 'none' }}>
        {rise > 0 && sink < 1 && (
          <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${g.iconX}px, ${(g.iconY + iconDy).toFixed(3)}px)` }}>
            <AppIcon size={g.iconS} petals={petals} core={core} />
          </div>
        )}
      </div>
      <RisingName id={id} frame={f} x={g.nameX} baseline={baseline} line={g.lineY} size={size} face={NAME_FACE} letters={nb.letters} out={nb.out} />
      {drawn > 0 && gone < 1 && (
        <div
          style={{
            position: 'absolute',
            left: 0,
            top: 0,
            width: lineW,
            height: hair,
            background: `${C.ink}2E`,
            transformOrigin: gone > 0 ? '100% 50%' : '0% 50%',
            transform: `translate(${x}px, ${g.lineY - hair / 2}px) scaleX(${(gone > 0 ? 1 - gone : drawn).toFixed(4)})`,
          }}
        />
      )}
      {sub && (
        <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${g.nameX}px, ${subTop}px)` }}>
          <BlurWords id={sub.id} frame={f} size={sub.size} face={sub.face} lift={-0.25} />
        </div>
      )}
    </>
  );
};

/** Ink metrics re-exported for acts that lay copy out against the name. */
export { nameMetrics, clamp };
