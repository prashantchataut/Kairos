# Kairos launch film

A 39-second launch film for Kairos, built from code with the [cinetic](https://github.com/Leonxlnx/cinetic)
skill (Remotion 4) and scored with an [earshot](https://github.com/Leonxlnx/earshot) sound kit.
Brief and the notes on the six references from PR #611: [`BRIEF.md`](BRIEF.md). Concept, device path
and beat sheet: [`TREATMENT.md`](TREATMENT.md).

## The idea

**Most days just pass; Kairos keeps the one moment that should stay.** χρόνος is clock time,
καιρός is the right moment, and the app's bloom mark already says it: eight petals, one of them at
one o'clock like the hand of a clock. The film opens on a clock of days whirring past. One hand stops
dead and turns coral, and that coral petal travels through every shot until it becomes the moment
petal of the logo:

| Act | Time | The coral petal is… | On screen (the app's own renders) |
|---|---|---|---|
| 1 Chronos | 0:00 | the clock hand that stops: "Days / just pass." "Some moments / stay." | film art |
| 2 Name | 0:06 | a flood that shrinks into the dot of the ı in "Kairos" / "the right moment." | the wordmark's face, Fraunces 600 |
| 3 Word | 0:10 | the Save heart; the word of the day rewrites itself, Saudade → Apricity → Hiraeth | `today.jpg`, the Saved state, Kai's drawKai frames |
| 4 Feed | 0:16 | the Keep heart; "It learns / what you keep." with a hand-drawn loop | `feed_2` → `feed_0` (kept) → `feed_1` |
| 5 Play | 0:20 | (Kai rises beside the phone, delighted) | `play_front` turning over to `play_back` |
| 6 Letter | 0:24 | the coral seal stamping down on the sealed letter, under the night | `compose_write` (its title types) → `compose_when` → `letter_sealed` |
| 7 Share | 0:30 | the sticker on a share card that changes shape: pin → story → square | the studio's Bloom, Bold, Glow, Eclipse and Garden cards |
| 8 Lockup | 0:34 | the moment petal: the app's bloom unfolds around it, and it whitens into the mark | drawBloom's unfold frames, the app's Wordmark |

## The app, as it is

Nothing in the film redraws the app. `app/src/test/java/com/kairos/app/screens/FilmAssetsTest.kt`
renders the real composables with Roborazzi: every screen the film shows (400 x 860 dp at 2.5 px/dp),
Kai's frame sequences straight from `drawKai`, the bloom unfolding from `drawBloom`, the mark, the
`Wordmark`, and twelve studio templates in pin, story and square. `scripts/import-app-assets.py`
difference-mattes the sprites into `public/app`; `src/kit/App.tsx` shows them. To refresh after an app
change:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.kairos.app.screens.FilmAssetsTest' -Proborazzi.test.record=true
python3 video/launch/scripts/import-app-assets.py
```

The few live overlays (the word rewriting, Saved and Kept, the title typing, the seal stamping) are
set in the app's faces and colours at positions measured off the renders. `python3 scripts/calib.py`
renders the Today overlay over `today.jpg` and checks the ink lines up (word 0 px, chip 2 px,
definition 4 px off).

## Render it

Heavy renders run on your machine or on GitHub Actions, not in the agent sandbox. On CI: install
`ci/render.yml` once (`cp video/launch/ci/render.yml .github/workflows/film.yml`, commit, push), then
Actions → film → Run workflow; the MP4s arrive as the run's `launch-film` artifact. Locally: Needs Node 22+, Python 3.11+
with `numpy scipy soundfile pyloudnorm opencv-python librosa pillow`, and ffmpeg 6+ on `PATH`.

```bash
cd video/launch
npm install

# 1. The sound kit (once; ~15 s). earshot renders it without models or PyTorch:
git clone https://github.com/Leonxlnx/earshot ~/earshot
python3 -m venv ~/earshot/.venv
~/earshot/.venv/bin/pip install numpy scipy soundfile soxr librosa pyloudnorm scikit-learn pillow matplotlib
~/earshot/.venv/bin/pip install --no-deps -e ~/earshot
export EARSHOT_PYTHON=~/earshot/.venv/bin/python
npm run kit

# 2. The soundtrack (seconds): cues from the picture code, then the score and the master
npm run cues && npm run audio          # -> public/audio/soundtrack.wav, PASS at -14 LUFS

# 3. A sharp preview with sound (~2-4 min), then the motion-blurred master (~20-40 min on 4 cores)
npm run render:preview                 # -> out/preview.mp4
npm run render                         # -> out/film.mp4 (render.sh --blur, sync-checked and probed)
npm run qa                             # probe, forensics, A/V audit, contact sheet
```

Iterate with `npm run studio` (each act is its own composition under "Acts"), or quick stills with
`node scripts/peek.mjs out/qa/peek.png 0,300,600,900` (quarter resolution, tiled).

## Checks

`npm run check` runs `tsc`, `lint-film.mjs` (the hard bans: Kairos' coral, Fraunces, Caveat and its
sky/night gradients are recorded as brand-supplied in `BRIEF.md`) and `grid-check.ts` (the 120 BPM grid,
reading holds, fragment flow, word budget). All three pass. Four grid warnings are accepted on purpose:
two fragments that answer each other quickly ("Days" → "just pass.", "Kairos" → "the right moment."),
"Opens in 6 months" (a product chip, not copy) held 0.8 s, and the opening 10 s, whose only text-free
stretch is the 0.6 s coral flood.

`npm run audio` passed in the sandbox: −14.0 LUFS integrated, true peak −2.3 dBTP, LRA 8.0 LU,
0 dB of limiting, the payoff (the re-entry under the share cards at 0:30) 5.2 LU over the median.

## Files

| Path | What |
|---|---|
| `src/timeline.ts` | the only source of timing: acts, cues, every word of copy |
| `src/acts/Act1…8.tsx` | one act each, pure functions of the frame |
| `src/kit/` | the app rebuilt for film: Today, Feed, Play cards, letter, envelope, share cards, Kai, data |
| `src/brand/` | Kairos tokens, fonts (the app's own TTFs in `public/fonts`), the bloom ported from `Bloom.kt` |
| `src/sync.ts` | sound events computed from the picture |
| `audio/score.json`, `audio/kit.json` | the score (D major, 120 BPM, sections per act) and the crisp earshot kit |
| `backdrops.json`, `public/fx/` | the night and dawn gradients, baked as dithered PNGs |

Still to do: the 9:16 and 1:1 re-layouts (same timeline, new layouts per act) and the delivery
folder (`bash scripts/deliver.sh out/film.mp4 --variants Film9x16,Film1x1`).
