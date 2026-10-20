# Verification — Garden of Time

## Completed
- `:app:assembleDebug :app:testDebugUnitTest`: successful; 279 tests across 34 suites, zero failures, errors or skips.
- `:app:lintDebug`: successful; zero errors, 747 warnings and 9 informational findings across the application. Kotlin analysis compatibility warnings also appeared in the tool output. This is not a warning-free codebase.
- `scripts/verify_kairos.sh`: passed, checking 523 Kotlin files.
- `git diff --check`: passed.
- Six primary text/surface palette pairs meet WCAG AA normal-text contrast (lowest measured ratio 5.42:1).
- New policy coverage includes discovery exhaustion, recall ordering, local-date boundaries, capsule validation and ownership. ViewModel tests cover persistence failure, attachment retention and notification failure after a successful save.

## Limits
The debug APK was built, but a usable app runtime session could not be established on this host's unaccelerated emulator. Physical-device behavior, layout screenshots, TalkBack, large font scaling, attachment picker interoperability and animation frame pacing remain unverified. No emulator image is presented as a screenshot of the rebuilt app.

Capsules use local private storage and an application date gate. Uninstall removes their data; backup/sync preservation of attachment files is not certified. Existing deeper journal/settings/study features remain accessible and are not all rebuilt.

An optional generated artwork request failed with provider `insufficient_credits`; all shipped art uses native vector/Canvas drawing. Direct APK attachment upload was rejected by the delivery service's size limit. Source and reproducible build instructions are included in the PR.

## Release follow-up
Install on a physical device and exercise welcome, discovery/save/recall, all three Word Print exports, capsule creation with several MIME types, process recreation, date unlock and attachment opening. Check dark mode, large text, TalkBack and disabled animations. Validate backup/restore before promising long-term cross-device keepsakes.
