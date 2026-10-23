// cinetic:brand-supplied coral: the sticker is the moment petal in Kairos' coral (BRIEF.md).
//
// The film's device on the share cards: the coral moment petal as a die-cut sticker. The cards
// themselves are the studio's own renders (src/kit/App.tsx CardArt).
import React from 'react';
import { petalPath } from '../brand/Mark';
import { C } from '../brand/tokens';

/** The die-cut sticker: the coral moment petal on a white rim. `size` px. */
export const Sticker: React.FC<{ size: number; rot?: number; style?: React.CSSProperties }> = ({ size, rot = 45, style }) => (
  <svg width={size} height={size} viewBox="-50 -50 100 100" style={{ overflow: 'visible', display: 'block', ...style }}>
    <g transform={`rotate(${rot}) translate(0 30)`}>
      <path d={petalPath(50, 1)} fill={C.white} stroke={C.white} strokeWidth={14} strokeLinejoin="round" />
      <path d={petalPath(50, 1)} fill={C.coral} />
    </g>
  </svg>
);

/** Act 7's hero card is laid out once at this design height and scaled by transform. */
export const DH = 640;
