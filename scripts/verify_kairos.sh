#!/usr/bin/env bash
# Fast repository checks that run before Gradle in CI.
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$root"
issues=0
fail() { printf 'FAILED: %s\n' "$*" >&2; issues=$((issues + 1)); }

[[ -f gradlew ]] || fail "gradlew is missing"
[[ -f gradle/wrapper/gradle-wrapper.jar ]] || fail "Gradle wrapper JAR is missing"
[[ -f keystore/Kairos-release.jks ]] || fail "public test keystore is missing"

# Content bundles must be valid JSON with unique ids.
python3 - <<'PY'
import json, sys, pathlib
bad = 0
for name in ["words", "quotes", "kindness", "prompts"]:
    p = pathlib.Path(f"app/src/main/assets/content/{name}.json")
    data = json.loads(p.read_text(encoding="utf-8"))
    ids = [i["id"] for i in data["items"]]
    if len(ids) != len(set(ids)):
        print(f"FAILED: duplicate ids in {p}"); bad += 1
for p in [pathlib.Path("app/src/main/assets/games/catalog.json"), pathlib.Path("catalog/games.json")]:
    data = json.loads(p.read_text(encoding="utf-8"))
    for g in data["games"]:
        for key in ["id", "title", "creator", "platform", "kind", "summary", "page", "source"]:
            if key not in g:
                print(f"FAILED: {p} game {g.get('id')} missing {key}"); bad += 1
sys.exit(1 if bad else 0)
PY
[[ $? -eq 0 ]] || issues=$((issues + 1))

# No provider secrets in source.
if grep -rEn "(sk-[A-Za-z0-9]{20,}|AIza[0-9A-Za-z_-]{30,})" app/src >/dev/null 2>&1; then fail "possible API key committed"; fi

if (( issues > 0 )); then echo "$issues issue(s) found" >&2; exit 1; fi
echo "Kairos repository checks passed."
