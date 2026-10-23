// The product's own content, in one place, with asserts: every string here is something the app
// really shows (the curated word list; the rest of the film's product content is the app's own
// renders). Nothing is a fake metric. Acts read from here, never from string literals.

export type Word = { word: string; say: string; pos: string; means: string };

export const WORDS: Record<string, Word> = {
  saudade: { word: 'Saudade', say: 'sow-DAH-djee', pos: 'noun', means: 'A tender longing for something or someone you love and have lost.' },
  apricity: { word: 'Apricity', say: 'uh-PRIS-ih-tee', pos: 'noun', means: 'The warmth of the sun on a cold winter day.' },
  hiraeth: { word: 'Hiraeth', say: 'HEER-eyeth', pos: 'noun', means: 'A longing for a home you cannot go back to.' },
};

/** The word of the day rewrites in place: Saudade -> Apricity -> Hiraeth (Act 3). */
export const TODAY = [WORDS.saudade, WORDS.apricity, WORDS.hiraeth];

/** The Feed page that gets kept (src/acts/Act4.tsx FEED_PAGES[1]: Apricity, feed_0). */
export const FEED_KEPT = 1;

// --- asserts: the data must agree with the story -------------------------------------------
const assert = (ok: boolean, msg: string) => {
  if (!ok) throw new Error(`data.ts: ${msg}`);
};
assert(TODAY.length === 3 && new Set(TODAY.map((w) => w.word)).size === 3, 'three different words of the day');
for (const w of Object.values(WORDS)) assert(!/written for you/i.test(w.means), 'never "written for you"');
