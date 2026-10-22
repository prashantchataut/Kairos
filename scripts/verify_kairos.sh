#!/usr/bin/env bash
# Fast repository checks run by CI before the Gradle build.
set -euo pipefail
cd "$(dirname "$0")/.."

fail() { echo "verify: $*" >&2; exit 1; }

# Kairos ships no fixed word or quote lists: everything is generated per person.
if [ -d app/src/main/assets/content ]; then
  fail "bundled content found in assets/content; words and quotes must come from the curator"
fi

for f in bricolage fraunces fraunces_italic; do
  [ -s "app/src/main/res/font/$f.ttf" ] || fail "missing font $f.ttf"
done

# Never commit an API key: people add their own OpenRouter key in the app.
if grep -rnE "(sk-or-v1-[A-Za-z0-9]{20,}|sk-ant-[A-Za-z0-9_-]{20,}|AIza[0-9A-Za-z_-]{30,})" app/src >/dev/null; then
  fail "something that looks like an API key is committed"
fi

echo "verify: all checks passed"
