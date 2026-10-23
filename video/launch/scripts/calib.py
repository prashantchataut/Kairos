#!/usr/bin/env python3
"""Check a live overlay against the app render it sits on (src/kit/Calib.tsx).

Renders the CalibToday still (the Today render with the film's word overlay set to the same word on
top) and compares it with public/app/today.jpg: the ink extents of the word, the chip, the part of
speech and the definition must agree to 4 px (Chrome and Android shape a 750 px line ~0.5% apart), and the mean error in the patch must stay near JPEG
noise. Run after touching TODAY in src/kit/App.tsx.

usage: python3 scripts/calib.py
"""
import subprocess
import sys

import numpy as np
from PIL import Image

OUT = 'out/qa/calib-today.png'
subprocess.run(['npx', 'remotion', 'still', 'CalibToday', OUT, '--log=error'], check=True)
a = np.asarray(Image.open(OUT).convert('RGB'), dtype=np.float32)
b = np.asarray(Image.open('public/app/today.jpg').convert('RGB'), dtype=np.float32)

# bands in render px (y0, y1, x0, x1) for each text element of the word card
BANDS = {'word': (790, 930, 90, 930), 'pronunciation+pos': (1000, 1036, 130, 600), 'definition': (1095, 1218, 90, 930)}
ok = True
for name, (y0, y1, x0, x1) in BANDS.items():
    box = []
    for im in (a, b):
        ink = im[y0:y1, x0:x1].mean(axis=2) < 200
        ys, xs = np.where(ink)
        box.append((int(xs.min()) + x0, int(ys.min()) + y0, int(xs.max()) + x0, int(ys.max()) + y0))
    off = max(abs(p - q) for p, q in zip(*box))
    ok &= off <= 4
    print(f'{name:18s} overlay {box[0]}  render {box[1]}  max offset {off} px')
err = np.abs(a - b).mean(axis=2)[750:1275, 90:910].mean()
print(f'patch mean error {err:.1f} (JPEG + antialiasing; a misplaced overlay is 20+)')
ok &= err < 12
print('calib: PASS' if ok else 'calib: FAIL')
sys.exit(0 if ok else 1)
