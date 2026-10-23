// The overlay check: a screen render with the film's live overlay on top, at the render's own
// 1000 x 2150. scripts/calib.py diffs it against the render: an overlay that reads the same text
// as the screenshot must disappear (mean error near JPEG noise).
import React from 'react';
import { FontGate } from '../lib/FontGate';
import { SHOT, Shot, TodayWordPatch, todayWordStyle } from './App';
import { TODAY as WORDS } from './data';

export const CalibToday: React.FC<{ overlay: boolean }> = ({ overlay }) => (
  <FontGate>
    <div style={{ position: 'absolute', left: 0, top: 0, width: SHOT.w, height: SHOT.h, transformOrigin: '0 0', transform: `scale(${SHOT.px})` }}>
      <Shot name="today" />
      {overlay && <TodayWordPatch word={<span style={todayWordStyle}>{WORDS[0].word}</span>} info={WORDS[0]} />}
    </div>
  </FontGate>
);
