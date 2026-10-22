import type React from 'react';
import { ACT } from '../timeline';
import { Act1 } from './Act1';
import { Act2 } from './Act2';
import { Act3 } from './Act3';
import { Act4 } from './Act4';
import { Act5 } from './Act5';
import { Act6 } from './Act6';
import { Act7 } from './Act7';
import { Act8 } from './Act8';

/** The acts in film order, one per ACT entry (Root registers composition ActN for the Nth). */
export const ACTS: { key: keyof typeof ACT; name: string; C: React.FC }[] = [
  { key: 'chronos', name: '1 Chronos', C: Act1 },
  { key: 'name', name: '2 Name', C: Act2 },
  { key: 'word', name: '3 Word', C: Act3 },
  { key: 'feed', name: '4 Feed', C: Act4 },
  { key: 'play', name: '5 Play', C: Act5 },
  { key: 'letter', name: '6 Letter', C: Act6 },
  { key: 'share', name: '7 Share', C: Act7 },
  { key: 'lockup', name: '8 Lockup', C: Act8 },
];
