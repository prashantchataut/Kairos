// cinetic:brand-supplied coral, serif, gradient: every pixel here is the app's own (BRIEF.md).
//
// The film's app kit, built on the app's real renders (public/app, made by FilmAssetsTest.kt with
// Roborazzi and imported by scripts/import-app-assets.py): the screens, Kai's frame sequences, the
// bloom unfolding, the mark, the wordmark and the studio's share cards. Nothing here redraws the
// app; the few live overlays (a word rewriting, a heart filling, Kai blinking) are set in the app's
// own faces and colours and placed in app units measured off the renders.
import React from 'react';
import { Img, staticFile } from 'remotion';
import { FONT } from '../brand/tokens';
import { SHADOW } from './Kit';
import type { Word } from './data';

/** A screen render: 400 x 860 dp at 2.5 px per dp (1000 x 2150). Overlays use these app units. */
export const SHOT = { w: 400, h: 860, px: 2.5 };

export type ScreenName =
  | 'today' | 'feed_0' | 'feed_1' | 'feed_2' | 'feed_3' | 'play_front' | 'play_back' | 'word_sonder'
  | 'compose_write' | 'compose_tuck' | 'compose_when' | 'letter_sealed' | 'letter_opened' | 'capsule' | 'studio' | 'onboarding_hello';

const src = (name: string) => staticFile(`app/${name}`);

/** A whole screen, filling the phone's 400 x 860 canvas. */
export const Shot: React.FC<{ name: ScreenName; style?: React.CSSProperties }> = ({ name, style }) => (
  <Img src={src(`${name}.jpg`)} style={{ position: 'absolute', left: 0, top: 0, width: SHOT.w, height: SHOT.h, display: 'block', ...style }} />
);

/**
 * A rectangle of a screen (app units), drawn at its own place: for moving one part of a screen (a
 * card in the reel, a button that changes state) while the rest stays still.
 */
export const ShotPart: React.FC<{ name: ScreenName; x: number; y: number; w: number; h: number; radius?: number; style?: React.CSSProperties }> = ({ name, x, y, w, h, radius = 0, style }) => (
  <div style={{ position: 'absolute', left: x, top: y, width: w, height: h, overflow: 'hidden', borderRadius: radius, ...style }}>
    <Img src={src(`${name}.jpg`)} style={{ position: 'absolute', left: -x, top: -y, width: SHOT.w, height: SHOT.h, display: 'block' }} />
  </div>
);

// ------------------------------------------------------------------------------------------
// Kai: drawKai's own frames, rendered on a 220 dp square (440 px). drawKai fills its canvas the
// same way at any size, so a Kai the app drew at d dp is this sprite drawn d units wide around the
// same centre (the header Kai: 56 dp, so the 220 dp frames scale by 56 / 220).

export const KAI_SEQ = { blink: 9, breathe: 16, delight: 12, wow: 12 } as const;
export type KaiSeq = keyof typeof KAI_SEQ;
export type KaiMood = 'curator' | 'radiant' | 'good' | 'okay' | 'low' | 'thinking';

const pad2 = (n: number) => String(n).padStart(2, '0');

/**
 * Kai from the app's frames. `seq` + `t` (0..1) plays a sequence; `mood` shows a still. `size` is
 * the drawn Kai's size in px (the sprite canvas is drawn at exactly that size: drawKai fills its
 * canvas, so a Kai drawn at 56 dp in the app is this sprite at 56 units).
 */
export const KaiArt: React.FC<{ size: number; seq?: KaiSeq; t?: number; mood?: KaiMood; style?: React.CSSProperties }> = ({ size, seq, t = 0, mood = 'curator', style }) => {
  const file = seq ? `kai_${seq}_${pad2(Math.round(Math.min(1, Math.max(0, t)) * (KAI_SEQ[seq] - 1)))}.png` : `kai_${mood}.png`;
  return <Img src={src(file)} style={{ width: size, height: size, display: 'block', ...style }} />;
};

/** A blink as the sequence parameter: 0 open .. closed at 0.5 .. open at 1, over `dur` frames. */
export const blinkT = (f: number, at: number, dur = 14) => (f < at || f > at + dur ? 0 : (f - at) / dur);

/** Kai breathing, looping every `period` frames (the app breathes Kai slowly). */
export const breatheT = (f: number, period = 150) => (((f % period) + period) % period) / period;

// ------------------------------------------------------------------------------------------
// The mark: drawBloom's own frames (unfolding, 40 frames on a 300 dp square) and the still mark.

export const BLOOM_FRAMES = 40;
export type MarkInk = 'white' | 'ink';

/** The bloom unfolding as the app draws it, petal by petal from the moment petal; t 0..1. */
export const BloomArt: React.FC<{ size: number; ink?: MarkInk; t?: number; style?: React.CSSProperties }> = ({ size, ink = 'white', t = 1, style }) => {
  const i = Math.round(Math.min(1, Math.max(0, t)) * (BLOOM_FRAMES - 1));
  return <Img src={src(`bloom_${ink}_${pad2(i)}.png`)} style={{ width: size, height: size, display: 'block', ...style }} />;
};

/** The finished mark (drawMark). The bloom's radius is 0.46 of the unfold canvas, 0.5 of this one. */
export const MarkArt: React.FC<{ size: number; ink?: MarkInk; style?: React.CSSProperties }> = ({ size, ink = 'white', style }) => (
  <Img src={src(`mark_${ink}.png`)} style={{ width: size, height: size, display: 'block', ...style }} />
);

/** The wordmark exactly as the app sets it (Wordmark: the mark, 7 dp, "Kairos" in Fraunces). */
export const WORDMARK = { w: 1109, h: 240 };
export const WordmarkArt: React.FC<{ h: number; ink?: MarkInk; style?: React.CSSProperties }> = ({ h, ink = 'white', style }) => (
  <Img src={src(`wordmark_${ink}.png`)} style={{ width: (h * WORDMARK.w) / WORDMARK.h, height: h, display: 'block', ...style }} />
);

// ------------------------------------------------------------------------------------------
// Share cards: the studio's own templates (CardThumbnail) at 1080 px wide.

export type CardShape = 'pin' | 'story' | 'square';
export const CARD_RATIO: Record<CardShape, number> = { pin: 2 / 3, story: 9 / 16, square: 1 };
export const CARD_DESIGNS = ['bloom_sky', 'bold_sky', 'glow_peach', 'eclipse_ink', 'garden_lavender', 'poster_mint', 'stage_mint', 'window_sky', 'blurbloom_sky', 'starry_ink', 'breathe_peach', 'swiss_sky'] as const;
export type CardDesign = (typeof CARD_DESIGNS)[number];

/** A share card at height h px, rounded like the studio's preview, with a soft shadow. */
export const CardArt: React.FC<{ shape: CardShape; design: CardDesign; h: number; shadow?: boolean; style?: React.CSSProperties }> = ({ shape, design, h, shadow = true, style }) => (
  <div style={{ position: 'relative', width: h * CARD_RATIO[shape], height: h, borderRadius: h * 0.035, overflow: 'hidden', boxShadow: shadow ? SHADOW : undefined, ...style }}>
    <Img src={src(`card_${shape}_${design}.jpg`)} style={{ width: '100%', height: '100%', display: 'block' }} />
  </div>
);

// ------------------------------------------------------------------------------------------
// Live overlays on the Today screen (app units, measured on today.jpg).

/** The word card's text block on today.jpg, and the colours sampled off the render. */
export const TODAY = {
  // the patch: from under the "Word of the day" row to above Kai's reason line, card-inner width
  patch: { x: 36, y: 300, w: 328, h: 210 },
  card: '#FFFFFF',
  ink: '#0F1A3A',
  inkSoft: '#55607A',
  chip: '#EEF2FB',
  accent: '#2F5BFF',
  textX: 44.4, // the card's inner left edge
  wordBase: 376, // the word's line box bottom (lineHeight 1), measured against the render
  chipY: 391.2, // the pronunciation chip's top (it is 28.8 tall)
  defY: 434, // the definition's first line box top
  heart: { x: 177.2, y: 635.2, size: 22 }, // the Save heart's centre and size (dp)
  pill: { x: 90.4, y: 606.4, w: 219.2, h: 57.6 }, // the Save pill
  kai: { x: 360, y: 36, size: 56 }, // the header Kai's centre and size
};

const SPEAKER = ['M5 10v4h3l4.5 3.5v-11L8 10z', 'M15.6 9.6a3.4 3.4 0 0 1 0 4.8', 'M17.9 7.3a6.6 6.6 0 0 1 0 9.4'];
export const HEART_PATH = 'M12 19.4s-7-4.3-7-9.5C5 7.4 6.9 5.6 9.2 5.6c1.3 0 2.3.6 2.8 1.6.5-1 1.5-1.6 2.8-1.6 2.3 0 4.2 1.8 4.2 4.3 0 5.2-7 9.5-7 9.5z';

/**
 * The word card's word, pronunciation chip, part of speech and definition, set exactly as
 * TodayScreen.WordCard sets them, over a patch of the card's white. `word` is the text drawn
 * (it may be mid-rewrite); `info` the details; `infoFade` 0..1 hides the details while they change.
 */
export const TodayWordPatch: React.FC<{ word: React.ReactNode; info: Word; infoFade?: number }> = ({ word, info, infoFade = 0 }) => {
  const T = TODAY;
  const sans = (size: number, weight: number, color: string): React.CSSProperties => ({ fontFamily: FONT.text, fontSize: size, fontWeight: weight, color, fontVariationSettings: '"wdth" 100, "opsz" 16', whiteSpace: 'pre' });
  return (
    <div style={{ position: 'absolute', left: T.patch.x, top: T.patch.y, width: T.patch.w, height: T.patch.h, background: T.card }}>
      <div style={{ position: 'absolute', left: T.textX - T.patch.x - 1.2, top: T.wordBase - T.patch.y, transform: 'translateY(-100%)' }}>
        {word}
      </div>
      <div style={{ position: 'absolute', left: T.textX - T.patch.x, top: T.chipY - T.patch.y, opacity: 1 - infoFade, display: 'flex', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', height: 28.8, padding: '0 10px 0 12px', borderRadius: 14.4, background: T.chip }}>
          <span style={{ ...sans(14, 500, T.ink), lineHeight: '20px', position: 'relative', top: 1.2 }}>{info.say}</span>
          <svg width={17} height={17} viewBox="0 0 24 24" style={{ marginLeft: 6 }}>
            {SPEAKER.map((d) => (
              <path key={d} d={d} fill="none" stroke={T.accent} strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" />
            ))}
          </svg>
        </div>
        <span style={{ marginLeft: 4.4, fontFamily: FONT.word, fontStyle: 'italic', fontSize: 16, color: T.inkSoft, fontVariationSettings: '"opsz" 72, "SOFT" 100, "WONK" 1' }}>{info.pos}</span>
      </div>
      <div style={{ position: 'absolute', left: T.textX - T.patch.x, top: T.defY - T.patch.y, width: 312, opacity: 1 - infoFade, ...sans(19, 400, T.ink), whiteSpace: 'normal', lineHeight: '27px' }}>{info.means}</div>
    </div>
  );
};

/** The word of the day as WordCard sets it: Fraunces 400 at opsz 144, 64 sp, -0.03 em. */
export const todayWordStyle: React.CSSProperties = {
  fontFamily: FONT.word,
  fontWeight: 400,
  fontSize: 64,
  lineHeight: 1,
  letterSpacing: '-0.03em',
  color: TODAY.ink,
  whiteSpace: 'pre',
  fontVariationSettings: '"opsz" 144, "SOFT" 100, "WONK" 1',
};

/** The Save pill switching to Saved: HeartOn in coral, the label in SemiBold 16. */
export const SavedPill: React.FC<{ on: number }> = ({ on }) => {
  const P = TODAY.pill;
  const s = TODAY.heart.size;
  return (
    <div style={{ position: 'absolute', left: P.x, top: P.y, width: P.w, height: P.h, borderRadius: P.h / 2, background: '#FFFFFF', opacity: on, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
      <svg width={s} height={s} viewBox="0 0 24 24" style={{ transform: `scale(${0.4 + 0.6 * on})` }}>
        <path d={HEART_PATH} fill="#FF6A4D" />
      </svg>
      <span style={{ fontFamily: FONT.text, fontSize: 16, fontWeight: 600, color: TODAY.ink, fontVariationSettings: '"wdth" 100, "opsz" 16', lineHeight: '20px' }}>Saved</span>
    </div>
  );
};
