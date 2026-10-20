# Detection Commands Reference

Language-specific and tool-specific commands for each clutter category.

## File-Level Detection

### Orphaned Files (not imported/referenced anywhere)

```bash
# Find all source files, then check each for references
# Replace EXT with js,ts,py,go,rs,java,rb,etc.

# Step 1: List all source files
find . -type f -name "*.EXT" ! -path "*/node_modules/*" ! -path "*/.git/*" ! -path "*/dist/*" ! -path "*/build/*" | sort

# Step 2: For each file, check if it's imported anywhere
# Example for JS/TS:
for file in $(find src -type f -name "*.ts" ! -path "*/node_modules/*"); do
  basename_no_ext=$(basename "$file" .ts)
  if ! grep -rq "$basename_no_ext" src --include="*.ts" --include="*.tsx" --include="*.js" --include="*.jsx"; then
    echo "ORPHAN: $file"
  fi
done

# Python: check for unused modules
for file in $(find . -type f -name "*.py" ! -path "*/__pycache__/*" ! -path "*/venv/*"); do
  module=$(echo "$file" | sed 's/\//./g; s/\.py$//')
  if ! grep -rq "$module\|from $module\|import $module" . --include="*.py"; then
    echo "ORPHAN: $file"
  fi
done
```

### Empty Files and Directories

```bash
# Empty files
find . -type f -empty ! -path "*/.git/*" ! -path "*/node_modules/*"

# Empty directories
find . -type d -empty ! -path "*/.git/*" ! -path "*/node_modules/*"

# Near-empty files (only whitespace/comments)
find . -type f \( -name "*.py" -o -name "*.js" -o -name "*.ts" \) ! -path "*/node_modules/*" ! -path "*/.git/*" -exec sh -c '
  lines=$(grep -cv "^\s*$\|^\s*#\|^\s*//" "$1" 2>/dev/null || echo 0)
  if [ "$lines" -le 2 ]; then echo "NEAR-EMPTY: $1 ($lines meaningful lines)"; fi
' _ {} \;
```

### Backup and Generated Files

```bash
# Backup files
find . -type f \( -name "*.bak" -o -name "*.orig" -o -name "*.old" -o -name "*.swp" -o -name "*.swo" -o -name "*~" \) ! -path "*/.git/*"

# Generated files that should be gitignored
find . -type f \( -name "*.pyc" -o -name "*.pyo" -o -name ".DS_Store" -o -name "Thumbs.db" \) ! -path "*/.git/*"
find . -type d -name "__pycache__" ! -path "*/.git/*"

# Merge conflict artifacts
grep -rl "<<<<<<\|>>>>>>\|======" . --include="*.{js,ts,py,go,rs,java,rb,md,txt,json,yaml,yml}" ! -path "*/node_modules/*" ! -path "*/.git/*"
```

### Duplicate Files

```bash
# Find files with identical content (by hash)
find . -type f ! -path "*/.git/*" ! -path "*/node_modules/*" ! -path "*/dist/*" -exec md5sum {} + 2>/dev/null | sort | uniq -w32 -D

# Find duplicate filenames (different content, same name)
find . -type f ! -path "*/.git/*" ! -path "*/node_modules/*" -printf "%f\n" | sort | uniq -d | while read name; do
  find . -type f -name "$name" ! -path "*/.git/*" ! -path "*/node_modules/*"
done
```

## Code-Level Detection

### Unused Imports

```bash
# JavaScript/TypeScript (use ESLint if available)
npx eslint --rule 'no-unused-vars: error' --rule '@typescript-eslint/no-unused-imports: error' src/

# Python (use autoflake if available)
autoflake --check --remove-all-unused-imports -r src/

# Go
go vet ./...

# Rust
cargo check 2>&1 | grep "warning: unused import"
```

### Unused Exports

```bash
# JavaScript/TypeScript
# Find all exports, then check if they're imported anywhere
grep -rn "export " src/ --include="*.ts" --include="*.tsx" | while read line; do
  symbol=$(echo "$line" | grep -oP "(?:export (?:default |const |let |var |function |class |interface |type |enum ))\K\w+" || echo "")
  if [ -n "$symbol" ]; then
    count=$(grep -r "$symbol" src/ --include="*.ts" --include="*.tsx" | grep -v "export " | wc -l)
    if [ "$count" -le 1 ]; then echo "UNUSED EXPORT: $symbol ($line)"; fi
  fi
done
```

### Commented-Out Code

```bash
# Find blocks of 3+ consecutive commented-out lines
awk '/^[[:space:]]*(\/\/|#|\/\*|\*|--)/ { count++; if (count >= 3) print FILENAME":"NR": possible commented-out code block" } !/^[[:space:]]*(\/\/|#|\/\*|\*|--)/ { count=0 }' $(find . -type f \( -name "*.js" -o -name "*.ts" -o -name "*.py" -o -name "*.go" -o -name "*.rs" -o -name "*.java" -o -name "*.rb" \) ! -path "*/node_modules/*" ! -path "*/.git/*")
```

### Debug Logging

```bash
# Console.log statements
grep -rn "console\.log\|console\.debug\|console\.warn\|console\.error" src/ --include="*.js" --include="*.ts" --include="*.jsx" --include="*.tsx" ! -path "*/node_modules/*"

# Python print statements (excluding __main__ blocks)
grep -rn "print(" src/ --include="*.py" ! -path "*/__pycache__/*" | grep -v "if __name__"

# Go fmt.Println
grep -rn "fmt\.Println\|fmt\.Printf\|log\.Println\|log\.Printf" . --include="*.go"

# Rust println!
grep -rn "println!\|dbg!" src/ --include="*.rs"
```

### TODO/FIXME/HACK Comments

```bash
# Find all TODO/FIXME/HACK comments with context
grep -rn "TODO\|FIXME\|HACK\|XXX\|OPTIMIZE\|REFACTOR" . --include="*.{js,ts,py,go,rs,java,rb}" ! -path "*/node_modules/*" ! -path "*/.git/*" ! -path "*/dist/*"

# Find TODOs older than 6 months (requires git)
for file in $(grep -rl "TODO" . --include="*.{js,ts,py,go,rs}" ! -path "*/node_modules/*" ! -path "*/.git/*"); do
  last_modified=$(git log -1 --format="%ai" -- "$file" 2>/dev/null || echo "unknown")
  echo "$last_modified $file"
done | sort
```

## Dependency Detection

### Unused Packages

```bash
# Node.js: check package.json dependencies against actual imports
for dep in $(jq -r '.dependencies | keys[]' package.json 2>/dev/null); do
  if ! grep -rq "\"$dep\"\|'$dep'\|from '$dep'\|from \"$dep\"" src/ --include="*.js" --include="*.ts" --include="*.jsx" --include="*.tsx" 2>/dev/null; then
    echo "UNUSED DEP: $dep"
  fi
done

# Python: check requirements.txt against actual imports
for dep in $(cut -d'=' -f1 requirements.txt 2>/dev/null | tr -d ' '); do
  pkg=$(echo "$dep" | tr '-' '_')
  if ! grep -rq "import $pkg\|from $pkg" . --include="*.py" ! -path "*/venv/*" ! -path "*/.git/*" 2>/dev/null; then
    echo "UNUSED DEP: $dep"
  fi
done
```

## Config Detection

### Unused Environment Variables

```bash
# Find env vars referenced in .env but not in source
for var in $(grep -oP "^[A-Z_]+" .env 2>/dev/null); do
  if ! grep -rq "$var" . --include="*.{js,ts,py,go,rs,java,rb,yaml,yml,json,toml}" ! -path "*/.git/*" ! -path "*/node_modules/*" ! -path "*/.env*"; then
    echo "UNUSED ENV: $var"
  fi
done
```

### Dead Routes

```bash
# Express.js: find registered routes
grep -rn "app\.\(get\|post\|put\|delete\|patch\|use\)" src/ --include="*.js" --include="*.ts"

# Next.js: find API routes
find src/app -type f -name "route.ts" -o -name "route.js"

# Django: find URL patterns
grep -rn "path(\|re_path(" --include="*.py" urls/
```

## Verification Commands

### Cross-Reference Check

```bash
# Check if a file/symbol is referenced ANYWHERE in the project
# Usage: verify-reference "symbol_or_filename"

verify_reference() {
  target="$1"
  echo "=== Checking references to: $target ==="
  echo ""
  echo "--- Source files ---"
  grep -r "$target" . --include="*.{js,ts,jsx,tsx,py,go,rs,java,rb,vue,svelte}" ! -path "*/node_modules/*" ! -path "*/.git/*" ! -path "*/dist/*" ! -path "*/build/*" 2>/dev/null | head -20
  echo ""
  echo "--- Config files ---"
  grep -r "$target" . --include="*.{json,yaml,yml,toml,ini,cfg,env,conf}" ! -path "*/.git/*" ! -path "*/node_modules/*" 2>/dev/null | head -20
  echo ""
  echo "--- Docs ---"
  grep -r "$target" . --include="*.{md,txt,rst,adoc}" ! -path "*/.git/*" ! -path "*/node_modules/*" 2>/dev/null | head -20
  echo ""
  echo "--- Tests ---"
  grep -r "$target" . --include="*test*" --include="*spec*" ! -path "*/.git/*" ! -path "*/node_modules/*" 2>/dev/null | head -20
  echo ""
  echo "--- Scripts/CI ---"
  grep -r "$target" . --include="*.{sh,bash,ps1,bat,cmd}" .github/ .gitlab-ci.yml Jenkinsfile Makefile Dockerfile 2>/dev/null | head -20
}
```

## Documentation Clutter Detection

### Stale README Detection

```bash
# Find file/directory references in README that don't exist
grep -oP '\[.*?\]\((.*?)\)' README.md | grep -oP '(?<=\().*?(?=\))' | while read link; do
  # Skip external URLs
  if [[ "$link" =~ ^https?:// ]]; then continue; fi
  # Check if local path exists
  if [ ! -e "$link" ]; then echo "STALE LINK: $link"; fi
done

# Find command references in README that don't exist in package.json scripts
grep -oP '`(npm|yarn|pnpm) (run )?\K\w+' README.md | while read cmd; do
  if ! grep -q "\"$cmd\"" package.json 2>/dev/null; then echo "STALE COMMAND: $cmd"; fi
done

# Find version references that don't match current version
current_version=$(jq -r '.version' package.json 2>/dev/null)
grep -n "version.*[0-9]\+\.[0-9]\+\.[0-9]\+" README.md | grep -v "$current_version"
```

### Orphaned .md Files

```bash
# Find .md files not linked from any other doc or source file
for mdfile in $(find . -name "*.md" ! -path "*/.git/*" ! -path "*/node_modules/*"); do
  basename=$(basename "$mdfile")
  # Check if this file is referenced from anywhere else
  ref_count=$(grep -r "$mdfile\|$basename" . --include="*.{md,html,js,ts,py,go,rs,yaml,yml,json}" ! -path "*/.git/*" ! -path "*/node_modules/*" 2>/dev/null | grep -v "^$mdfile:" | wc -l)
  if [ "$ref_count" -eq 0 ]; then
    echo "ORPHANED DOC: $mdfile (0 external references)"
  fi
done
```

### Broken Internal Links

```bash
# Check all internal links in .md files
for mdfile in $(find . -name "*.md" ! -path "*/.git/*" ! -path "*/node_modules/*"); do
  grep -oP '\[.*?\]\((.*?)\)' "$mdfile" | grep -oP '(?<=\().*?(?=\))' | while read link; do
    # Skip external URLs and anchors
    if [[ "$link" =~ ^https?:// ]] || [[ "$link" =~ ^# ]]; then continue; fi
    # Resolve relative path
    dir=$(dirname "$mdfile")
    resolved="$dir/$link"
    if [ ! -e "$resolved" ]; then
      echo "BROKEN LINK: $mdfile -> $link (resolved: $resolved)"
    fi
  done
done
```

### Template/Placeholder Detection

```bash
# Find docs with TODO/TBD/PLACEHOLDER markers
grep -rn "TODO\|TBD\|\[PLACEHOLDER\]\|\[TODO\]\|FILL IN\|FILLME\|XXX" . --include="*.md" ! -path "*/.git/*" ! -path "*/node_modules/*"
```

### Changelog Gap Detection

```bash
# Compare git tags with CHANGELOG entries (if CHANGELOG.md exists)
if [ -f CHANGELOG.md ]; then
  for tag in $(git tag --sort=-version:refname | head -20); do
    if ! grep -q "$tag" CHANGELOG.md; then
      echo "CHANGELOG GAP: $tag not in CHANGELOG.md"
    fi
  done
fi
```

### Outdated API Docs

```bash
# Find documented endpoints that don't exist in routes
# Example for Express.js:
grep -oP '(?:GET|POST|PUT|DELETE|PATCH)\s+\K/[^\s)]+' docs/api.md 2>/dev/null | while read endpoint; do
  if ! grep -rq "$endpoint" src/ --include="*.js" --include="*.ts"; then
    echo "STALE ENDPOINT DOC: $endpoint not found in routes"
  fi
done

# Find routes that aren't documented
# Example for Express.js:
grep -rn "app\.\(get\|post\|put\|delete\|patch\)('"'"'"\/[^'"'"']*'"'"'" src/ --include="*.js" --include="*.ts" | grep -oP '"'"'"\/[^'"'"']+'"'"'"' | while read route; do
  if ! grep -q "$route" docs/api.md 2>/dev/null; then
    echo "UNDOCUMENTED ROUTE: $route"
  fi
done
```