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

## The launch film (`video/launch/`): round two (owner's review of v1, Oct 23 2026)

Owner's notes on v1: no clock/dial shapes; the logo must match the app; no centred name reveal; hated
the serif "Kairos" at 0:08; the phone must look like a real Nothing Phone (4a), not an overlay; share
cards should show great thoughts (Seneca "Nothing, Lucilius, is ours, except time." etc., issue #606);
keep the 0:24 rising-columns transition; sound like the Pinterest ref (/workspace/refs/ref7.mp4) plus the
current track; trendy, creative, no bouncy/childish effects; render the video. (The owner mentioned
uploading real phone screenshots: none arrived in the session, so the phone is modelled from specs.)

Done (on `main`):
- `src/kit/Device.tsx`: a 3D Nothing Phone (4a) (frame depth, glass sheen, status bar, punch-hole,
  gesture bar); `kit/rig.ts` drifts its angle through the acts. `kit/Icon.tsx`: the real launcher icon
  from `ic_launcher_*.xml`. `kit/Wall.tsx`: a tilted wall of the studio's real cards.
- `FilmAssetsTest.kt` renders eight great thoughts as studio cards (`public/app/thought_*`). The app's
  Gradle/Roborazzi run works in the sandbox now (JDK 17 at /opt/jdk17, SDK at /opt/android; see
  /tmp/gradle-run.sh pattern: JAVA_TOOL_OPTIONS proxy + the mitm CA imported into the JDK cacerts).
- Act 1: night wall of cards (speed ramp, depth of field) stops dead on Seneca's card. Act 2: morning
  rises in columns; icon blooms over a horizon line, "Kairos" in Bricolage Display, phone on the
  welcome screen. Acts 3-6: the real screens on the device, no pops/rings/bounce. Act 7: a wall of
  great thoughts rises, Marcus Aurelius' card lifts. Act 8: mirrored lockup, phone turns to face us.
- Sound: groove re-scored toward ref7 (16th plucks, mallet line, pulse bass; `npm run audio` PASS).
- `renders/kairos-launch-v2-preview.mp4` (+ sheet): the first round-two render (before the Acts 1/2/8
  rework landed).

In flight / next (run subagents ONE AT A TIME: parallel launches died on 409 settlement errors):
1. Acts 3-5 (word, feed, play): break the monotony (phone centred + two side lines for 25 s): camera
   variety, macro push-ins, kinetic type, more app content (word_sonder, feed_3, Kai moods).
2. Acts 6-7 (letter, share): richer letter (compose_tuck, letter_opened, capsule), the Studio screen
   bridging into the wall of thoughts.
3. Research references with the agent-reach skill -> /workspace/task/refs/REFERENCES.md; device fixes.
4. `npm run cues && npm run audio` (the sound must follow the new picture), render the v3 preview
   (`bash scripts/render.sh Film out/preview.mp4 --preview --audio public/audio/soundtrack.wav`, ~15 min
   on 2 cores; needs PATH=/tmp/ffbin:$PATH for ffprobe), copy to `renders/`, push, post to the owner.

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
