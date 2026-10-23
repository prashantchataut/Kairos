// The single source of truth for time. Picture (src/acts), sound (src/sync.ts -> out/cues.json)
// and QA (scripts/grid-check.ts) all import this file, so a change here moves everything together.
// Think in bars and beats, never in seconds. Keep this file free of React and CSS imports:
// Node scripts load it directly.
import { holdFor, PACE, readingWindows, revealFor, wordCount, type PaceName } from './lib/pace';

export { PACE } from './lib/pace'; // grid-check checks COPY against the film's own pace numbers

export const FPS = 60;
export const BPM = 120;
export const W = 1920;
export const H = 1080;
/**
 * The film's pace (src/lib/pace.ts). calm: text arrives as short fragments that take turns with
 * the picture, held about 1-1.5 s (the default for text-led films, at 90-100 BPM). crisp: the
 * denser launch-film tempo, for briefs that ask for energy (at 120 BPM).
 */
export const PACE_NAME: PaceName = 'crisp';

/** Frames per beat. Must be a whole number (grid-check fails otherwise): 60 fps -> 90, 100, 120, 144, 150 BPM. */
export const BEAT = (60 / BPM) * FPS;
export const BAR = BEAT * 4;
/**
 * Absolute frame of a grid position: bar is 1-based, beat 0-3, sub = 16ths within the beat.
 * b(3) is the downbeat of bar 3; b(2, 1, 2) is the "and" of beat 2 in bar 2.
 */
export const b = (bar: number, beat = 0, sub = 0) => Math.round(((bar - 1) * 4 + beat) * BEAT + sub * (BEAT / 4));

/**
 * A choreography length written in 60 fps frames (the unit every number in the cinetic
 * references uses), converted to this film's fps. Cues come from b(); durations inside a move
 * use f60(), so the film keeps its timing if --fps changes.
 */
export const f60 = (frames: number) => Math.round((frames * FPS) / 60);

/** After the last bar the picture holds while the audio decays to digital silence. */
export const TAIL = FPS;

// Acts: contiguous, bar-aligned, [from, from + dur). Each act is its own composition in Root.tsx.
// The story device (the coral moment petal) crosses every seam: TREATMENT.md "Device path".
export const ACT = {
  chronos: { from: b(1), dur: b(4) - b(1) }, // a clock of days whirs; one hand stops and turns coral
  name: { from: b(4), dur: b(6) - b(4) }, // the coral hand floods the frame, then becomes the dot of the i in "Kairos"
  word: { from: b(6), dur: b(9) - b(6) }, // the phone rises; the word of the day rewrites in place; dive in
  feed: { from: b(9), dur: b(11) - b(9) }, // the reel: a card is kept, the next ones follow it
  play: { from: b(11), dur: b(13) - b(11) }, // flashcards of your own words fan out and turn
  letter: { from: b(13), dur: b(16) - b(13) }, // night rises; a letter to future you is folded and sealed
  share: { from: b(16), dur: b(18) - b(16) }, // the envelope becomes a share card: pin, story, square
  lockup: { from: b(18), dur: b(20) + TAIL - b(18) }, // the cards scatter; the bloom unfolds around the petal
};

export const TOTAL = b(20) + TAIL;

// Named sync points (absolute frames), all on the 16th grid. Picture code reads these; src/sync.ts
// turns them into sound events.
export const CUE = {
  // 1 chronos
  whir: b(1, 1, 2), // the hands speed up between the first two fragments
  blurUp: b(2), // a turn of the dial past full speed
  stop: b(3, 0, 3), // one hand stops dead at one o'clock and turns coral (contact)
  pushIn: b(4), // the camera starts into the coral hand
  // 2 name
  flood: b(4, 0, 3), // the coral petal fills the frame (the film's one accent flood)
  letters: b(4, 1, 2), // the flood shrinks back as "Kairos" rises letter by letter (the one hero word)
  dot: b(4, 2, 1), // the petal lands as the dot of the i
  // 3 word
  rise: b(6), // the phone rises already moving
  heart: b(6, 0, 2), // the petal lands on the card as its Save heart
  rewrite1: b(7), // Saudade -> Apricity, rewritten in place
  rewrite2: b(8, 0, 2), // Apricity -> Hiraeth
  dive: b(8, 2), // isolating push into the card
  through: b(8, 3, 1), // the card fills the frame and becomes the Feed
  // 4 feed
  flick1: b(9), // the reel flicks to a new card
  keep: b(9, 3, 1), // the Keep heart fills coral
  flick2: b(10, 3, 1), // the next card matches what was kept
  // 5 play
  fan: b(11), // the phone falls away, flashcards fan out
  turn1: b(11, 3, 2), // one card turns over
  turn2: b(12, 3, 1), // another card turns over
  // 6 letter
  night: b(13), // night columns rise behind
  paper: b(13, 1), // the letter slides in, already being written
  fold: b(14, 0, 3), // the letter folds into its envelope
  seal: b(14, 1, 2), // the coral seal lands (landing 1)
  opens: b(15, 1), // "Opens in 6 months" rolls in
  flip: b(15, 3, 2), // the envelope flips and becomes a share card
  // 7 share
  story: b(16, 3, 2), // pin -> story
  square: b(17, 3, 1), // story -> square, the three shapes side by side
  scatter: b(18), // the cards fly off, leaving the petal at the centre
  // 8 lockup
  unfold: b(18, 0, 2), // seven petals unfold around the coral one
  core: b(18, 2), // the core lands (landing 2)
  lockup: b(19), // the wordmark is readable: the payoff
  end: b(20), // last bar line; TAIL follows
};

export type Copy = {
  id: string;
  text: string; // exactly what is on screen (\n separates lines)
  in: number; // first frame any of it is visible
  resolved: number; // first frame it is fully readable
  out: number; // first frame it starts to leave (TOTAL if it stays)
  kind?: 'copy' | 'ui'; // 'ui': a string the product shows (a typed command, code, a URL, an app label); not budgeted
  chain?: string; // the thought this fragment belongs to (fx/Chain renders a chain; grid-check checks its gaps)
  mode?: 'build' | 'replace'; // build: fragments append to one held line; replace: each takes the last one's place
};

const pace = PACE[PACE_NAME];

/**
 * One line of copy revealed word by word with this film's pace (the numbers <Words> uses), so
 * `resolved` is the frame its last word is fully in. `out` defaults to the pace's hold.
 */
export const line = (id: string, text: string, inF: number, out?: number): Copy => {
  const n = wordCount(text);
  const resolved = inF + f60(revealFor(n, pace));
  return { id, text, in: inF, resolved, out: out ?? resolved + f60(holdFor(n, pace)) };
};

/**
 * One thought as a chain of fragments, one COPY entry each (`<id>.1`, `<id>.2`, ...), rendered by
 * fx/Chain. A thought longer than 4 words arrives this way: 1-4 words per fragment, never a whole
 * sentence at once, with a picture beat between fragments (the product does what the fragment set
 * up). Give each fragment its first frame; `resolved` follows from the pace.
 *   build    fragments append to one held line, which re-centres; the line clears as a whole, at
 *            `out` (default: the last fragment's resolve plus the hold of the full line)
 *   replace  each fragment leaves (as a whole, over pace.exitDur: 24 f when calm) and has gone
 *            6 f before the next one rises in its place, so two fragments never share the spot;
 *            the last one leaves at `out` (default: its resolve plus its hold)
 * grid-check checks the result: every fragment is read before the next arrives, the picture beat
 * between them, the words on screen at once and the copy rate.
 */
export const chain = (id: string, mode: 'build' | 'replace', fragments: [text: string, inF: number][], o: { out?: number } = {}): Copy[] => {
  const base = fragments.map(([text, inF], i) => ({ id: `${id}.${i + 1}`, text, in: inF, resolved: inF + f60(revealFor(wordCount(text), pace)), chain: id, mode }));
  const last = base[base.length - 1];
  if (!last) throw new Error(`timeline: chain "${id}" has no fragments`);
  const words = (list: { text: string }[]) => list.reduce((n, c) => n + wordCount(c.text), 0);
  if (mode === 'build') {
    const out = o.out ?? last.resolved + f60(holdFor(words(base), pace));
    return base.map((c) => ({ ...c, out }));
  }
  // replace: the outgoing fragment's exit (exitDur) is over 6 f before the next one starts to rise
  const leave = f60(pace.exitDur) + f60(6);
  return base.map((c, i) => ({ ...c, out: base[i + 1] ? base[i + 1].in - leave : (o.out ?? c.resolved + f60(holdFor(wordCount(c.text), pace))) }));
};

// Every on-screen word: the word budget (kind: 'ui' entries are counted apart), the hold check,
// and the text-flow checks (fragments, words on screen at once, copy rate, text share) read this.
export const COPY: Copy[] = [
  // "Days" "just pass." builds; then "Some moments" -> (a hand stops dead) -> "stay." replaces it.
  ...chain('days', 'build', [['Days', b(1, 0, 1)], ['just pass.', b(1, 2, 3)]]),
  ...chain('stay', 'replace', [['Some moments', b(2, 2)], ['stay.', b(3, 1, 2)]]),
  // The hero word, then what it means.
  ...chain('name', 'build', [['Kairos', CUE.letters], ['the right moment.', b(5)]]),
  // "A word" [the phone, the word rewrites] "for every feeling."
  ...chain('word', 'build', [['A word', b(6, 1)], ['for every feeling.', b(7, 1)]]),
  // "It learns" [a card is kept] "what you keep."
  ...chain('learns', 'build', [['It learns', b(9, 0, 2)], ['what you keep.', b(10)]]),
  ...chain('play', 'build', [['Play', b(11, 1)], ['your words.', b(12)]]),
  // "Write to" [the letter folds, the seal lands] "future you."
  ...chain('write', 'build', [['Write to', b(13, 2)], ['future you.', b(14, 2)]]),
  ...chain('share', 'build', [['Share', b(16, 1)], ['what stays.', b(17)]]),
  { id: 'wordmark', text: 'Kairos', in: b(18, 3), resolved: CUE.lockup, out: TOTAL },
  { ...line('android', 'Now on Android', b(19, 2)), out: TOTAL },
  // Product strings (kind 'ui'): the app's own content, not film copy.
  { id: 'ui.dear', text: 'Dear future me...', in: CUE.paper, resolved: CUE.paper + 32, out: CUE.fold, kind: 'ui' },
  { id: 'ui.opens', text: 'Opens on April 1st 2027', in: CUE.opens, resolved: CUE.opens + 26, out: CUE.flip, kind: 'ui' },
];

/**
 * When each COPY entry is being read: [from, to) frame ranges by id (src/lib/pace.ts
 * readingWindows(): from `resolved` for at most readMax, an earlier fragment of a build chain only
 * for its own hold, then the whole line again). The one rule every check uses: export-cues.ts
 * writes it into out/cues.json, and grid-check, layout-audit, measure-speed and forensics read it.
 * Nothing new should start inside a window: the read happens in the quiet.
 */
export const READ = readingWindows(COPY, pace, FPS, TOTAL);

/** One COPY entry by id; acts read their text and timing from here so the budget stays true. */
export const copy = (id: string): Copy => {
  const c = COPY.find((x) => x.id === id);
  if (!c) throw new Error(`timeline: no COPY entry "${id}"`);
  return c;
};

/** The fragments of one chain, in order of arrival. */
export const chainOf = (id: string): Copy[] => {
  const list = COPY.filter((x) => x.chain === id).sort((p, q) => p.in - q.in);
  if (!list.length) throw new Error(`timeline: no COPY chain "${id}"`);
  return list;
};
