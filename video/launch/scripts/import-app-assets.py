#!/usr/bin/env python3
"""Import the app's own renders into the film: app/screenshots/film -> video/launch/public/app.

The renders come from app/src/test/java/com/kairos/app/screens/FilmAssetsTest.kt:

    ./gradlew :app:testDebugUnitTest --tests 'com.kairos.app.screens.FilmAssetsTest' -Proborazzi.test.record=true

- Screens and share cards are opaque: saved as JPEG (q 92), screens at 1000 px wide.
- Sprites (Kai, the bloom, the mark, the wordmark) were rendered twice, on black (_k) and on white
  (_w). Difference matting recovers the exact alpha: a = 1 - (white - black) / 255, and the colour
  is black / a. Saved as PNG with alpha.

usage: python3 scripts/import-app-assets.py [--src ../../app/screenshots/film] [--out public/app]
"""
import argparse
import glob
import os

import numpy as np
from PIL import Image

ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
here = os.path.dirname(os.path.abspath(__file__))
ap.add_argument('--src', default=os.path.join(here, '..', '..', '..', 'app', 'screenshots', 'film'))
ap.add_argument('--out', default=os.path.join(here, '..', 'public', 'app'))
args = ap.parse_args()
os.makedirs(args.out, exist_ok=True)

SPRITE_W = {'kai': 440, 'bloom': 720, 'mark': 720, 'wordmark': None}

n_opaque = n_sprite = 0
for f in sorted(glob.glob(os.path.join(args.src, '*.png'))):
    name = os.path.basename(f)[:-4]
    if name.endswith('_w'):
        continue
    if name.endswith('_k'):
        base = name[:-2]
        k = np.asarray(Image.open(f).convert('RGB'), dtype=np.float32)
        w = np.asarray(Image.open(f[:-6] + '_w.png').convert('RGB'), dtype=np.float32)
        a = np.clip(1.0 - (w - k).mean(axis=2) / 255.0, 0, 1)
        rgb = np.clip(k / np.maximum(a[..., None], 1e-4), 0, 255)
        rgb[a < 1e-3] = 0
        im = Image.fromarray(np.dstack([rgb, a * 255]).round().astype(np.uint8), 'RGBA')
        if base.startswith('wordmark'):
            im = im.crop(im.getbbox())  # the wordmark's box is the window; keep only its ink
        width = SPRITE_W.get(base.split('_')[0])
        if width and im.width > width:
            im = im.resize((width, round(im.height * width / im.width)), Image.LANCZOS)
        im.save(os.path.join(args.out, base + '.png'), optimize=True)
        n_sprite += 1
    else:
        im = Image.open(f).convert('RGB')
        if not name.startswith('card_') and im.width > 1000:
            im = im.resize((1000, round(im.height * 1000 / im.width)), Image.LANCZOS)
        im.save(os.path.join(args.out, name + '.jpg'), quality=92, optimize=True)
        n_opaque += 1

total = sum(os.path.getsize(p) for p in glob.glob(os.path.join(args.out, '*')))
print(f'import-app-assets: {n_opaque} screens and cards, {n_sprite} sprites -> {args.out} ({total / 1e6:.1f} MB)')
