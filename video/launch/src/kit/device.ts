// The story device's seams: where the coral moment petal is, in screen px, on the frames where it
// crosses from one act to the next. Both sides of each seam read the same numbers.
import { U } from '../brand/tokens';
import { CUE, W } from '../timeline';

/** Act 2 -> 3: the petal has left the dot of the i and is falling toward the rising phone. */
export const FALL = { from: CUE.rise - 22, at: CUE.rise, x: W / 2 + 40 * U, y: 360 * U, size: 46 * U, rot: 45 };
