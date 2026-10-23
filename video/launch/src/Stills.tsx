// cinetic:brand-supplied coral, serif: the bloom's moment petal and the Fraunces wordmark (BRIEF.md).
import React from 'react';
import { AbsoluteFill } from 'remotion';
import { Bloom } from './brand/Mark';
import { C, U, WORD_FACE } from './brand/tokens';
import { FontGate } from './lib/FontGate';

// Style frames: rendered with `npm run stills` and looked at before any animation is built.
// The mark must hold up at favicon size and at full screen, on the canvas and on cobalt.

type Surface = 'paper' | 'ink';
const ground = (s: Surface) => (s === 'ink' ? C.cobalt : C.canvas);
const petal = (s: Surface) => (s === 'ink' ? C.white : C.cobalt);

export const MarkStill: React.FC<{ size: number; surface: Surface }> = ({ size, surface }) => (
  <AbsoluteFill style={{ background: ground(surface), alignItems: 'center', justifyContent: 'center' }}>
    <Bloom size={size} petal={petal(surface)} />
  </AbsoluteFill>
);

const SIZES = [16, 24, 32, 48, 64, 128, 256];

/** The mark at every working size on both surfaces: the petals must stay apart and the coral must read. */
export const MarkSizes: React.FC = () => (
  <AbsoluteFill style={{ background: C.canvas }}>
    {(['paper', 'ink'] as const).map((surface) => (
      <div key={surface} style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 64 * U, background: ground(surface) }}>
        {SIZES.map((s) => (
          <Bloom key={s} size={s * U} petal={petal(surface)} />
        ))}
      </div>
    ))}
  </AbsoluteFill>
);

type Ground = 'light' | 'dark' | 'accent';
const inkOn = (g: Ground) => (g === 'light' ? C.cobalt : C.white);
const groundOf = (g: Ground) => (g === 'light' ? C.canvas : g === 'dark' ? C.cobalt : C.coral);

/** The lockup as the film ends on it: the bloom beside "Kairos" in Fraunces. */
const Lockup: React.FC<{ nameSize: number; ink: string; cx: number; cy: number }> = ({ nameSize, ink, cx, cy }) => (
  <div style={{ position: 'absolute', left: 0, top: 0, transform: `translate(${cx}px, ${cy}px) translate(-50%, -50%)`, display: 'flex', alignItems: 'center', gap: nameSize * 0.22 }}>
    <Bloom size={nameSize * 0.92} petal={ink} />
    <span style={{ ...WORD_FACE, fontSize: nameSize, fontWeight: 600, letterSpacing: '-0.03em', lineHeight: 1, color: ink }}>Kairos</span>
  </div>
);

export const BrandMark: React.FC<{ size: number; ground: Ground }> = ({ size, ground: g }) => (
  <AbsoluteFill>
    <Bloom size={size} petal={inkOn(g)} />
  </AbsoluteFill>
);

export const BrandLockup: React.FC<{ ground: Ground }> = ({ ground: g }) => (
  <FontGate>
    <AbsoluteFill>
      <Lockup nameSize={240} ink={inkOn(g)} cx={1200} cy={400} />
    </AbsoluteFill>
  </FontGate>
);

export const BrandAvatar: React.FC<{ ground: Ground }> = ({ ground: g }) => (
  <AbsoluteFill style={{ background: groundOf(g), alignItems: 'center', justifyContent: 'center' }}>
    <Bloom size={220} petal={inkOn(g)} moment={g === 'accent' ? C.white : C.coral} />
  </AbsoluteFill>
);

export const BrandHeader: React.FC<{ ground: Ground }> = ({ ground: g }) => (
  <FontGate>
    <AbsoluteFill style={{ background: groundOf(g) }}>
      <Lockup nameSize={120} ink={inkOn(g)} cx={750} cy={230} />
    </AbsoluteFill>
  </FontGate>
);
