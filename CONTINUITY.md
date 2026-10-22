# CONTINUITY

Where work on Kairos stands, for the next session (human or agent). Keep it current; push it.

## Branch and PR

- Integration branch: `redesign/round-three` → PR #611 (open). The app redesign rounds 1–3 are on it
  and its last APK build (GitHub Actions `release`) passed.
- Pushing from the agent sandbox: plain `git push` has no credential there. Use
  `task/tools/ghpush.py <repo-dir> prashantchataut/Kairos redesign/round-three` (replays local
  commits through the GitHub API via the actl connector, then soft-resets the local branch onto the
  pushed commits). The connector cannot write `.github/workflows/*` (GitHub 404s it): CI edits have
  to be pushed by the owner.

## Done (app, PR #611)

Onboarding motion, the bloom logo, 16+ share-card designs (pin / story / square), Kai's writing with
built-in Mercury and Gemini keys + on-device Gemini Nano, micro book summaries, the Feed (reel of
words, thoughts, affirmations, books, learning from keeps/shares/time), Play (random flashcards),
page transitions, the redesigned letter to future you, no "written for you". See the PR description
and `git log` for detail.

## In flight: the launch film (`video/launch/`)

Owner's ask (PR #611): a full motion launch video replicating the six references in comment
6341107956, in Kairos' design. Built with the cinetic skill (Remotion 4). Brief, references notes
and treatment: `video/launch/BRIEF.md`, `video/launch/TREATMENT.md`.

| Act | Bars | State |
|---|---|---|
| 1 Chronos (clock of days, the hand stops and turns coral) | 1–3 | built, reviewed on stills |
| 2 Name (coral flood → dot of the ı in "Kairos", "the right moment.") | 4–5 | built, reviewed on stills |
| 3 Word (phone rises, Saudade → Apricity → Hiraeth rewrite) | 6–8 | next |
| 4 Feed (reel, Keep fills coral, "It learns / what you keep.") | 9–10 | todo |
| 5 Play (flashcards fan and turn) | 11–12 | todo |
| 6 Letter (night columns, letter folds, coral seal lands) | 13–15 | todo |
| 7 Share (pin → story → square, scatter) | 16–17 | todo |
| 8 Lockup (bloom unfolds around the coral petal, wordmark, "Now on Android") | 18–19 + tail | todo |

Then: `src/sync.ts` (sound events), earshot kit + score, 9:16 and 1:1 re-layouts.

## Exact next step

Write `video/launch/src/acts/Act3.tsx` (the phone rises already moving on CUE.rise; the petal falls
from `kit/device.ts` FALL into the Save heart at CUE.heart; the word rewrites in place at
CUE.rewrite1 / rewrite2; dive at CUE.dive), add it to `src/acts/index.ts`, peek, commit, push.

## Commands

```bash
cd video/launch
npm install                                   # once
npx tsc --noEmit && npx tsx scripts/grid-check.ts src/timeline.ts
node scripts/peek.mjs out/qa/peek.png 600,630,720,900   # quick quarter-res frames to look at
npm run studio                                # scrub it live
# Heavy renders are the owner's (local machine or CI), not the agent's:
npm run render:preview                        # sharp preview with audio
npm run render                                # the motion-blurred master (render.sh --blur)
```
