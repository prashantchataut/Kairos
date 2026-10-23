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

## In flight: the launch film (`video/launch/`): rebuilding the frames on the app's real renders

PR #611 is merged; work now lands on `main` directly (small commits). The owner rejected the first
cut: the UI in the frames, the logo and Kai were hand-drawn look-alikes. Fix: every screen, card,
Kai and mark in the film now comes from the app's own renders (`public/app`, made by
`app/src/test/.../FilmAssetsTest.kt` with Roborazzi, imported by `scripts/import-app-assets.py`),
shown by `src/kit/App.tsx`. Live overlays (the word rewrite, Saved, Kai's frames) are set in the
app's faces at positions measured off the renders; `python3 scripts/calib.py` checks the Today
overlay against `today.jpg` (PASS: word 0 px, chip 2 px, definition 4 px).

Geometry: renders are 400 x 860 dp at 2.5 px/dp (1000 x 2150); the phone's screen canvas is the
same 400 x 860. Kai sprites are drawKai on a 220 dp canvas: a Kai the app drew at d dp is the sprite
drawn d units wide around the same centre (header Kai: 56 dp at (360, 36)).

| Act | State on the real renders |
|---|---|
| 1 Chronos, 2 Name | film-only art (clock, the name); Act 2's "Kairos" to be checked against the wordmark |
| 3 Word | DONE: today.jpg + calibrated rewrite + Saved pill + drawKai frames |
| 4 Feed | next: feed_0..3 renders, reel slides the card region, Kept button from feed_3 |
| 5 Play | todo: play_front / play_back renders, the card flips |
| 6 Letter | todo: compose_write -> letter_sealed renders |
| 7 Share | todo: the studio's real cards (bloom, bold, glow, eclipse...) |
| 8 Lockup | todo: bloom_white unfold frames, mark_white, wordmark_white |

Then: delete the hand-drawn kit (`Screens.tsx`, `ShareCards.tsx`, `Kai` in `Kit.tsx`) once unused,
`npm run check`, quarter-res peeks of every act, update `TREATMENT.md`. The owner renders the MP4.

## Exact next step

Act 4 on the feed renders (see the table). Push with
`python3 /workspace/task/tools/ghpush.py <repo-dir> prashantchataut/Kairos main` (the sandbox's git
has no credential; the script replays commits through the GitHub API via the actl connector).

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
