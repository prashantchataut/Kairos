import type React from 'react';
import { ACT } from '../timeline';
import { Act1 } from './Act1';
import { Act2 } from './Act2';

/** The acts in film order, one per ACT entry (Root registers composition ActN for the Nth). */
export const ACTS: { key: keyof typeof ACT; name: string; C: React.FC }[] = [
  { key: 'chronos', name: '1 Chronos', C: Act1 },
  { key: 'name', name: '2 Name', C: Act2 },
];
