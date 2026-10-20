# Safe Deletion Checklist

Before proposing ANY deletion, verify ALL applicable items on this checklist.

## Pre-Deletion Verification

For every item proposed for deletion, check:

### File-Level Checks

- [ ] **Not imported anywhere** — `grep -r "filename" . --include="*.{js,ts,py,go,rs,java,rb}"` returns 0 results (excluding the file itself)
- [ ] **Not referenced in config** — `grep -r "filename" . --include="*.{json,yaml,yml,toml,ini,cfg}"` returns 0 results
- [ ] **Not referenced in docs** — `grep -r "filename" . --include="*.{md,txt,rst}"` returns 0 results
- [ ] **Not referenced in tests** — `grep -r "filename" . --include="*test*"` returns 0 results
- [ ] **Not referenced in scripts** — `grep -r "filename" . --include="*.{sh,bash,ps1,bat,cmd}"` returns 0 results
- [ ] **Not referenced in CI/CD** — `grep -r "filename" .github/ .gitlab-ci.yml Jenkinsfile` returns 0 results
- [ ] **Not a git-tracked essential** — `git ls-files | grep "filename"` shows it's tracked but not essential
- [ ] **Not recently modified** — `git log -1 --format="%ai" -- filename` shows last modification > 30 days ago

### Code-Level Checks

- [ ] **Not called anywhere** — `grep -r "function_name" .` returns 0 results outside its own definition
- [ ] **Not exported and used** — If exported, check all importers
- [ ] **Not part of a public API** — Not listed in any exports, index files, or API documentation
- [ ] **Not a type definition used elsewhere** — TypeScript interfaces, Python type hints, Go structs
- [ ] **Not an error handler** — Even if currently unreachable, error handlers are safety nets
- [ ] **Not a fallback/default** — Default cases in switches, else branches, catch blocks
- [ ] **Not an i18n string** — Even if one language appears unused, translation keys should be kept

### Dependency Checks

- [ ] **Not a transitive dependency** — Removing it might break packages that depend on it
- [ ] **Not used in scripts** — Dev dependencies might be used in build scripts, not source code
- [ ] **Not a peer dependency** — Other packages might expect it to be available
- [ ] **Not used in CI/CD** — Check GitHub Actions, GitLab CI, etc.

## Absolute Never-Delete List

These items must NEVER be proposed for deletion, regardless of scan results:

1. **Test files** — `*.test.*`, `*.spec.*`, `test/`, `tests/`, `__tests__/`, `*_test.*`
2. **Type declarations** — `*.d.ts`, `types/`, `typings/`, `*.types.py`
3. **Config templates** — `.env.example`, `.env.template`, `.env.local.example`
4. **Documentation** — `README*`, `CHANGELOG*`, `CONTRIBUTING*`, `LICENSE*`, `docs/`
5. **Git hooks** — `.husky/`, `.git-hooks/`, `lefthook.yml`
6. **CI/CD config** — `.github/`, `.gitlab-ci.yml`, `Jenkinsfile`, `.circleci/`
7. **Security files** — `.htaccess`, CSP headers, security policies, `.well-known/`
8. **License headers** — Copyright notices, SPDX headers
9. **i18n files** — `locales/`, `i18n/`, `lang/`, `translations/`, `*.json` in locale dirs
10. **Gitignore entries** — Files listed in `.gitignore` are intentionally untracked
11. **Lock files** — `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`, `poetry.lock`, `Cargo.lock`
12. **Docker files** — `Dockerfile`, `docker-compose.yml` — even if appear unused, they define environments
13. **Makefiles** — `Makefile`, `makefile` — build orchestration files
14. **Editor config** — `.editorconfig`, `.prettierrc`, `.eslintrc*`, `pyproject.toml` (linting sections)

## Confidence Levels

### High Confidence (safe to propose for batch deletion)

- Empty files (0 bytes)
- Backup files (`*.bak`, `*.orig`, `*.old`, `*.swp`)
- Generated files in source directories (`__pycache__/`, `.DS_Store`, `Thumbs.db`)
- Merge conflict artifacts (`<<<<<<`, `>>>>>>`, `======`)
- Unused imports confirmed by linter with zero grep results
- Commented-out code blocks (5+ lines) with no git history of recent use

### Medium Confidence (propose with evidence, require individual review)

- Files not imported anywhere but with similar names to active files (might be used via dynamic import)
- Unused exports that might be part of a public API contract
- TODO/FIXME comments older than 6 months
- Debug logging statements (might be intentionally left for monitoring)
- Near-duplicate code blocks (might have subtle differences)

### Low Confidence (flag but recommend keeping)

- Files referenced only in test fixtures (tests need them)
- Config entries not in source code (might be environment-specific)
- Feature flags with constant values (might be toggled in production)
- Unused dependencies that are peer dependencies
- Error handlers for edge cases (safety nets)

## Commit Strategy

When the user confirms deletions, apply them in this order with separate commits:

1. `chore: remove backup and generated files` — .bak, .orig, __pycache__, .DS_Store
2. `chore: remove orphaned files` — files not imported/referenced anywhere
3. `chore: remove unused imports` — import statements with no usage
4. `chore: remove unused exports` — exported symbols not imported anywhere
5. `chore: remove commented-out code` — large blocks of commented code
6. `chore: remove debug logging` — console.log, print(), fmt.Println in production code
7. `chore: remove unused dependencies` — packages not imported anywhere
8. `chore: remove stale TODO/FIXME comments` — TODOs older than 6 months with no linked issue
9. `docs: regenerate stale documentation` — replace outdated .md files with current versions
10. `docs: link orphaned documentation` — add links from README to orphaned .md files

After each commit, run the project's test suite to verify nothing broke.

## Documentation-Specific Deletion Rules

Documentation files get SPECIAL TREATMENT — they are never just deleted. Instead:

### Classification Decision Tree

```
Is it a .md file?
├── YES → Is it README.md, CHANGELOG.md, CONTRIBUTING.md, or LICENSE?
│   ├── YES → NEVER DELETE. Regenerate if stale.
│   └── NO → Is it linked from README or docs/index?
│       ├── YES → Is the content accurate?
│       │   ├── YES → KEEP (no action needed)
│       │   └── NO → REGENERATE from codebase analysis
│       └── NO → Is the content still useful?
│           ├── YES → LINK from README, then UPDATE if stale
│           └── NO → REGENERATE or DELETE (with replacement proposed)
└── NO → Apply normal deletion rules
```

### Before Deleting Any .md File

1. **Propose a replacement** — Generate the new content from current codebase analysis
2. **Show diff** — Present old vs. new content side-by-side
3. **Get explicit confirmation** — "Delete stale X.md and replace with regenerated version? [Y/N]"
4. **Write new before deleting old** — Never leave a gap where no documentation exists
5. **Update all links** — If the filename changes, update all references to it

### Documentation Regeneration Checklist

After regenerating documentation, verify:

- [ ] Every file path mentioned actually exists (`ls` check)
- [ ] Every command mentioned actually exists in package.json/scripts
- [ ] Every version number matches current package versions
- [ ] Every internal link resolves to an existing file
- [ ] No `TODO`, `TBD`, or placeholder markers remain
- [ ] The regenerated doc is linked from README or docs index
- [ ] Code examples are syntactically valid (run through linter if possible)
- [ ] API endpoints match actual route handlers
- [ ] Configuration options match actual .env.example and source code