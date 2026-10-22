// The product's own content, in one place, with asserts: every string here is something the app
// really shows (the curated word list, the Feed's kinds, the time capsule's copy). Nothing is a
// fake metric. Acts read from here, never from string literals.

export type Word = { word: string; say: string; pos: string; means: string };

export const WORDS: Record<string, Word> = {
  saudade: { word: 'Saudade', say: 'sow-DAH-djee', pos: 'noun', means: 'A tender longing for something or someone you love and have lost.' },
  apricity: { word: 'Apricity', say: 'uh-PRIS-ih-tee', pos: 'noun', means: 'The warmth of the sun on a cold winter day.' },
  hiraeth: { word: 'Hiraeth', say: 'HEER-eyeth', pos: 'noun', means: 'A longing for a home you cannot go back to.' },
  petrichor: { word: 'Petrichor', say: 'PET-ri-kor', pos: 'noun', means: 'The smell of rain on dry earth.' },
  komorebi: { word: 'Komorebi', say: 'koh-moh-REH-bee', pos: 'noun', means: 'Sunlight that filters through the leaves.' },
  sonder: { word: 'Sonder', say: 'SON-der', pos: 'noun', means: 'The feeling that every stranger has a life as full as yours.' },
  akrasia: { word: 'Akrasia', say: 'uh-KRAY-zhuh', pos: 'noun', means: 'Acting against your better judgement.' },
  meraki: { word: 'Meraki', say: 'may-RAH-kee', pos: 'noun', means: 'Putting a piece of yourself into what you make.' },
};

/** The word of the day rewrites in place: Saudade -> Apricity -> Hiraeth (Act 3). */
export const TODAY = [WORDS.saudade, WORDS.apricity, WORDS.hiraeth];

export type FeedItem =
  | { kind: 'word'; w: Word; tint: 'cobalt' | 'sky' | 'mint' | 'night' }
  | { kind: 'thought'; text: string; tint: 'cobalt' | 'sky' | 'mint' | 'night' }
  | { kind: 'book'; title: string; author: string; gist: string; tint: 'cobalt' | 'sky' | 'mint' | 'night' };

/**
 * The reel, top to bottom. Card 1 is kept (Apricity, a quiet-weather word); after that the Feed
 * follows what was kept: Petrichor and Komorebi are weather words too, and the app says so in its
 * own chip ("You keep saving quiet weather words.").
 */
export const FEED: FeedItem[] = [
  { kind: 'thought', text: 'You are allowed to\nbe a beginner again.', tint: 'night' },
  { kind: 'word', w: WORDS.apricity, tint: 'cobalt' },
  { kind: 'word', w: WORDS.petrichor, tint: 'sky' },
  { kind: 'word', w: WORDS.komorebi, tint: 'mint' },
];
export const FEED_KEPT = 1; // index of the card that gets kept
export const FEED_CHIP = 'You keep saving quiet weather words.';

/** Play: flashcards made of the words this person kept (candy colours from the app's Play deck). */
export const FLASH = [
  { w: WORDS.apricity, hue: 'cobalt' as const },
  { w: WORDS.petrichor, hue: 'mint' as const },
  { w: WORDS.sonder, hue: 'coral' as const },
  { w: WORDS.komorebi, hue: 'sun' as const },
  { w: WORDS.meraki, hue: 'skyCandy' as const },
];

export const LETTER = {
  greeting: 'Dear future me,',
  body: ['I hope the new job turned', 'out kinder than you feared.', 'Whatever happened, I am', 'proud of you for trying.'],
  sign: '— you, October 2026',
  opens: 'Opens in 6 months',
};

export const SHARE = {
  pin: WORDS.saudade,
  story: { text: 'Some days the bravest\nthing is to stay soft.' },
  square: WORDS.komorebi,
};

// --- asserts: the data must agree with the story -------------------------------------------
const assert = (ok: boolean, msg: string) => {
  if (!ok) throw new Error(`data.ts: ${msg}`);
};
assert(TODAY.length === 3 && new Set(TODAY.map((w) => w.word)).size === 3, 'three different words of the day');
assert(FEED[FEED_KEPT].kind === 'word', 'the kept card is a word');
assert(FLASH.every((f) => Object.values(WORDS).includes(f.w)), 'flashcards are words the app knows');
for (const w of Object.values(WORDS)) assert(!/written for you/i.test(w.means), 'never "written for you"');
