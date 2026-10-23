import React, { useLayoutEffect, useRef, useState } from 'react';
import { useDelayRender } from 'remotion';

export type LetterBox = { x: number; w: number };

/**
 * Measures each letter of `text` set in `style` from the DOM (offset within the line), once per tab,
 * holding the frame until it is done. Canvas measurement cannot see a variable font's optical size
 * axis, so a word set in Fraunces at opsz 144 is measured where it is drawn. Call under FontGate.
 * Returns [boxes, probe]: render `probe` anywhere; boxes is null until measured.
 */
export const useLetters = (text: string, style: React.CSSProperties): [LetterBox[] | null, React.ReactElement] => {
  const { delayRender, continueRender } = useDelayRender();
  const [handle] = useState(() => delayRender(`useLetters: ${text}`));
  const [boxes, setBoxes] = useState<LetterBox[] | null>(null);
  const ref = useRef<HTMLSpanElement>(null);
  useLayoutEffect(() => {
    const el = ref.current;
    if (!el || boxes) return;
    // offsetLeft/offsetWidth are layout px, untouched by any scale on the way to the screen.
    setBoxes(Array.from(el.children).map((c) => ({ x: (c as HTMLElement).offsetLeft, w: (c as HTMLElement).offsetWidth })));
    continueRender(handle);
  }, [boxes, continueRender, handle]);
  const probe = React.createElement(
    'span',
    { ref, 'aria-hidden': true, style: { ...style, position: 'absolute', left: 0, top: 0, visibility: 'hidden', whiteSpace: 'pre', display: 'inline-flex', flexWrap: 'nowrap', transform: 'none' } },
    Array.from(text).map((ch, i) => React.createElement('span', { key: i, style: { display: 'inline-block' } }, ch)),
  );
  return [boxes, probe];
};
