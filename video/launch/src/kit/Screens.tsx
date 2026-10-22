// cinetic:brand-supplied coral, serif, gradient, lilac, handwriting: Kairos' own UI (BRIEF.md).
//
// The app's screens, rebuilt from docs/screenshots (Today, Feed, Play, the time capsule and the
// share studio) in app units: a 400 x 868 canvas that <Phone> scales. Every prop is a pose, so the
// acts drive them from the frame.
import React from 'react';
import { BLOOM, Bloom } from '../brand/Mark';
import { C, FONT } from '../brand/tokens';
import { FEED, FEED_CHIP, type FeedItem, LETTER, type Word } from './data';
import { at, Chip, Heart, Kai, PHONE, SHADOW, SHADOW_SOFT, skyGradient, ui, wordStyle } from './Kit';

const SW = PHONE.sw;

// ------------------------------------------------------------------------------------------
// Today

export type TodayPose = {
  word: React.ReactNode; // the word itself, rendered by the act (it rewrites in place)
  info: Word; // pronunciation, part of speech, meaning
  infoFade?: number; // 0..1 the details cross-fade while the word rewrites
  heart?: number; // 0..1 the Save heart fills coral
  heartHidden?: boolean; // the device petal is still flying in: draw the outline only
  kaiLook?: [number, number];
  kaiBlink?: number;
  deck?: number; // 0..1 the cards behind fan out
};

/** The word card alone (also used as the dive target). 352 x 392 app units. */
export const WordCard: React.FC<{ pose: TodayPose }> = ({ pose }) => (
  <div style={{ position: 'relative', width: 352, height: 392, borderRadius: 30, background: C.surface, boxShadow: SHADOW_SOFT }}>
    <div style={at(26, 26)}>
      <Chip bg={C.skyFade} color={C.cobalt} size={13}>
        Word of the day
      </Chip>
    </div>
    <div style={{ ...at(296, 22), width: 34, height: 34, borderRadius: 17, background: C.surfaceSoft }} />
    <div style={at(26, 96)}>{pose.word}</div>
    <div style={{ ...at(26, 186), opacity: 1 - (pose.infoFade ?? 0) }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <Chip size={14}>{pose.info.say}</Chip>
        <span style={{ ...wordStyle(15, C.inkSoft, 400) }}>{pose.info.pos}</span>
      </div>
      <div style={{ ...ui(18, 400, C.ink), whiteSpace: 'normal', width: 300, marginTop: 18, lineHeight: 1.4 }}>{pose.info.means}</div>
    </div>
    <div style={{ ...at(26, 340), display: 'flex', alignItems: 'center', gap: 10 }}>
      <Kai size={26} look={pose.kaiLook} blink={pose.kaiBlink} />
      <span style={ui(13, 400, C.inkSoft)}>From your feelings interests</span>
    </div>
  </div>
);

/** Where the Save heart sits on the Today screen (app units, its centre): the device lands here. */
export const TODAY_HEART = { x: 162, y: 664, size: 22 };

export const TodayScreen: React.FC<{ pose: TodayPose }> = ({ pose }) => {
  const days = [
    ['Tue', '20', C.mint],
    ['Wed', '21', C.sun],
    ['Thu', '22', null],
    ['Fri', '23', null],
    ['Sat', '24', null],
  ] as const;
  const fan = pose.deck ?? 1;
  return (
    <div style={{ position: 'absolute', inset: 0, background: skyGradient(0.15) }}>
      <div style={at(22, 46)}>
        <div style={ui(13, 500, C.white, { opacity: 0.85 })}>Thursday, October 22</div>
        <div style={ui(25, 800, C.white, { letterSpacing: '-0.035em', marginTop: 2 })}>Good morning</div>
      </div>
      <div style={at(330, 36)}>
        <Kai size={52} look={pose.kaiLook} blink={pose.kaiBlink} />
      </div>
      {days.map(([d, n, dot], i) => {
        const x = 30 + i * 80;
        const y = 104 + [0, 10, 24, 10, 0][i];
        const bold = i === 2;
        return (
          <div key={d} style={{ ...at(x, y), width: 46, textAlign: 'center' }}>
            <div style={ui(13, 500, C.white, { opacity: bold ? 1 : 0.7 })}>{d}</div>
            <div style={ui(bold ? 22 : 18, 700, C.white, { opacity: bold ? 1 : 0.8 })}>{n}</div>
            {dot && <div style={{ width: 11, height: 11, borderRadius: 6, background: dot, margin: '4px auto 0' }} />}
          </div>
        );
      })}
      {/* the deck: two tinted cards peek behind the word card */}
      <div style={{ ...at(30, 196, `rotate(${-3.5 * fan}deg)`), width: 340, height: 380, borderRadius: 30, background: C.sun, opacity: 0.45 }} />
      <div style={{ ...at(34, 204, `rotate(${3 * fan}deg)`), width: 340, height: 380, borderRadius: 30, background: C.lilac, opacity: 0.35 }} />
      <div style={at(24, 214)}>
        <WordCard pose={pose} />
      </div>
      {/* Save */}
      <div style={{ ...at(22, 636), width: 56, height: 56, borderRadius: 28, border: `1.5px solid ${C.white}`, opacity: 0.5 }} />
      <div style={{ ...at(322, 636), width: 56, height: 56, borderRadius: 28, border: `1.5px solid ${C.white}`, opacity: 0.5 }} />
      <div style={{ ...at(92, 636), width: 216, height: 56, borderRadius: 28, background: C.surface, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10 }}>
        <div style={{ width: TODAY_HEART.size, height: TODAY_HEART.size }}>{!pose.heartHidden && <Heart size={TODAY_HEART.size} fill={pose.heart ?? 0} />}</div>
        <span style={ui(17, 600)}>Save</span>
      </div>
      {/* the next card, cut by the bottom edge, like the real scroll */}
      <div style={{ ...at(22, 730), width: 356, height: 200, borderRadius: 30, background: C.surface }}>
        <div style={{ ...at(24, 26), display: 'flex', alignItems: 'center', gap: 14 }}>
          <Kai size={44} />
          <div>
            <div style={ui(18, 600)}>How did that land?</div>
            <div style={ui(13, 400, C.inkSoft)}>Your answer shapes what comes next.</div>
          </div>
        </div>
      </div>
    </div>
  );
};

// ------------------------------------------------------------------------------------------
// Feed: an endless reel of full-bleed cards

const FEED_BG: Record<FeedItem['tint'], string> = { cobalt: C.cobalt, sky: C.skyCandy, mint: C.mint, night: C.night2 };

export const FeedCard: React.FC<{ item: FeedItem; kept?: number }> = ({ item }) => {
  const bg = FEED_BG[item.tint];
  return (
    <div style={{ position: 'absolute', left: 18, top: 0, width: 300, height: 600, borderRadius: 28, background: bg, overflow: 'hidden' }}>
      {item.kind === 'word' && (
        <>
          <div style={at(24, 26)}>
            <div style={ui(12, 600, C.white, { opacity: 0.75 })}>A word, kept</div>
          </div>
          <div style={at(24, 70)}>
            <div style={wordStyle(54, C.white)}>{item.w.word}</div>
            <div style={ui(13, 400, C.white, { opacity: 0.8, marginTop: 10 })}>{`${item.w.say} · ${item.w.pos}`}</div>
            <div style={{ ...ui(18, 400, C.white), whiteSpace: 'normal', width: 250, marginTop: 14, lineHeight: 1.4 }}>{item.w.means}</div>
          </div>
          {/* a big bloom of the card's art, cropped by the bottom edge */}
          <div style={{ ...at(40, 330), opacity: 0.95 }}>
            <Bloom size={300} petal={C.white} moment={null} />
          </div>
        </>
      )}
      {item.kind === 'thought' && (
        <div style={at(24, 210)}>
          <div style={{ ...wordStyle(34, C.white, 500), lineHeight: 1.12 }}>{item.text}</div>
          <div style={ui(13, 400, C.white, { opacity: 0.7, marginTop: 18 })}>A thought</div>
        </div>
      )}
      {item.kind === 'book' && (
        <div style={at(24, 120)}>
          <div style={wordStyle(34, C.white)}>{item.title}</div>
          <div style={ui(14, 400, C.white, { opacity: 0.8 })}>{item.author}</div>
          <div style={{ ...ui(18, 400, C.white), whiteSpace: 'normal', width: 250, marginTop: 14 }}>{item.gist}</div>
        </div>
      )}
      <div style={{ ...at(270, 576), opacity: 0.8 }}>
        <Bloom size={18} moment={null} />
      </div>
    </div>
  );
};

/** Where the Keep heart sits on the Feed screen (app units, centre). */
export const FEED_KEEP = { x: 356, y: 430, size: 26 };

export const FeedScreen: React.FC<{ scroll: number; keep: number; chip: number; tabs?: number }> = ({ scroll, keep, chip }) => {
  const pitch = 620;
  return (
    <div style={{ position: 'absolute', inset: 0, background: C.night2 }}>
      <div style={at(22, 46)}>
        <div style={ui(26, 800, C.white, { letterSpacing: '-0.035em' })}>Feed</div>
      </div>
      <div style={{ ...at(22, 92), display: 'flex', gap: 8 }}>
        <Chip bg={C.white} color={C.ink} size={13}>
          For you
        </Chip>
        {['Words', 'Thoughts', 'Affirmations'].map((t) => (
          <Chip key={t} bg="rgba(255,255,255,0.14)" color={C.white} size={13}>
            {t}
          </Chip>
        ))}
      </div>
      <div style={{ position: 'absolute', left: 0, top: 150, width: SW, height: 640, overflow: 'hidden' }}>
        {FEED.map((item, i) => {
          const y = i * pitch - scroll;
          if (y < -pitch || y > 700) return null;
          return (
            <div key={i} style={at(0, y)}>
              <FeedCard item={item} />
            </div>
          );
        })}
      </div>
      {/* the rail: Keep, Share, Less */}
      {[
        ['Keep', 0],
        ['Share', 1],
        ['Less', 2],
      ].map(([label, i]) => (
        <div key={label as string} style={{ ...at(334, 400 + (i as number) * 86), width: 44, textAlign: 'center' }}>
          <div style={{ width: 44, height: 44, borderRadius: 22, border: '1.5px solid rgba(255,255,255,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            {i === 0 && <Heart size={FEED_KEEP.size * 0.85} fill={keep} stroke={C.white} />}
            {i === 1 && <div style={{ width: 14, height: 14, border: `2px solid ${C.white}`, borderRadius: 3 }} />}
            {i === 2 && <div style={{ width: 16, height: 2, background: C.white, borderRadius: 1 }} />}
          </div>
          <div style={ui(11, 500, C.white, { marginTop: 4 })}>{label}</div>
        </div>
      ))}
      <div style={{ ...at(22, 560, `translateY(${(1 - chip) * 14}px)`), opacity: chip }}>
        <Chip bg="rgba(255,255,255,0.16)" color={C.white} size={13}>
          <Kai size={18} />
          {FEED_CHIP}
        </Chip>
      </div>
    </div>
  );
};

// ------------------------------------------------------------------------------------------
// Play: a flashcard. 300 x 420 px at k = 1 (film px, not app units: these fill the frame).

export const CARD_HUE = { cobalt: C.cobalt, mint: C.mint, coral: C.coral, sun: C.sun, skyCandy: C.skyCandy } as const;
export type CardHue = keyof typeof CARD_HUE;

export const Flashcard: React.FC<{ w: Word; hue: CardHue; turned?: number; scale?: number; mono?: number }> = ({ w, hue, turned = 0, mono = 0 }) => {
  // The front shows the word; turning it over (rotateY) shows the meaning on the back.
  const back = turned > 0.5;
  const ink = hue === 'sun' ? C.ink : C.white;
  const bg = CARD_HUE[hue];
  return (
    <div style={{ position: 'relative', width: 300, height: 420, transform: `rotateY(${turned * 180}deg)`, transformStyle: 'preserve-3d' }}>
      <div
        style={{
          position: 'absolute',
          inset: 0,
          borderRadius: 34,
          background: bg,
          boxShadow: SHADOW,
          transform: back ? 'rotateY(180deg)' : undefined,
          overflow: 'hidden',
        }}
      >
        {!back ? (
          <>
            <div style={at(30, 34)}>
              <div style={ui(13, 700, ink, { opacity: 0.75, letterSpacing: '0.12em' })}>TRUE OR NOT?</div>
            </div>
            <div style={at(30, 76)}>
              <div style={wordStyle(56, ink)}>{w.word}</div>
            </div>
            <div style={{ ...at(30, 344), display: 'flex', gap: 10 }}>
              {['True', 'Not quite'].map((t) => (
                <div key={t} style={{ width: 114, height: 44, borderRadius: 22, border: `1.5px solid ${ink}`, opacity: 0.7, display: 'flex', alignItems: 'center', justifyContent: 'center', ...ui(16, 600, ink) }}>
                  {t}
                </div>
              ))}
            </div>
          </>
        ) : (
          <>
            <div style={at(30, 40)}>
              <div style={wordStyle(40, ink)}>{w.word}</div>
            </div>
            <div style={{ ...at(30, 104), ...ui(22, 500, ink), whiteSpace: 'normal', width: 240, lineHeight: 1.3 }}>{w.means}</div>
          </>
        )}
        {/* duotone until played: a light wash over the candy colour, never a muddy desaturation */}
        {mono > 0.01 && <div style={{ position: 'absolute', inset: 0, background: C.skyFade, opacity: 0.62 * mono }} />}
        {/* the corner pip: the moment petal on every card */}
        <div style={at(246, back ? 360 : 30)}>
          <Bloom size={26} petal={ink} moment={hue === 'coral' ? C.white : C.coral} />
        </div>
      </div>
    </div>
  );
};

// ------------------------------------------------------------------------------------------
// The time capsule: the letter and its envelope (film px).

export const LETTER_SIZE = { w: 560, h: 700 };

export const LetterPaper: React.FC<{ typed: number; lines: number }> = ({ typed, lines }) => {
  // `typed` characters of the greeting are on; `lines` 0..1 the body's lines appear in turn.
  const g = LETTER.greeting.slice(0, Math.max(0, Math.round(typed)));
  return (
    <div style={{ position: 'relative', width: LETTER_SIZE.w, height: LETTER_SIZE.h, borderRadius: 22, background: C.letter, boxShadow: SHADOW, overflow: 'hidden' }}>
      {Array.from({ length: 13 }, (_, i) => (
        <div key={i} style={{ position: 'absolute', left: 48, right: 48, top: 150 + i * 46, height: 1.5, background: C.line }} />
      ))}
      <div style={{ position: 'absolute', left: 64, top: 0, bottom: 0, width: 1.5, background: C.coral, opacity: 0.25 }} />
      <div style={at(80, 70)}>
        <div style={{ fontFamily: FONT.hand, fontSize: 64, fontWeight: 600, color: C.letterInk, lineHeight: 1, whiteSpace: 'pre' }}>{g}</div>
      </div>
      {LETTER.body.map((l, i) => {
        const o = Math.min(1, Math.max(0, lines * LETTER.body.length - i));
        return (
          <div key={i} style={{ ...at(80, 156 + i * 46), opacity: o, fontFamily: FONT.word, fontSize: 30, color: C.letterInk, whiteSpace: 'pre', lineHeight: 1 }}>
            {l}
          </div>
        );
      })}
      <div style={{ ...at(80, 156 + 5 * 46), opacity: Math.min(1, Math.max(0, lines * 5 - 4)), fontFamily: FONT.hand, fontSize: 36, color: C.letterInk, whiteSpace: 'pre' }}>{LETTER.sign}</div>
    </div>
  );
};

/** The envelope, front view: a body and a flap that closes (`flap` 0 open .. 1 shut). */
export const ENVELOPE = { w: 620, h: 400 };
export const Envelope: React.FC<{ flap: number; color?: string; children?: React.ReactNode }> = ({ flap, color = C.skyCandy, children }) => {
  const { w, h } = ENVELOPE;
  // the flap is a triangle hinged at the top edge; rotateX from -180 (open, pointing up) to 0 (shut)
  const rot = -180 * (1 - flap);
  const flapEl = (
      <div style={{ position: 'absolute', left: 0, top: 0, width: w, height: h * 0.62, transformOrigin: '50% 0', transform: `rotateX(${rot}deg)`, backfaceVisibility: 'visible' }}>
        <svg width={w} height={h * 0.62} style={{ overflow: 'visible', display: 'block' }}>
          <path d={`M14 0 L${w - 14} 0 L${w / 2} ${h * 0.6} Z`} fill={color} stroke={C.white} strokeOpacity={0.3} strokeWidth={2} strokeLinejoin="round" style={{ filter: 'brightness(0.94)' }} />
        </svg>
      </div>
  );
  return (
    <div style={{ position: 'relative', width: w, height: h, perspective: 1600 }}>
      <div style={{ position: 'absolute', inset: 0, borderRadius: 26, background: color, boxShadow: SHADOW }} />
      {/* open, the flap stands up behind the letter; once it passes upright it closes over the pocket */}
      {flap < 0.5 && flapEl}
      {children}
      {/* the front pocket */}
      <svg width={w} height={h} style={{ position: 'absolute', inset: 0, overflow: 'visible' }}>
        <path d={`M0 ${h * 0.34} L${w / 2} ${h * 0.72} L${w} ${h * 0.34} L${w} ${h - 26} Q${w} ${h} ${w - 26} ${h} L26 ${h} Q0 ${h} 0 ${h - 26} Z`} fill={color} />
        <path d={`M0 ${h * 0.34} L${w / 2} ${h * 0.72} L${w} ${h * 0.34}`} stroke={C.white} strokeOpacity={0.35} strokeWidth={2} fill="none" />
      </svg>
      {flap >= 0.5 && flapEl}
    </div>
  );
};

// ------------------------------------------------------------------------------------------
// Share cards: pin 2:3, story 9:16, square 1:1, each its own design (film px, height 640).

export type ShareShape = 'pin' | 'story' | 'square';
export const SHARE_RATIO: Record<ShareShape, number> = { pin: 2 / 3, story: 9 / 16, square: 1 };

export const ShareCard: React.FC<{ shape: ShareShape; h: number; children?: React.ReactNode; bg: string }> = ({ shape, h, bg, children }) => (
  <div style={{ position: 'relative', width: h * SHARE_RATIO[shape], height: h, borderRadius: h * 0.05, background: bg, overflow: 'hidden', boxShadow: SHADOW }}>{children}</div>
);

export { BLOOM };
