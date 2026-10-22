#!/usr/bin/env node
// peek.mjs: quick low-res frames for self-critique (bundles once, renders stills, tiles them).
// usage: node scripts/peek.mjs <out.png> <frame>[,<frame>...] [--comp Film] [--scale 0.25] [--cols 4]
import { bundle } from '@remotion/bundler';
import { renderStill, selectComposition } from '@remotion/renderer';
import { execFileSync } from 'node:child_process';
import path from 'node:path';
import fs from 'node:fs';

const args = process.argv.slice(2);
const opt = (k, d) => { const i = args.indexOf(k); return i >= 0 ? args[i + 1] : d; };
const out = args[0];
const frames = args[1].split(',').map(Number);
const comp = opt('--comp', 'Film');
const scale = Number(opt('--scale', '0.25'));
const cols = Number(opt('--cols', '4'));
const tmp = fs.mkdtempSync('/tmp/peek-');
const serveUrl = await bundle({ entryPoint: path.resolve('src/index.ts'), publicDir: path.resolve('public') });
const composition = await selectComposition({ serveUrl, id: comp, inputProps: {} });
const files = [];
for (const f of frames) {
  const file = path.join(tmp, `${String(f).padStart(5, '0')}.png`);
  await renderStill({ serveUrl, composition, frame: f, output: file, scale, inputProps: {}, browserExecutable: process.env.REMOTION_BROWSER || null });
  files.push(file);
}
execFileSync('python3', ['-c', `
import sys
from PIL import Image, ImageDraw
fs=sys.argv[3:]; cols=int(sys.argv[2]); ims=[Image.open(f).convert('RGB') for f in fs]
w,h=ims[0].size; rows=(len(ims)+cols-1)//cols
S=Image.new('RGB',(cols*w+(cols-1)*4,rows*(h+18)),(30,30,30)); d=ImageDraw.Draw(S)
for i,(f,im) in enumerate(zip(fs,ims)):
  x=(i%cols)*(w+4); y=(i//cols)*(h+18); S.paste(im,(x,y+18)); d.text((x+4,y+3),'f'+str(int(f.split('/')[-1][:5])),fill=(255,255,255))
S.save(sys.argv[1])
`, out, String(cols), ...files], { stdio: 'inherit' });
console.log(`peek: ${out} (${frames.length} frames)`);
