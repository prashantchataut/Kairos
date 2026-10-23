// Brand tokens: the only place a colour, face or type size is written down. Acts import these;
// scripts/lint-film.mjs fails any hex colour that is not declared here.
//
// cinetic:brand-supplied coral, serif, handwriting, gradient, lilac: Kairos' own palette and faces
// (app/src/main/java/com/kairos/app/ui/theme/Tokens.kt and the kairos-playful-sky skill), recorded
// in BRIEF.md. The brand wins over the starter's defaults.
import type React from 'react';
import { safeInsets } from '../lib/safe';
import { H, W } from '../timeline';

/**
 * Colours sampled off the app's own renders (public/app), for plates that sit flush with a render:
 * where the film covers part of a screen, the ground has to be the screen's own pixels.
 */
export const SAMPLED = {
  keepLabel: '#622610', // feed_0's wash behind the rail's "Keep" label (Kept is set over it)
  play0: '#1E2C69', // play_front's wash at the card's top edge ...
  play1: '#243279',
  play2: '#2C3D8D',
  play3: '#3B4BA0',
  play4: '#4E5DB8', // ... to its bottom edge: the plate the card turns over
  write0: '#2149CC', // compose_write's wash behind its title, left ...
  write1: '#244DD0',
  write2: '#2A54D8', // ... to right: the plate the title types itself on
};

/** The phone's materials (kit/Device.tsx): a Nothing Phone (4a) in black, matte frame, black glass. */
export const DEVICE = {
  face: '#1A1B1F', // the frame, front face
  edge0: '#3A3C42', // the frame's lit side
  edge1: '#0C0D10', // the frame's back half
  glass: '#050506', // the black glass around the panel
  rimLit: '#4A4C53', // the rim catching the key light, top left
  rimLow: '#2E3036', // the rim, bottom right
  lens: '#020203', // the punch-hole camera
  lensGlint: '#5A6E9A', // its coating, catching light
};

export const C = {
  ink: '#0F1A3A', // type on light stages: Kairos navy ink
  inkSoft: '#55607A', // secondary text on light
  inkFaint: '#9AA3B8', // hairlines, ghost labels
  canvas: '#F6F8FE', // the light stage: page ground in the app
  surface: '#FFFFFF', // cards
  surfaceSoft: '#EEF2FB', // soft panels, chips
  line: '#DCE3F2', // hairlines on light
  white: '#FFFFFF',
  // The brand stage. Cobalt is the one action colour of the app; the sky runs top to bottom.
  cobalt: '#2F5BFF',
  skyTop: '#3D7BFA',
  skyMid: '#8EB9FF',
  skyFade: '#E9F2FF',
  night1: '#1D4BE0', // the time capsule's night, top
  night2: '#0B1440',
  night3: '#05071A',
  navy: '#141C31', // dark surfaces
  // The one accent and its meaning: "the moment". The hand that stops, the petal that is yours.
  coral: '#FF6A4D',
  // The launcher icon's sky (ic_launcher_background.xml), top to bottom.
  iconTop: '#4A7DFF',
  iconBottom: '#2346D8',
  coralInk: '#C7381F', // coral text on light (handwritten loop)
  // The candy set: product UI only (flashcards, share cards, Kai's moods), never on film copy.
  sun: '#FFC83D',
  mint: '#2ECF8B',
  skyCandy: '#4DA3FF',
  bubblegum: '#FF79B5',
  lilac: '#A98CFF',
  letter: '#FFF8EC', // the letter paper in the app's time capsule
  letterInk: '#2A2140', // ink on the letter
  // Starter roles, mapped onto the brand (fx/, Stills and the audit read them).
  accent: '#FF6A4D', // = coral
  paper: '#F6F8FE', // = canvas
  mist: '#EEF2FB', // = surfaceSoft
  mute: '#55607A', // = inkSoft
  ink2: '#141C31', // = navy
};

export const FONT = {
  text: '"Bricolage", system-ui, sans-serif', // display + interface: the app's own grotesque
  word: '"Fraunces", Georgia', // the words: Kairos sets every word of the day in Fraunces
  hand: '"Caveat", cursive', // the handwriting of a letter to future you
  mono: '"Bricolage", system-ui, sans-serif', // no mono in this brand; numbers use tabular figures
};

/** Faces the FontGate waits for before anything renders or measures (one entry per weight in use). */
export const FACES = ['400 16px "Bricolage"', '600 16px "Bricolage"', '800 16px "Bricolage"', '400 16px "Fraunces"', '600 16px "Fraunces"', '500 16px "Caveat"'];

/** Layout unit: 1 at 1080p, scales every size with the short side of the frame (16:9, 9:16, 1:1). */
export const U = Math.min(W, H) / 1080;

// One token per role, reused everywhere, so nothing is "almost matched" (size in px at 1080p).
export const TYPE = {
  display: { size: 120, weight: 800, track: -0.04, lead: 0.98 }, // statements: Bricolage ExtraBold, as the app's display
  line: { size: 84, weight: 700, track: -0.035, lead: 1.02 }, // fragment chains
  secondary: { size: 44, weight: 500, track: -0.015, lead: 1.15 }, // the end-card line
  ui: { size: 24, weight: 500, track: -0.01, lead: 1.3 }, // smallest readable size on screen
};

/** Fraunces settings: the app's word face (opsz 144, SOFT 100, WONK 1). */
export const WORD_FACE: React.CSSProperties = { fontFamily: FONT.word, fontVariationSettings: '"opsz" 144, "SOFT" 100, "WONK" 1' };

/** CSS for a type role at a size (px). Tabular figures always, so numbers never jitter in width. */
export const typeStyle = (role: keyof typeof TYPE, size = TYPE[role].size * U): React.CSSProperties => ({
  fontFamily: FONT.text,
  fontSize: size,
  fontWeight: TYPE[role].weight,
  letterSpacing: `${TYPE[role].track}em`,
  lineHeight: TYPE[role].lead,
  fontVariantNumeric: 'tabular-nums',
  fontVariationSettings: '"wdth" 100, "opsz" 96',
  whiteSpace: 'pre',
});

/**
 * Safe margins for a w x h frame as a symmetric envelope: the larger inset of each pair from the
 * frame's safe-zone preset (src/lib/safe.ts).
 */
export const safeArea = (w: number, h: number) => {
  const i = safeInsets(undefined, w, h);
  return { x: Math.max(i.left, i.right), y: Math.max(i.top, i.bottom) };
};
/** The film's safe margins; text stays inside them (scripts/layout-audit.sh checks). */
export const SAFE = safeArea(W, H);
