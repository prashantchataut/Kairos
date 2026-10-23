// The phone: a Nothing Phone (4a), modelled from its real proportions (164 x 77.6 x 8.6 mm, a flat
// 6.78" 20:9 panel, so ~3.4 mm bezels all round, a centred punch-hole camera, glass front, a matte
// frame). It is a 3D object, not an overlay: the frame has depth (stacked planes), the glass a sheen
// that moves with the angle, the screen an Android status bar and gesture bar over the app's renders.
//
// Screens are laid out in dp on a 400 x 889 canvas (the panel's 20:9 at the app's 400 dp width); the
// app's renders (400 x 860) sit under the 30 dp status bar, which shows the render's own top colour.
import React from 'react';
import { Img, staticFile } from 'remotion';
import { FONT } from '../brand/tokens';
import { H, W } from '../timeline';

export const SCREEN = { w: 400, h: 889, status: 30 };
/** Device proportions as fractions of its width (Nothing Phone (4a)). */
const DEV = { ratio: 164 / 77.6, bezel: 3.44 / 77.6, rim: 0.9 / 77.6, radius: 10.5 / 77.6, depth: 8.6 / 77.6 };

export type Pose = {
  x: number; // centre of the device on screen (px)
  y: number;
  w: number; // device width (px)
  rx?: number; // degrees: tilt back (+) / forward
  ry?: number; // turn: + shows the left edge
  rz?: number;
  z?: number; // px toward the camera
};

export const deviceSize = (w: number) => {
  const h = w * DEV.ratio;
  const bezel = w * DEV.bezel;
  const k = (w - 2 * bezel) / SCREEN.w; // px per dp
  return { w, h, bezel, k, radius: w * DEV.radius, depth: w * DEV.depth };
};

/** Screen px of a dp point on the screen of an untilted device at pose p (rotation ignored). */
export const onScreen = (p: Pose, x: number, y: number) => {
  const s = deviceSize(p.w);
  return { x: p.x - p.w / 2 + s.bezel + x * s.k, y: p.y - s.h / 2 + s.bezel + y * s.k };
};

export type Tone = 'light' | 'dark'; // the status bar's ink: light on blue and night screens

const FRAME = { face: '#1A1B1F', edge0: '#3A3C42', edge1: '#0C0D10', glass: '#050506' };

/** Android's status bar as Nothing OS draws it: the time left, signal, wifi and battery right. */
const StatusBar: React.FC<{ tone: Tone }> = ({ tone }) => {
  const ink = tone === 'light' ? '#FFFFFF' : '#0F1A3A';
  return (
    <div style={{ position: 'absolute', left: 0, top: 0, width: SCREEN.w, height: SCREEN.status, color: ink, fontFamily: '"Geist Variable", ' + FONT.text, fontSize: 13.5, fontWeight: 500, letterSpacing: '0.01em' }}>
      <div style={{ position: 'absolute', left: 26, top: 8, lineHeight: '16px', fontVariantNumeric: 'tabular-nums' }}>9:41</div>
      <svg width={64} height={14} viewBox="0 0 64 14" style={{ position: 'absolute', right: 22, top: 9 }}>
        {/* signal */}
        {[0, 1, 2, 3].map((i) => <rect key={i} x={i * 3.6} y={10 - i * 2.6} width={2.4} height={3 + i * 2.6} rx={0.7} fill={ink} />)}
        {/* wifi */}
        <path d="M24.5 4.6a9 9 0 0 1 12 0M26.6 7.1a5.8 5.8 0 0 1 7.8 0" fill="none" stroke={ink} strokeWidth={1.7} strokeLinecap="round" />
        <circle cx={30.5} cy={10.6} r={1.6} fill={ink} />
        {/* battery */}
        <rect x={42.5} y={2.5} width={17} height={9} rx={2.6} fill="none" stroke={ink} strokeOpacity={0.45} strokeWidth={1.1} />
        <rect x={44.2} y={4.2} width={11.2} height={5.6} rx={1.3} fill={ink} />
        <rect x={60.6} y={5.3} width={1.4} height={3.4} rx={0.7} fill={ink} fillOpacity={0.45} />
      </svg>
    </div>
  );
};

/**
 * The phone at pose `p`. Children are the screen in dp (400 x 889); the app's renders go under the
 * status bar via <AppFrame>. The device is lit from the top left; its shadow falls down and right.
 */
export const Device: React.FC<{ p: Pose; tone?: Tone; children: React.ReactNode; shadow?: number; style?: React.CSSProperties }> = ({ p, tone = 'light', children, shadow = 1, style }) => {
  const s = deviceSize(p.w);
  const { rx = 0, ry = 0, rz = 0, z = 0 } = p;
  const layers = 10;
  const screenR = (s.radius - s.bezel) / s.k;
  // the sheen slides across the glass as the device turns
  const sheen = 38 + ry * 1.6 - rx * 0.8;
  return (
    <div style={{ position: 'absolute', inset: 0, perspective: 2600, perspectiveOrigin: `${W / 2}px ${H / 2}px`, pointerEvents: 'none', ...style }}>
      <div
        style={{
          position: 'absolute',
          left: 0,
          top: 0,
          width: s.w,
          height: s.h,
          transformStyle: 'preserve-3d',
          transform: `translate3d(${p.x - s.w / 2}px, ${p.y - s.h / 2}px, ${z}px) rotateX(${rx}deg) rotateY(${ry}deg) rotateZ(${rz}deg)`,
        }}
      >
        {/* the shadow it casts, behind the body */}
        {shadow > 0 && (
          <div style={{ position: 'absolute', inset: 0, borderRadius: s.radius, transform: `translateZ(${-s.depth - 1}px)`, boxShadow: `${18 * shadow}px ${46 * shadow}px ${110 * shadow}px rgba(8,14,40,${0.34 * shadow}), ${4 * shadow}px ${12 * shadow}px ${28 * shadow}px rgba(8,14,40,${0.22 * shadow})` }} />
        )}
        {/* the frame's depth: stacked planes, darker toward the back */}
        {Array.from({ length: layers }, (_, i) => {
          const t = (i + 1) / layers;
          return (
            <div
              key={i}
              style={{
                position: 'absolute',
                inset: 0,
                borderRadius: s.radius,
                background: t < 0.5 ? FRAME.edge0 : FRAME.edge1,
                transform: `translateZ(${-s.depth * t}px)`,
                boxShadow: `inset 0 0 0 ${Math.max(1, s.w * 0.004)}px rgba(255,255,255,${0.1 * (1 - t)})`,
              }}
            >
              {/* power key on the right, volume on the left, proud of the frame */}
              <div style={{ position: 'absolute', right: -s.w * 0.008, top: s.h * 0.27, width: s.w * 0.012, height: s.h * 0.075, borderRadius: s.w, background: FRAME.edge0 }} />
              <div style={{ position: 'absolute', left: -s.w * 0.008, top: s.h * 0.2, width: s.w * 0.012, height: s.h * 0.13, borderRadius: s.w, background: FRAME.edge0 }} />
            </div>
          );
        })}
        {/* the front: a thin matte rim around the black glass */}
        <div style={{ position: 'absolute', inset: 0, borderRadius: s.radius, background: `linear-gradient(145deg, #4A4C53 0%, ${FRAME.face} 22%, ${FRAME.face} 70%, #2E3036 100%)` }} />
        <div style={{ position: 'absolute', inset: s.w * DEV.rim, borderRadius: s.radius - s.w * DEV.rim, background: FRAME.glass }} />
        {/* the panel */}
        <div style={{ position: 'absolute', left: s.bezel, top: s.bezel, width: SCREEN.w, height: SCREEN.h, transformOrigin: '0 0', transform: `scale(${s.k})`, borderRadius: screenR, overflow: 'hidden', background: '#000' }}>
          {children}
          <StatusBar tone={tone} />
          {/* the punch-hole camera, centred */}
          <div style={{ position: 'absolute', left: SCREEN.w / 2 - 6.5, top: 9, width: 13, height: 13, borderRadius: 7, background: '#020203', boxShadow: '0 0 0 1px rgba(255,255,255,0.06)' }}>
            <div style={{ position: 'absolute', left: 3.5, top: 3, width: 3, height: 3, borderRadius: 2, background: 'rgba(120,140,255,0.35)' }} />
          </div>
          {/* the gesture bar */}
          <div style={{ position: 'absolute', left: SCREEN.w / 2 - 54, top: SCREEN.h - 11, width: 108, height: 4, borderRadius: 2, background: tone === 'light' ? 'rgba(255,255,255,0.85)' : 'rgba(15,26,58,0.7)' }} />
        </div>
        {/* the glass: a soft reflection that moves with the turn, and its edge catching the light */}
        <div style={{ position: 'absolute', left: s.bezel * 0.4, top: s.bezel * 0.4, right: s.bezel * 0.4, bottom: s.bezel * 0.4, borderRadius: s.radius - s.bezel * 0.4, background: `linear-gradient(118deg, rgba(255,255,255,0) ${sheen - 16}%, rgba(255,255,255,0.075) ${sheen}%, rgba(255,255,255,0) ${sheen + 14}%)`, boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.10)' }} />
      </div>
    </div>
  );
};

/**
 * An app render placed under the status bar, with the 30 dp above it filled by the render's own
 * top row (every screen's top edge is its plain background), so the screen runs edge to edge.
 */
export const AppFrame: React.FC<{ src: string; children?: React.ReactNode }> = ({ src, children }) => (
  <>
    <div style={{ position: 'absolute', left: 0, top: 0, width: SCREEN.w, height: SCREEN.status + 1, backgroundImage: `url(${staticFile(src)})`, backgroundSize: `${SCREEN.w}px ${860 * 60}px`, backgroundPosition: '0 0' }} />
    <div style={{ position: 'absolute', left: 0, top: SCREEN.status, width: SCREEN.w, height: 860 }}>{children ?? <Img src={staticFile(src)} style={{ width: SCREEN.w, height: 860, display: 'block' }} />}</div>
  </>
);

/** The status bar ink each screen needs. */
export const TONE: Record<string, Tone> = {
  today: 'light', feed_0: 'light', feed_1: 'light', feed_2: 'light', feed_3: 'light', play_front: 'light', play_back: 'light',
  compose_write: 'light', compose_tuck: 'light', compose_when: 'light', letter_sealed: 'light', letter_opened: 'light', capsule: 'light',
  studio: 'dark', word_sonder: 'light', onboarding_hello: 'dark',
};
