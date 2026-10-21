# Screen blueprints — state and wiring

Current entry points (do not rename public composables; the screenshot test and nav depend on
them): `OnboardingScreen(onDone)`, `TodayScreen(openWord, openQuote, openStudio, openPractice,
openCapsule, openAiSettings, openYou)`, `LearnScreen(openWord, openQuote, openPractice,
openCurator)`, `CapsuleHome(openCompose, openLetter)`, `YouScreen(openCurator, openInterests)`,
`CuratorScreen(onBack)`, `WordScreen(id, onBack, onShare, onPractice)`, `QuoteScreen(id, onBack,
onShare)`, `ComposeLetter(onBack, onSealed)`, `LetterScreen(id, onBack, onReply)`.

## Onboarding
- Local state: `step 0..3`, `name`, `interests`, `themes`, `level`. Persist with
  `c.prefs.completeOnboarding(name, interests, themes, level)` then `onDone()`.
- Fixed top chrome: bloom 22dp + 4-petal progress. Fixed bottom CTA + quiet link.
- Beat 0 has no scroll; beats 1–3 scroll inside a weight(1) column.
- Key field is `KeyField(onConnected)`; it already validates and stores. Never block "later".
- Back handler: step > 0 → step--.
- Use `AnimatedContent` with a horizontal slide + fade; content must not animate the chrome.

## Today (`TodayViewModel`)
State: `name`, `today` (word/quote), `status` (Loading/NeedsKey/Failed), `mood`, `due`,
`nextCapsule`, `readyCapsules`, `activeDays`, `feedback`.
- Week arc: 7 days, centred on today; a filled petal = `activeDays` contains the epoch day.
- Hero word sizes: ≤8 chars 72sp, ≤11 58, ≤14 46, else 38. Quote: <70 30sp, <130 25, else 21.
- Segmented Word/Thought persists in `rememberSaveable`.
- Save action: call `vm.toggleWord/Quote`, play `BloomBurst` only when turning ON.
- Mood: `AdaptiveSlider` over `Mood.entries` (5), labels under, reply crossfades; write with
  `vm.logMood(mood)`.
- Feedback row: `WordFeedback` chips; after feedback show the reply in accent for 3.5s
  (`s.feedback`, auto-clear).
- The sky block must keep `bottom = SkyTail`; the list bottom padding is 120dp for the dock.
- Needs-key and failed states live inside the hero; keep the copy from the current version but
  in the new type scale.

## Learn (`LearnViewModel`)
- Search card: text field, submit, topic stickers (`Topics.suggestions` if present, else a
  local list), generating state = orbiting seed + "Looking for the right words…".
- Board: words and quotes in two columns; tile tint by `Topics.tint(category/theme)`; saved =
  small gold seed top-right; status line in caption.
- Keep the review banner and the segmented control.

## Capsule
- `CapsuleHome`: 2-column grid; full-span header; ready/sealed/opened sections.
- Folder objects: 128dp tall, tab 52×18dp, ghost date 28sp serif at 12% ink, stack of 2 photo
  corners. Ready cards: envelope 96×66dp with wax seal, gold glow, "Open" pill.
- `Compose.kt`: keep the existing draft/attachment logic. Replace the date preset radio row
  with `AdaptiveSlider` over `OpenIn.entries` (1 month → 5 years) and show the resolved date
  ("Opens 21 October 2027") in serif. Tint picker = 6 paint chips with a check. Seal button
  triggers the wax-stamp + bloom animation before `onSealed()`.

## You
- Portrait: 64dp paper circle, bloom behind at 20% opacity, initial in serif 30.
- Stats: three petal-shaped tiles (custom `PetalStat`) rather than glass rectangles.
- Mood month: 4×7 grid of 26dp cells; a logged day is a small bloom (5 petals) in its mood
  tint; today is an ink ring.
- Settings: paper list with mono section labels ("CURATOR", "RITUAL", "LOOK"), rows 56dp,
  hairline dividers that fade at the ends, switches in mint when on.
- Keep permission flow for reminders and the animator-scale switch (`reducedMotion`).

## Curator
- Header: "Your curator" serif 40 on sky; subtitle explains the loop in one sentence.
- Connection card: status pill + key field; when connected show the model list as paper rows
  with a coral radio, plus "Write a fresh batch" (coral) and "Remove key" (quiet).
- Generation: show a full-width progress ribbon with the bloom breathing, then a result line.
- Never expose the key; the field is password-masked and cleared after save.

## Detail (word/quote)
- Sky header with shared bounds on the word (`shared("word-$id")`), phonetic pill, three glass
  actions, save burst.
- Paper sections with mono eyebrows: MEANING, IN A SENTENCE, WHERE IT COMES FROM, REMEMBERING
  IT, TEACH YOUR CURATOR.
- Quote: serif 32 quote on sky, author, save + make-a-card; paper notes.

## Motion hooks already in the app
`LocalCalmMotion` (`K.calm`), `Motion.*`, `Modifier.pressable`, `LocalHaze`, `LocalShared`,
`shared(key)`, `Envelope`, `CardThumbnail`, `TastePicker`, `KeyField`. Reuse; don't fork.

## Screenshot test
`app/src/test/java/com/kairos/app/screens/ScreenshotTest.kt` renders real screens with seeded
data. When adding a screen/state, add a capture. Run:
`./gradlew :app:testDebugUnitTest --tests '*ScreenshotTest*' -Proborazzi.test.record=true`
Output: `app/screenshots/` (gitignored; copy finals to `docs/screenshots/`).
