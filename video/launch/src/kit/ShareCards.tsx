// cinetic:brand-supplied coral, serif, gradient: Kairos' share cards (BRIEF.md).
//
// The share cards, one design per shape (pin 2:3, story 9:16, square 1:1), as the app's studio
// draws them: each is laid out once at the design height DH and scaled by transform.
import React from 'react';
import { Bloom, petalPath } from '../brand/Mark';
import { C } from '../brand/tokens';
import { SHARE } from './data';
import { Kai, SHADOW, ui, wordStyle } from './Kit';
import { SHARE_RATIO, type ShareShape } from './Screens';

/** The die-cut sticker: the coral moment petal on a white rim. `size` px. */
export const Sticker: React.FC<{ size: number; rot?: number; style?: React.CSSProperties }> = ({ size, rot = 45, style }) => (
  <svg width={size} height={size} viewBox="-50 -50 100 100" style={{ overflow: 'visible', display: 'block', ...style }}>
    <g transform={`rotate(${rot}) translate(0 30)`}>
      <path d={petalPath(50, 1)} fill={C.white} stroke={C.white} strokeWidth={14} strokeLinejoin="round" />
      <path d={petalPath(50, 1)} fill={C.coral} />
    </g>
  </svg>
);

/** Share cards are laid out once at this design height and scaled by transform (no per-frame layout). */
export const DH = 640;

/** The three designs, at the design height, for a given shape (each adapts its layout to the shape). */
export const ShareDesign: React.FC<{ shape: ShareShape }> = ({ shape }) => {
  const dw = DH * SHARE_RATIO[shape];
  const u = 1;
  if (shape === 'pin') {
    const s = SHARE.pin;
    return (
      <div style={{ position: 'absolute', inset: 0, background: C.cobalt }}>
        <div style={{ position: 'absolute', left: dw * 0.12, top: DH * 0.62 }}>
          <Bloom size={dw * 1.05} petal={C.white} moment={null} style={{ opacity: 0.16 }} />
        </div>
        <div style={{ position: 'absolute', left: 34 * u, top: 64 * u }}>
          <div style={wordStyle(76 * u, C.white)}>{s.word}</div>
          <div style={ui(16 * u, 500, C.white, { opacity: 0.8, marginTop: 14 * u })}>{`${s.say} · ${s.pos}`}</div>
          <div style={{ ...ui(22 * u, 400, C.white), whiteSpace: 'normal', width: dw - 70 * u, marginTop: 18 * u, lineHeight: 1.35 }}>{s.means}</div>
        </div>
        <div style={{ position: 'absolute', left: 34 * u, bottom: 30 * u, display: 'flex', alignItems: 'center', gap: 8 * u }}>
          <Bloom size={22 * u} petal={C.white} moment={C.coral} />
          <span style={wordStyle(20 * u, C.white, 600)}>Kairos</span>
        </div>
      </div>
    );
  }
  if (shape === 'story') {
    return (
      <div style={{ position: 'absolute', inset: 0, background: `linear-gradient(180deg, ${C.skyTop} 0%, ${C.skyMid} 55%, ${C.skyFade} 100%)` }}>
        <div style={{ position: 'absolute', left: 30 * u, top: 150 * u, width: dw - 60 * u }}>
          <div style={{ ...wordStyle(31 * u, C.white, 500), whiteSpace: 'pre', lineHeight: 1.18 }}>{SHARE.story.text}</div>
        </div>
        <div style={{ position: 'absolute', left: dw / 2 - 50 * u, top: DH * 0.66 }}>
          <Kai size={100 * u} look={[0, -0.4]} />
        </div>
        <div style={{ position: 'absolute', left: 0, right: 0, bottom: 30 * u, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 8 * u }}>
          <Bloom size={20 * u} petal={C.cobalt} moment={C.coral} />
          <span style={wordStyle(18 * u, C.ink, 600)}>Kairos</span>
        </div>
      </div>
    );
  }
  const s = SHARE.square;
  return (
    <div style={{ position: 'absolute', inset: 0, background: C.mint }}>
      <div style={{ position: 'absolute', right: -dw * 0.18, top: -dw * 0.18 }}>
        <Bloom size={dw * 0.8} petal={C.white} moment={null} style={{ opacity: 0.22 }} />
      </div>
      <div style={{ position: 'absolute', left: 40 * u, bottom: 46 * u }}>
        <div style={wordStyle(92 * u, C.white)}>{s.word}</div>
        <div style={{ ...ui(22 * u, 500, C.white), whiteSpace: 'normal', width: dw * 0.62, marginTop: 14 * u, lineHeight: 1.3 }}>{s.means}</div>
      </div>
    </div>
  );
};

/** A share card of a shape at height h (px on screen), with its sticker; laid out at DH, scaled. */
export const ShareCardAt: React.FC<{ shape: ShareShape; h: number; sticker?: boolean }> = ({ shape, h, sticker = true }) => (
  <div style={{ position: 'relative', width: DH * SHARE_RATIO[shape], height: DH, transformOrigin: '0 0', transform: `scale(${h / DH})` }}>
    <div style={{ position: 'absolute', inset: 0, borderRadius: DH * 0.045, overflow: 'hidden', boxShadow: SHADOW }}>
      <ShareDesign shape={shape} />
    </div>
    {sticker && <Sticker size={DH * 0.13} style={{ position: 'absolute', right: -DH * 0.03, top: -DH * 0.03 }} />}
  </div>
);

