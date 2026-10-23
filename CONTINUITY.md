# CONTINUITY

Where work on Kairos stands, for the next session (human or agent). Keep it current; push it.

## Branch and PR

- PR #611 (`redesign/round-three`, the app redesign rounds 1–3 and the film's first cut) is merged
  into `main`. Film work continues on `main` in small commits.
- Pushing from the agent sandbox: plain `git push` has no credential there. Use
  `task/tools/ghpush.py <repo-dir> prashantchataut/Kairos main` (replays local commits through the
  GitHub API via the actl connector, then soft-resets the local branch onto the pushed commits). The
  connector cannot write `.github/workflows/*` (GitHub 404s it): CI edits have to be pushed by the owner.

## Done (app, PR #611)

Onboarding motion, the bloom logo, 16+ share-card designs (pin / story / square), Kai's writing with
built-in Mercury and Gemini keys + on-device Gemini Nano, micro book summaries, the Feed (reel of
words, thoughts, affirmations, books, learning from keeps/shares/time), Play (random flashcards),
page transitions, the redesigned letter to future you, no "written for you". See the PR description
and `git log` for detail.

## The launch film (`video/launch/`): rebuilt on the app's real renders (done in code)

PR #611 is merged; film work lands on `main` in small commits. The owner rejected the first cut (the
UI in the frames, the logo and Kai were hand-drawn look-alikes). Now every screen, card, Kai and mark
comes from the app's own renders (`public/app`, made by `app/src/test/.../FilmAssetsTest.kt` with
Roborazzi, imported by `scripts/import-app-assets.py`), shown by `src/kit/App.tsx`; the hand-drawn kit
is deleted. Details per act: `video/launch/README.md` table and "The app, as it is".

Checks (Oct 23, 2026): `npm run check` passes (tsc, lint-film 0 errors / 4 accepted warnings,
grid-check pass); `python3 scripts/calib.py` PASS (Today overlay vs `today.jpg`: word 0 px, chip 2 px,
definition 4 px). All eight acts and their seams reviewed on quarter- and half-res peeks.

Geometry: renders are 400 x 860 dp at 2.5 px/dp; the phone's screen canvas is the same 400 x 860. Kai
sprites are drawKai on a 220 dp canvas: a Kai the app drew at d dp is the sprite drawn d units wide
around the same centre (header Kai: 56 dp at (360, 36)). The Wordmark sprite's mark is its first h x h
square; drawBloom's unfold frames draw the bloom at radius 0.46 of their canvas.

Not done by the agent (heavy, the owner's): the MP4. `out/cues.json` was regenerated (the typed title
is now "Dear future me...", 15 key sounds), so the soundtrack must be re-rendered before the film.

## Exact next step

1. Owner: install the render workflow once, from the repo root:
   `cp video/launch/ci/render.yml .github/workflows/film.yml && git add .github/workflows/film.yml && git commit -m "CI: render the launch film" && git push`
   then Actions -> film -> Run workflow (tick "master" for the motion-blurred cut). Or locally:
   `video/launch/README.md` "Render it".
2. Watch the preview; notes go to the next session. Then the 9:16 and 1:1 re-layouts (`Film9x16`,
   `Film1x1` reusing `timeline.ts`) and `bash scripts/deliver.sh out/film.mp4 --variants Film9x16,Film1x1`.
3. Known small things: the screens' sample data greets "Prashant" (ScreenshotTest seed; change the
   seed name and re-run FilmAssetsTest if the film should be anonymous); during the bloom's first
   frames the white moment petal grows slightly out from under the coral one (4-6 frames).

Push from the sandbox with `python3 /workspace/task/tools/ghpush.py <repo-dir> prashantchataut/Kairos main`
(the sandbox's git has no credential; the script replays commits through the GitHub API via the actl
connector; it cannot touch `.github/workflows`).

## Commands

```bash
cd video/launch
npm install                                   # once
npx tsc --noEmit && npx tsx scripts/grid-check.ts src/timeline.ts
python3 scripts/calib.py                      # overlays vs the app renders
node scripts/peek.mjs out/qa/peek.png 600,630,720,900   # quick quarter-res frames to look at
npm run studio                                # scrub it live
# Heavy renders are the owner's (local machine or CI), not the agent's:
npm run render:preview                        # sharp preview with audio
npm run render                                # the motion-blurred master (render.sh --blur)
# Sound (needs earshot: see video/launch/README.md, no PyTorch needed)
npm run kit && npm run cues && npm run audio
```
