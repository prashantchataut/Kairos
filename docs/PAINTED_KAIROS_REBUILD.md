# Painted Kairos

Kairos is a hopeful place to learn, reflect, leave a memory, and play. The visual direction follows issue #606: watery blue pigment, translucent controls, soft paper, layered cards, and an original smiling seedling. Artwork and launcher symbols are repository-native vectors and Compose Canvas, so they scale without raster downloads.

## Daily learning

Today keeps the existing offline catalog, deterministic daily selection, Room history and spaced repetition. The new feedback engine separates words from quotes, normalizes category names, decays signals with a 30-day half-life, bounds their influence, and reverses saves when unsaved. Saving expresses interest; recalling a word in practice determines learning progress. Practice routes now serialize their real mode. Daily selections refresh when returning on another date.

## Time capsules

Capsules have their own navigation tab. Write text with photos, videos and a voice note; choose a future date; seal it; return after delivery; open attachments; favorite or reply to your past self. Date-picker days arrive at 9 am in the device timezone. Presets retain their existing same-time behavior.

Picked photos/videos are copied into private files rather than depending on temporary provider permissions. Each import accepts at most ten files, each at most 50 MB. Errors preserve the writing. Early reveal is rejected. A capsule is marked read on deliberate reveal. Reminder text hides the letter body. The screen checks its next delivery while open, and checks overdue letters on entry; notifications are optional and subject to Android settings.

Capsules are local to this installation. They are not email delivery or a new cloud backup service. Keep the app installed and preserve a device backup before resetting it. Existing backup machinery is retained, but attachment backup/restore needs a separate device-level acceptance test.

## Playroom

- **Pocket Garden:** a daily offline word seed grows the original seedling; one bloom per local calendar day is stored on-device. Seven curated puzzles cycle; this is intentionally a small game.
- **Cloud Post:** an original branching story about a snail, a sleepy star, and a letter. The game is a downloadable data-only package with an immutable source URL, a trusted SHA-256 digest, a size limit, atomic installation, offline replay and removal. No downloaded code is executed.
- **Fan-game shelf:** creator-thread links for Pokémon Unbound and Gaia. Their download hosts and permissions are controlled by their creators. Paste a direct HTTPS package link to download using Android's manager, monitor progress, and open in a compatible external app. Kairos includes no ROM, emulator, patcher, automatic APK installer or unverified mirror.

## Material and motion

The painted palette flows through Material light/dark themes. Floating controls retain glass highlights and press feedback. Paper grain and the seedling are light Canvas primitives. Navigation gives each destination a persistent text label and 48dp+ touch area. Existing reduced-motion support is retained; Android's animator scale also governs Compose transitions. Physical-device frame pacing still requires profiling.

## Build and review

A proposed CI patch makes feature branches and PRs test, lint and produce a review APK while restricting signed releases to main. GitHub rejected workflow-file writes through this session’s connector, so that patch is supplied separately and the existing workflow remains in place. No schema reset or destructive migration is introduced. Review the diff and run the acceptance checklist below before merging.

- Fresh local onboarding; daily word and quote visible offline.
- Review/new-word session buttons choose the intended mode.
- Saves persist after restart and do not increment recalled words.
- Seal a letter with photo, video and voice; restart; reveal after delivery and open each attachment.
- Attempt early reveal; verify content stays sealed.
- Run a custom date in Nepal and across DST.
- Download Cloud Post; disable network; finish all branches; remove/reinstall.
- Download a creator-provided package; test failed links and missing compatible apps.
- Check narrow screen, tablet, large text, dark mode and reduced motion.
