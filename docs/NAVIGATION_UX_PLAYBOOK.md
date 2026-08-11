# Prody Navigation + UX Playbook

This document addresses:
1. destination inventory from `ProdyNavigation.kt`
2. route argument contracts + back-stack rules
3. unified empty/loading/error templates
4. UX flow maps for core journeys
5. analytics checkpoints for drop-off + dead-end detection

## 1) Destination Inventory (`ui/navigation/ProdyNavigation.kt`)

| Domain | Destination | Route | Args |
|---|---|---|---|
| Core | Onboarding | `onboarding` | - |
| Core | Home | `home` | - |
| Journal | JournalList | `journal` | - |
| Journal | JournalHistory | `journal/history` | - |
| Journal | NewJournalEntry | `journal/new` | - |
| Journal | JournalDetail | `journal/{entryId}` | `entryId: Long` |
| Future Message | FutureMessageList | `future_message` | - |
| Future Message | WriteMessage | `future_message/write` | - |
| Profile | Stats | `stats` | - |
| Profile | Profile | `profile` | - |
| Profile | EditProfile | `profile/edit` | - |
| Profile | BannerSelection | `profile/banner` | - |
| Profile | AchievementsCollection | `profile/achievements` | - |
| Profile | Settings | `settings` | - |
| Vocabulary | VocabularyList | `vocabulary` | - |
| Vocabulary | VocabularyDetail | `vocabulary/{wordId}` | `wordId: Long` |
| Quotes | Quotes | `quotes/{tab}` | `tab: String = quotes` |
| Wellness | Meditation | `meditation` | - |
| Social-lite | Challenges | `challenges` | - |
| Utility | Search | `search` | - |
| Quotes | IdiomDetail | `idiom/{idiomId}` | `idiomId: Long` |
| Daily engagement | WisdomCollection | `wisdom_collection` | - |
| Daily engagement | MicroJournal | `micro_journal` | - |
| Daily engagement | DailyRitual | `daily_ritual` | - |
| Daily engagement | WeeklyDigest | `weekly_digest` | - |
| Daily engagement | FutureMessageReply | `future_message/reply/{messageId}` | `messageId: Long` |
| Daily engagement | TimeCapsuleReveal | `time_capsule/reveal/{messageId}` | `messageId: Long` |
| Haven | HavenHome | `haven` | - |
| Haven | HavenChat | `haven/chat/{sessionType}?sessionId={sessionId}` | `sessionType: SessionType`, `sessionId: Long?` |
| Haven | HavenExercise | `haven/exercise/{exerciseType}` | `exerciseType: ExerciseType` |
| Learning | LearningHome | `learning` | - |
| Learning | PathDetail | `learning/path/{pathId}` | `pathId: String` |
| Learning | Lesson | `learning/path/{pathId}/lesson/{lessonId}` | `pathId: String`, `lessonId: String` |
| Deep Dive | DeepDiveHome | `deep_dive` | - |
| Deep Dive | DeepDiveSession | `deep_dive/session/{deepDiveId}` | `deepDiveId: Long` |
| Missions | Missions | `missions` | - |
| Collaborative | CollaborativeHome | `collaborative` | - |
| Collaborative | ComposeMessage | `collaborative/compose?contactId={contactId}&occasion={occasion}` | `contactId: String?`, `occasion: String?` |
| Collaborative | SentMessageDetail | `collaborative/sent/{messageId}` | `messageId: String` |
| Collaborative | ReceivedMessageDetail | `collaborative/received/{messageId}` | `messageId: String` |

---

## 2) Route Contract + Back-Stack Behavior Rules

Use these global rules consistently:

1. **Root replacement rule**: onboarding completion must replace onboarding stack (`popUpTo(inclusive=true)`).
2. **Creation-flow rule**: create/compose/reply screens are modal-like and should pop one level on save/cancel.
3. **Detail-flow rule**: detail screens push and pop back to source context.
4. **Hub-flow rule**: hub screens (Home, HavenHome, LearningHome, CollaborativeHome) are stable anchors and should avoid deep chain duplication (`launchSingleTop` when re-selecting from tabs/cards).
5. **Safe-param rule**: all optional query params must have explicit default values and nullable handling.
6. **Enum-param rule**: enum route args must fail-safe to known defaults if parsing fails.
7. **Interrupted-session rule**: therapeutic or guided session exits should always return to immediate safe context (e.g., HavenHome) rather than orphaned intermediary states.

Source-of-truth implementation is in `NavigationContracts.kt`.

---

## 3) Unified Empty / Loading / Error Templates

A shared template is now defined in `FeatureStateTemplates.kt`:

- `FeatureUiState.Loading` → `ProdyFullScreenLoading` (`"Loading your space..."`).
- `FeatureUiState.Empty` → `ProdyEmptyState` or `ProdyEmptyStateWithAction`.
- `FeatureUiState.Error` → `ErrorScreen` with optional retry.
- `FeatureUiState.Content` → screen content lambda.

### Adoption rule for all feature screens

Every screen-level state machine should map into `FeatureUiState` before rendering, so users always see:
- consistent loading language
- consistent empty-state tone
- consistent retry affordances

---

## 4) UX Flow Maps (Primary Journeys)

### A) Onboarding → Journal

`Onboarding` → `Home` → `JournalList` → `NewJournalEntry` → save → back to `JournalList` → optional `JournalDetail`

Key intent:
- minimize post-onboarding latency to first meaningful reflection.

### B) Haven check-in

`Home` → `HavenHome` → `HavenChat(sessionType)` → optional `HavenExercise(exerciseType)` → complete/exit → `HavenHome`

Key intent:
- preserve emotional safety with predictable “return to Haven home” exits.

### C) Social challenge

`Home` → `Challenges` → challenge action (join/progress) → optional `Missions` or `CollaborativeHome` for social follow-through

Key intent:
- keep challenge continuation paths visible and not dead-end on completion dialogs.

### D) Future messages

Path 1 (authoring):
`Home` → `FutureMessageList` → `WriteMessage` → save → `FutureMessageList`

Path 2 (reveal):
`FutureMessageList`/notification → `TimeCapsuleReveal(messageId)` → optional `FutureMessageReply(messageId)` → optional `NewJournalEntry`

Key intent:
- turn emotionally high-signal moments into journaling continuity.

---

## 5) Analytics Checkpoints (Drop-off + Dead-end Detection)

Track at minimum these events:

### Global navigation
- `nav_screen_view` `{route, sourceRoute, depth}`
- `nav_back_pressed` `{route, depth}`
- `nav_dead_end_detected` `{route, reason}` (e.g., no CTA, empty state with no action)

### Funnel checkpoints

1. **Onboarding funnel**
   - `onboarding_started`
   - `onboarding_completed`
   - `onboarding_to_home_latency_ms`

2. **Journal activation funnel**
   - `journal_list_opened`
   - `journal_create_started`
   - `journal_create_saved`
   - `journal_create_abandoned`

3. **Haven safety funnel**
   - `haven_home_opened`
   - `haven_session_started` `{sessionType}`
   - `haven_session_completed` / `haven_session_exited_early`
   - `haven_exercise_started` / `haven_exercise_completed`

4. **Social challenge funnel**
   - `challenges_opened`
   - `challenge_join_tapped`
   - `challenge_joined`
   - `challenge_dropoff` `{step}`

5. **Future message funnel**
   - `future_message_list_opened`
   - `future_message_write_started`
   - `future_message_saved`
   - `time_capsule_revealed`
   - `future_message_reply_started`
   - `future_message_reply_saved`
   - `reveal_to_journal_handoff`

### Dead-end heuristics

Trigger `nav_dead_end_detected` when:
- empty state has no actionable CTA,
- user performs 2+ consecutive backs within 10 seconds,
- user remains >45s on a screen with no input and exits,
- repeated bounce pattern (`A → B → A`) occurs 3x within one session.

