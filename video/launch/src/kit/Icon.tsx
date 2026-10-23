// The app's launcher icon, drawn from its own adaptive-icon vectors
// (app/src/main/res/drawable/ic_launcher_background.xml and ic_launcher_foreground.xml): the cobalt
// sky with its soft top light, and the white bloom. Shown through the launcher's mask, a circle
// (Nothing OS's default icon shape): the adaptive icon's visible window is its inner 72 of 108 dp.
//
// The petals unfold the way the app unfolds its bloom (ui/art/Bloom.kt drawBloom, the onboarding
// wake-up): each petal grows out of the centre (scale u) while it turns the last 28 degrees into
// place, and the core grows last. No fades and no overshoot: at u = 1 every path is the XML's own.
import React, { useId } from 'react';
import { C } from '../brand/tokens';

const LONG = 'M54.00,48.24C56.07,44.96 58.92,39.35 58.92,34.92C58.92,32.21 56.71,30.00 54.00,30.00C51.29,30.00 49.08,32.21 49.08,34.92C49.08,39.35 51.93,44.96 54.00,48.24Z';
const SHORT = 'M54.00,48.24C56.07,45.61 58.92,43.00 58.92,38.57C58.92,35.86 56.71,33.65 54.00,33.65C51.29,33.65 49.08,35.86 49.08,38.57C49.08,43.00 51.93,45.61 54.00,48.24Z';
/** The XML's petals, in order: rotation and whether it is a long one (0, 45, 90, 180, 270 are long). */
const PETALS: [number, string][] = [[0, LONG], [45, LONG], [90, LONG], [135, SHORT], [180, LONG], [225, SHORT], [270, LONG], [315, SHORT]];
/** Bloom.MOMENT: the full-length petal at one o'clock (45 degrees), where the app's unfold begins. */
export const MOMENT_PETAL = 1;
/** The order the app unfolds its petals: clockwise from the moment petal. */
export const UNFOLD_ORDER = [1, 2, 3, 4, 5, 6, 7, 0];
/** drawBloom: a petal turns the last 28 degrees into place as it grows. */
const TURN = 28;
const CORE_R = 6.24;

export const AppIcon: React.FC<{
  size: number;
  /** Each petal's growth, 0..1 (index as in the XML: 0 at twelve, clockwise). */
  petals?: (i: number) => number;
  /** The core's growth, 0..1. */
  core?: number;
  style?: React.CSSProperties;
}> = ({ size, petals = () => 1, core = 1, style }) => {
  // one set of ids per instance, so two icons on screen never share a gradient or a mask
  const id = `kicon${useId().replace(/[^a-zA-Z0-9]/g, '')}`;
  const k = Math.max(0, Math.min(1, core));
  return (
    <svg width={size} height={size} viewBox="18 18 72 72" style={{ display: 'block', overflow: 'visible', ...style }}>
      <defs>
        <linearGradient id={`${id}-sky`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor={C.iconTop} />
          <stop offset="1" stopColor={C.iconBottom} />
        </linearGradient>
        <radialGradient id={`${id}-light`} cx="54" cy="20" r="52" gradientUnits="userSpaceOnUse">
          <stop offset="0" stopColor={C.white} stopOpacity={0.25} />
          <stop offset="1" stopColor={C.white} stopOpacity={0} />
        </radialGradient>
        <clipPath id={`${id}-mask`}>
          <circle cx="54" cy="54" r="36" />
        </clipPath>
      </defs>
      <g clipPath={`url(#${id}-mask)`}>
        <rect x="0" y="0" width="108" height="108" fill={`url(#${id}-sky)`} />
        <circle cx="54" cy="20" r="52" fill={`url(#${id}-light)`} />
        {PETALS.map(([rot, d], i) => {
          const u = Math.max(0, Math.min(1, petals(i)));
          if (u <= 0.001) return null;
          return <path key={i} d={d} fill={C.white} transform={`rotate(${(rot - (1 - u) * TURN).toFixed(3)} 54 54) translate(54 54) scale(${u.toFixed(4)}) translate(-54 -54)`} />;
        })}
        {k > 0.001 && <circle cx="54" cy="54" r={CORE_R * k} fill={C.white} />}
      </g>
    </svg>
  );
};
