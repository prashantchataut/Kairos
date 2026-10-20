#!/usr/bin/env bash
# Fast repository checks run by CI before the Gradle build.
set -euo pipefail
cd "$(dirname "$0")/.."

fail() { echo "verify: $*" >&2; exit 1; }

# Bundled content must be valid JSON and non-trivial.
python3 - <<'PY'
import json, sys
checks = {"words.json": 100, "quotes.json": 80, "sparks.json": 20, "capsule_prompts.json": 10}
for name, minimum in checks.items():
    data = json.load(open(f"app/src/main/assets/content/{name}"))
    if len(data) < minimum:
        sys.exit(f"verify: {name} has {len(data)} entries, expected at least {minimum}")
words = json.load(open("app/src/main/assets/content/words.json"))
seen = set()
for w in words:
    for key in ("word", "pos", "say", "def", "ex", "cat"):
        if not w.get(key):
            sys.exit(f"verify: word {w.get('word')} is missing {key}")
    if w["word"].lower() in seen:
        sys.exit(f"verify: duplicate word {w['word']}")
    seen.add(w["word"].lower())
print(f"verify: content ok ({len(words)} words)")
PY

# Fonts referenced by the type system must exist.
for f in fraunces fraunces_italic nunito caveat; do
  [ -s "app/src/main/res/font/$f.ttf" ] || fail "missing font $f.ttf"
done

# No secrets or API keys in source.
if grep -rnE "(AIza[0-9A-Za-z_-]{30,}|sk-[A-Za-z0-9]{20,})" app/src >/dev/null; then
  fail "something that looks like an API key is committed"
fi

echo "verify: all checks passed"
