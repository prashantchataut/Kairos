# Quality Gates: Test Matrix, Smoke Gates, and Coverage/Risk Controls

## 1) Feature-by-Feature Test Matrix

| Feature | Unit Tests | Integration Tests (Repository ↔ DAO ↔ Migration) | Compose UI Tests | Release Smoke Gate |
|---|---|---|---|---|
| Journal | Repository/business rules | Legacy DB row survives migration and is readable/writable through `JournalRepositoryImpl` | Entry actions, notification CTA/dismiss behavior | Data persistence across DB reopen |
| Haven | Session/memory services | Migration path preserving haven-related tables and cross-feature compatibility checks | Therapeutic prompt/suggestion rendering | Onboarding and baseline startup stability |
| Social | Circle/repository logic | DAO/repository consistency after schema upgrades | Circle journey and destructive paths (leave/remove) | Sync recovery on unstable networks |
| AI | Prompt/context builders and policy checks | AI metadata persistence after migration | Error + retry flows surfaced in UI | Startup + onboarding health for AI hints |
| Widgets | Update throttling and scheduling rules | DB-backed widget data state survives migration | Widget configuration journey (where testable in Compose) | Startup stability with widget providers enabled |
| Notifications | Message/content generation | Notification history persistence after migration | Banner render, CTA, and dismiss/error states | Smoke check for notifications pipeline initialization |
| DeepDive | Session planner/domain logic | DeepDive entities/DAO survive migration + repository CRUD | Primary deep-dive journey + abandon/cancel flow | App can recover and persist interrupted session state |
| Learning | Path recommendation/progress logic | Learning entities migrate + repository reads/writes | Lesson completion and failure retry path | Persistence and restart continuity |
| Wrapped | Summary/calculation logic | Yearly wrapped entities survive migration + repository access | Wrapped summary navigation + exit path | Startup/persistence verification for wrapped data |

## 2) Release-Gating Smoke Suite (must pass before release)

1. **Startup smoke**: app process can initialize persistence layer and DAOs.
2. **Onboarding smoke**: onboarding hint state defaults correctly and can transition to completed.
3. **Sync recovery smoke**: connectivity decision logic allows safe recovery decisions after degraded network.
4. **Data persistence smoke**: data written before restart remains available after reopening storage.

## 3) Minimum Coverage Thresholds + Critical-Path Risk Scores

| Module | Min Line Coverage | Min Branch Coverage | Critical-Path Risk (1-5) | Release Policy |
|---|---:|---:|---:|---|
| `data.repository` | 80% | 70% | 5 | Block release if below threshold |
| `data.local` (DAO/entity/migration touchpoints) | 78% | 65% | 5 | Block release if below threshold |
| `ui.screens.journal` | 70% | 60% | 4 | Block if critical-path tests fail |
| `ui.components` | 65% | 55% | 3 | Warn if below; block for critical components |
| `data.onboarding` | 75% | 65% | 4 | Block release if smoke fails |
| `data.sync`/network gating logic | 75% | 65% | 5 | Block release on smoke or threshold failure |

### Risk scoring guidance
- **5 (Highest):** Directly impacts user trust/data safety (migration integrity, persistence, sync correctness).
- **4:** High-frequency journeys with strong retention impact (onboarding, journal core paths).
- **3:** Important UX surfaces where failures are noticeable but recoverable.
- **1-2:** Nice-to-have or isolated auxiliary paths.
