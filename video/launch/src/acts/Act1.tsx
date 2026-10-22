// cinetic:brand-supplied coral: the moment hand is Kairos' coral (BRIEF.md).
import React from 'react';
import { AbsoluteFill, useCurrentFrame } from 'remotion';
import { petalPath } from '../brand/Mark';
import { C, TYPE, U } from '../brand/tokens';
import { Chain } from '../fx/Chain';
import { clamp, E, hitPulse, lmix, mix, prog } from '../lib/anim';
import { mixColor } from '../lib/color';
import { Grain } from '../kit/Kit';
import { ACT, CUE, H } from '../timeline';

// ACT 1 - CHRONOS. Clock time: a huge cobalt dial bleeding off the right edge, its twelve hour
// marks drawn as the bloom's petals, three petal hands whirring faster and faster while "Days" /
// "just pass." builds on the left. Then the long hand stops dead at one o'clock and turns coral,
// the moment petal, while the other hands spin on and fade: "Some moments" -> stop -> "stay."
// Frame 0 is already turning.

/** The dial, in screen px. Act 2 opens on exactly this pose. */
export const DIAL = { cx: 1400 * U, cy: H / 2, r: 600 * U };
/** The moment hand: a full-length bloom petal, resting at 45° (one o'clock) from CUE.stop on. */
export const HAND = { len: 0.86, angle: 45 };

// Angular speed of the long hand in degrees per frame: a slow turn on frame 0, faster on the whir,
// full speed on blurUp, then a dead stop on CUE.stop. Its tip peaks near 60 px/f, under the blur
// ceiling. The hour hand turns at 0.4x, the thin second hand at 1.6x.
const omega = (f: number) => mix(mix(2.2, 4.4, prog(f, CUE.whir - 20, CUE.whir + 30, E.smooth)), 6.6, prog(f, CUE.blurUp - 10, CUE.blurUp + 60, E.smooth));

/** Long-hand angle at frame f: integrated from the stop backwards, so it lands on 45° exactly. */
export const handAngle = (f: number) => {
  if (f >= CUE.stop) return HAND.angle;
  const step = 0.5;
  let a = 0;
  for (let t = f; t < CUE.stop; t += step) a += omega(t + step / 2) * Math.min(step, CUE.stop - t);
  return HAND.angle - a;
};

export const Act1: React.FC = () => {
  const f = useCurrentFrame() + ACT.chronos.from;
  const stopped = f >= CUE.stop;
  const t = f - CUE.stop;

  const long = handAngle(f) + (stopped ? -1.4 * hitPulse(t, 1, 6) : 0); // a dead stop, with a 1.4° shudder
  // The other hands keep time after the stop, slowing down, and fade out: time goes on, the moment stays.
  const spin = (k: number) => {
    const before = (handAngle(Math.min(f, CUE.stop)) - HAND.angle) * k;
    const after = stopped ? omega(CUE.stop) * k * 18 * (1 - Math.exp(-t / 18)) : 0;
    return HAND.angle * k + before + after + (k === 0.4 ? 200 : 110);
  };
  const others = 1 - prog(f, CUE.stop + 10, CUE.stop + 70, E.smooth);
  const coral = prog(f, CUE.stop - 1, CUE.stop + 8, E.out);
  const dim = mix(1, 0.35, prog(f, CUE.stop + 10, CUE.stop + 80, E.smooth)); // the hour marks step back

  // Camera: a slow 4% creep through the act, a punch on the stop.
  const k = lmix(1, 1.04, prog(f, 0, ACT.chronos.dur, E.linear)) * (1 + 0.02 * hitPulse(t, 2, 6));
  const r = DIAL.r;

  return (
    <AbsoluteFill style={{ background: C.canvas, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0, transformOrigin: `${DIAL.cx}px ${DIAL.cy}px`, transform: `scale(${k})` }}>
        <svg width={DIAL.cx + r + 10} height={H} style={{ position: 'absolute', left: 0, top: 0, overflow: 'visible' }}>
          <circle cx={DIAL.cx} cy={DIAL.cy} r={r} fill={C.cobalt} />
          {Array.from({ length: 60 }, (_, i) => {
            const a = i * 6;
            if (i % 5 === 0) {
              return <path key={i} d={petalPath(r * 0.1, 1)} fill={C.white} opacity={0.9 * dim} transform={`translate(${DIAL.cx} ${DIAL.cy}) rotate(${a}) translate(0 ${-r * 0.82}) rotate(180) translate(0 ${-r * 0.024})`} />;
            }
            return <rect key={i} x={-1.5 * U} y={-r * 0.93} width={3 * U} height={r * 0.035} rx={1.5 * U} fill={C.white} opacity={0.4 * dim} transform={`translate(${DIAL.cx} ${DIAL.cy}) rotate(${a})`} />;
          })}
          {/* hour hand: a short petal */}
          <path d={petalPath(r * 0.52, 1)} fill={C.white} opacity={others} transform={`translate(${DIAL.cx} ${DIAL.cy}) rotate(${spin(0.4)})`} />
          {/* second hand: a thin line */}
          <rect x={-2 * U} y={-r * 0.6} width={4 * U} height={r * 0.72} rx={2 * U} fill={C.white} opacity={0.85 * others} transform={`translate(${DIAL.cx} ${DIAL.cy}) rotate(${spin(1.6)})`} />
          {/* the long hand: the moment petal */}
          <path d={petalPath(r * HAND.len, 1)} fill={mixColor(C.white, C.coral, coral)} transform={`translate(${DIAL.cx} ${DIAL.cy}) rotate(${long})`} />
          <circle cx={DIAL.cx} cy={DIAL.cy} r={r * 0.05} fill={C.white} />
        </svg>
      </div>
      <Chain chain="days" frame={f} align="left" x={128 * U} top={H / 2 - (TYPE.line.size * U) / 2} accent="none" color={C.ink} />
      <Chain chain="stay" frame={f} align="left" x={128 * U} top={H / 2 - (TYPE.line.size * U) / 2} accent="hold" color={C.ink} accentColor={C.coral} />
      <Grain frame={f} opacity={0.04 * clamp(1)} />
    </AbsoluteFill>
  );
};
