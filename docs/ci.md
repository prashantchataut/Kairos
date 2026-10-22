# CI notes

The workflow in `.github/workflows/android.yml` runs on every push: repository checks, unit and
screenshot tests, lint, a signed release build, an artifact upload and a GitHub Release.

## Re-running a build

Each release is tagged `build-<short sha>-<run number>`. Re-running a run that already published
its release fails at **Create GitHub Release** with "a release with the same tag name already
exists", even though the APK built fine (the first attempt's release already has it). To make
re-runs safe, use this in the release step:

```bash
TAG="build-${SHORT_SHA}-${GITHUB_RUN_NUMBER}-${GITHUB_RUN_ATTEMPT}"
if gh release view "$TAG" >/dev/null 2>&1; then
  gh release upload "$TAG" "$APK_NAME" "$APK_NAME.sha256" --clobber
  exit 0
fi
gh release create "$TAG" \
```

## Built-in AI keys

Add `INCEPTION_API_KEY` and `GEMINI_API_KEY` as repository secrets and pass them to the
"Build signed release APK" step's `env`. Without them the app asks people for their own
OpenRouter key. For local builds, put the same two lines in `local.properties`.
