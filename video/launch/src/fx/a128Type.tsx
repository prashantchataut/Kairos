// Type for Acts 1, 2 and 8: the copy blurs in word by word, and the name rises letter by letter
// from behind a line (a horizon) with the font's own kerning kept. Timing comes from COPY through
// fx/a128Time, so every fragment is fully in on its `resolved` frame and leaves from its `out`.
import React from 'react';
import { arrive, arriveK, blurOut, clamp, depart, E, prog } from '../lib/anim';
import { baselineOf, inkBox } from '../lib/measure';
import { copy, f60 } from '../timeline';
import { FOCUS, NEVER, WORD, wordSpan } from './a128Time';

const smooth = (t: number) => t * t * (3 - 2 * t);

// ------------------------------------------------------------------------------------------
// Measurement in the exact face (variation axes and tracking included). Only under FontGate.

const widths = new Map<string, number>();

/** Advance width (px) of `text` set in `face` at `size`, as the DOM lays it out (trailing tracking included). */
export const measureFace = (text: string, size: number, face: React.CSSProperties) => {
  const key = `${text}|${size}|${JSON.stringify(face)}`;
  const hit = widths.get(key);
  if (hit !== undefined) return hit;
  const span = document.createElement('span');
  span.textContent = text;
  Object.assign(span.style, {
    position: 'absolute',
    visibility: 'hidden',
    whiteSpace: 'pre',
    fontFamily: String(face.fontFamily ?? ''),
    fontWeight: String(face.fontWeight ?? 400),
    fontVariationSettings: String(face.fontVariationSettings ?? 'normal'),
    letterSpacing: String(face.letterSpacing ?? 'normal'),
    fontSize: `${size}px`,
    lineHeight: '1',
  });
  document.body.appendChild(span);
  const w = span.getBoundingClientRect().width;
  document.body.removeChild(span);
  widths.set(key, w);
  return w;
};

/** Left edge of each letter of `text` (kerning and tracking kept), and the word's ink-to-ink width. */
export const letterSlots = (text: string, size: number, face: React.CSSProperties) => {
  const chars = Array.from(text);
  const lefts = chars.map((ch, k) => measureFace(chars.slice(0, k + 1).join(''), size, face) - measureFace(ch, size, face));
  const ls = typeof face.letterSpacing === 'string' && face.letterSpacing.endsWith('em') ? parseFloat(face.letterSpacing) * size : 0;
  return { chars, lefts, width: measureFace(text, size, face) - ls };
};

// ------------------------------------------------------------------------------------------
// Copy: words come out of a blur as they rise a little, `stagger` apart; they leave word by word,
// lifting and blurring away. `rise: 0` holds a fragment still: it only comes into focus.

export type BlurWordsProps = {
  id: string; // COPY id
  frame: number; // absolute frame
  size: number;
  face: React.CSSProperties; // family, weight, tracking, axes, colour
  rise?: number; // em
  blur?: number; // px at the first frame
  lift?: number; // em the words lift as they leave
  exitIndex?: number; // this fragment's first word's place in its line (the exit runs along the line)
  exitStagger?: number;
  style?: React.CSSProperties;
};

export const BlurWords: React.FC<BlurWordsProps> = ({ id, frame: f, size, face, rise = 0.22, blur = 14, lift = 0.3, exitIndex = 0, exitStagger = WORD.exitStagger, style }) => {
  const c = copy(id);
  const words = c.text.split(' ');
  return (
    <span data-text={id} style={{ display: 'inline-flex', flexWrap: 'nowrap', whiteSpace: 'pre', fontSize: size, lineHeight: 1, ...face, ...style }}>
      {words.map((w, i) => {
        const [s, e] = wordSpan(id, i);
        if (f < s - 1) return <span key={i} style={{ display: 'inline-block', whiteSpace: 'pre', opacity: 0 }}>{w}{i < words.length - 1 ? ' ' : ''}</span>;
        const t = clamp((f - s) / (e - s));
        const p = WORD.ease(t);
        const k = arriveK(rise * size, e - s, WORD.ease);
        const o = c.out + (exitIndex + i) * exitStagger;
        const q = prog(f, o, o + WORD.exitDur, E.in);
        const gone = prog(f, o, o + WORD.exitDur, E.smooth);
        const y = (1 - arrive(p, k)) * rise - depart(q) * lift;
        const b = blur * (1 - clamp(t / FOCUS)) + blurOut(q, blur);
        return (
          <span
            key={i}
            style={{
              display: 'inline-block',
              whiteSpace: 'pre',
              transform: `translateY(${y.toFixed(4)}em)`,
              filter: b > 0.3 ? `blur(${b.toFixed(2)}px)` : undefined,
              opacity: smooth(clamp(t / 0.4)) * (1 - gone),
            }}
          >
            {w}
            {i < words.length - 1 ? ' ' : ''}
          </span>
        );
      })}
    </span>
  );
};

// ------------------------------------------------------------------------------------------
// The name: each letter rises from behind a line at `line` px (the clip's bottom edge), starting
// wholly under it, and lands with its baseline on `baseline`. The letters keep the word's kerning
// (letterSlots). Leaving, they sink back behind the line, first letter first.

/** Ink above the baseline of `text`, and the line-box offset of the baseline (lineHeight 1), for a face. */
export const nameMetrics = (text: string, size: number, face: React.CSSProperties) => {
  const weight = Number(face.fontWeight ?? 400);
  const family = String(face.fontFamily ?? '');
  return { ascent: inkBox(text, size, weight, family).ascent, baseline: baselineOf(size, weight, 1, family) };
};

export const RisingName: React.FC<{
  id: string; // COPY id (its text is drawn)
  frame: number;
  x: number; // left edge of the word (px)
  baseline: number; // px
  line: number; // the mask line (px), at or under the baseline
  size: number;
  face: React.CSSProperties;
  letters: { from: number; gap: number; dur: number };
  out?: number;
  outGap?: number;
  outDur?: number;
}> = ({ id, frame: f, x, baseline, line, size, face, letters, out = NEVER, outGap = f60(1), outDur = f60(12) }) => {
  const c = copy(id);
  const { chars, lefts, width } = letterSlots(c.text, size, face);
  const m = nameMetrics(c.text, size, face);
  const travel = line - baseline + m.ascent + size * 0.04; // from home to wholly under the line
  const k = arriveK(travel, letters.dur, E.out);
  const top = line - m.ascent - size * 0.6; // the clip starts well above the tallest letter
  const boxTop = baseline - m.ascent - top; // the word's ink box, inside the clip
  return (
    <div style={{ position: 'absolute', left: 0, top, width: x + width + size, height: line - top, overflow: 'hidden', pointerEvents: 'none' }}>
      <div data-text={id} style={{ position: 'absolute', left: x, top: boxTop, width, height: m.ascent }}>
        {chars.map((ch, i) => {
          const s = letters.from + i * letters.gap;
          const p = prog(f, s, s + letters.dur, E.out);
          const o = out + i * outGap;
          const q = prog(f, o, o + outDur, E.in);
          if (p <= 0 || q >= 1) return null;
          const y = (1 - arrive(p, k)) * travel + depart(q) * travel;
          return (
            <span
              key={i}
              style={{
                position: 'absolute',
                left: lefts[i],
                top: m.ascent - m.baseline, // a lineHeight-1 box whose baseline sits on the word's baseline
                display: 'block',
                whiteSpace: 'pre',
                fontSize: size,
                lineHeight: 1,
                ...face,
                transform: `translateY(${y.toFixed(3)}px)`,
              }}
            >
              {ch}
            </span>
          );
        })}
      </div>
    </div>
  );
};
