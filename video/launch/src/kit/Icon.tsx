// The app's launcher icon, drawn from its own adaptive-icon vectors
// (app/src/main/res/drawable/ic_launcher_background.xml and ic_launcher_foreground.xml): the cobalt
// sky with its soft top light, and the white bloom. Shown through the launcher's mask, a circle
// (Nothing OS's default icon shape): the adaptive icon's visible window is its inner 72 of 108 dp.
import React from 'react';
import { C } from '../brand/tokens';

const LONG = 'M54.00,48.24C56.07,44.96 58.92,39.35 58.92,34.92C58.92,32.21 56.71,30.00 54.00,30.00C51.29,30.00 49.08,32.21 49.08,34.92C49.08,39.35 51.93,44.96 54.00,48.24Z';
const SHORT = 'M54.00,48.24C56.07,45.61 58.92,43.00 58.92,38.57C58.92,35.86 56.71,33.65 54.00,33.65C51.29,33.65 49.08,35.86 49.08,38.57C49.08,43.00 51.93,45.61 54.00,48.24Z';
/** The XML's petals, in order: rotation and whether it is a long one (0, 45, 90, 180, 270 are long). */
const PETALS: [number, string][] = [[0, LONG], [45, LONG], [90, LONG], [135, SHORT], [180, LONG], [225, SHORT], [270, LONG], [315, SHORT]];

export const AppIcon: React.FC<{ size: number; petals?: (i: number) => number; style?: React.CSSProperties }> = ({ size, petals = () => 1, style }) => {
  const id = 'kairos-icon';
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
          return <path key={i} d={d} fill={C.white} opacity={u} transform={`rotate(${rot - (1 - u) * 20} 54 54) translate(54 54) scale(${0.6 + 0.4 * u}) translate(-54 -54)`} />;
        })}
        <circle cx="54" cy="54" r="6.24" fill={C.white} />
      </g>
    </svg>
  );
};
