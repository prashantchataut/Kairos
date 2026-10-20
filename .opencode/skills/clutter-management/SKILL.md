---
name: clutter-management
description: "Scans codebases for dead weight and proposes safe cleanup. Finds unused imports, orphaned files, dead code paths, stale config, duplicate logic, commented-out code, and other clutter. NEVER auto-deletes — proposes changes with evidence and waits for confirmation. Use when a codebase feels bloated, after removing features, before major refactors, or during spring cleaning."
license: MIT
compatibility: opencode
metadata:
  author: opencode-community
  version: "1.0.0"
  domain: codebase-hygiene
  triggers: clutter, cleanup, dead code, unused, orphaned, bloat, spring clean, tidy, remove unused, clean up
  role: auditor
  scope: whole-codebase
  output-format: proposed-changes-with-evidence
---

# Clutter Management

Scans codebases for dead weight and proposes safe cleanup. NEVER auto-deletes anything.

## Hard Rules

1. **NEVER delete without proof.** Every proposed deletion must have evidence that it's unused — search results showing zero references, import analysis confirming it's not imported anywhere, or git history showing it was replaced.
2. **NEVER auto-apply.** Always present findings as a proposal. The user confirms every change.
3. **NEVER delete tests.** Test files, test directories, and test utilities are always kept unless the feature they test is also being removed.
4. **NEVER delete config that might be environment-specific.** `.env.example`, `.env.template`, config with placeholders — these exist for a reason.
5. **NEVER delete documentation.** README, CHANGELOG, CONTRIBUTING, LICENSE, docs/ directories — always keep.
6. **When in doubt, KEEP IT.** False positives are worse than false negatives. A few unused files are harmless; deleting a critical file is catastrophic.

## Scan Phases

Run these phases in order. Each phase produces a findings list with evidence.

### Phase 1: File-Level Clutter

Scan for files that shouldn't exist:

| Category | Detection Method | Evidence Required |
|----------|-----------------|-------------------|
| Orphaned files | Not imported/referenced anywhere in the codebase | `grep -r "filename" .` returns 0 results (excluding the file itself) |
| Empty files | File size is 0 bytes | `find . -empty -type f` |
| Empty directories | No files inside | `find . -empty -type d` |
| Duplicate files | Identical content (hash match) | `md5sum` or `sha256sum` shows identical hashes |
| Backup files | `*.bak`, `*.orig`, `*.old`, `*.swp`, `*.swo`, `*~` | Filename pattern match |
| Generated files | `*.pyc`, `*.pyo`, `__pycache__/`, `.DS_Store`, `Thumbs.db` | Filename pattern match |
| Stale migration files | Migration files for features that no longer exist | Cross-reference migration targets with current code |
| Leftover merge artifacts | `*.orig` files, `HEAD` markers in files | `grep -r "<<<<<<\|>>>>>>\|======" .` |

### Phase 2: Code-Level Clutter

Scan for code that shouldn't exist:

| Category | Detection Method | Evidence Required |
|----------|-----------------|-------------------|
| Unused imports | Import statement with no usage in the file | Language-specific linter or manual grep |
| Unused exports | Exported symbol not imported anywhere | `grep -r "symbol_name" .` across all files |
| Dead code paths | Unreachable code after return/throw/break | AST analysis or manual inspection |
| Commented-out code | Large blocks of commented-out code (>3 lines) | Pattern detection |
| Console.log/print statements | Debug logging left in production code | `grep -r "console\.log\|print(" .` |
| TODO/FIXME/HACK comments | Stale or ancient TODOs | `grep -r "TODO\|FIXME\|HACK\|XXX" .` with date analysis |
| Unused variables | Declared but never read | Language-specific linter |
| Duplicate logic | Copy-pasted code blocks | Similarity analysis (3+ identical lines) |

### Phase 3: Dependency Clutter

Scan for dependencies that shouldn't exist:

| Category | Detection Method | Evidence Required |
|----------|-----------------|-------------------|
| Unused packages | Listed in package.json/requirements.txt but never imported | `grep -r "package_name" .` across source files |
| Dev dependencies in production | devDependencies imported in production code | Cross-reference devDeps with production imports |
| Duplicate functionality | Multiple packages doing the same thing (e.g., lodash + underscore) | Manual analysis of package purposes |
| Outdated dependencies | Major version behind latest | `npm outdated` or `pip list --outdated` |

### Phase 4: Config Clutter

Scan for configuration bloat:

| Category | Detection Method | Evidence Required |
|----------|-----------------|-------------------|
| Unused env vars | Referenced in .env but not in any source file | `grep -r "VAR_NAME" .` across source |
| Stale config keys | Config keys no longer read by any code | Cross-reference config schema with usage |
| Dead routes | API routes with no handler or handler with no route | Route registration analysis |
| Unused feature flags | Feature flags always true or always false | `grep -r "flag_name" .` for constant values |

### Phase 5: Documentation Clutter

Scan for documentation that is outdated, inaccurate, or should be regenerated:

| Category | Detection Method | Evidence Required |
|----------|-----------------|-------------------|
| Stale README | References files/directories/commands that no longer exist | Cross-reference README links and paths with actual filesystem |
| Outdated API docs | Endpoint descriptions that don't match current routes | Compare doc endpoints with actual route handlers |
| Orphaned .md files | Markdown files not linked from any other doc or README | `grep -r "filename.md" .` returns 0 results outside the file itself |
| Mismatched version numbers | Version in docs doesn't match package.json/Cargo.toml | Compare version strings |
| Broken links | Internal links pointing to files that don't exist | Check every `[text](link)` and `[](url)` resolves |
| Deprecated instructions | Setup/install steps that reference old tools or versions | Compare with current package.json/requirements.txt versions |
| Duplicate documentation | Same content explained in multiple .md files | Content hash comparison across docs/ |
| Unrendered template docs | Files with `TODO`, `TBD`, `[PLACEHOLDER]`, or `[TODO]` markers | `grep -r "TODO\|TBD\|\[PLACEHOLDER\]\|\[TODO\]" . --include="*.md"` |
| Changelog gaps | CHANGELOG.md missing entries for recent versions | Compare git tags with CHANGELOG entries |

### Documentation Regeneration Workflow

When stale or orphaned documentation is found, the skill proposes **regeneration** instead of just deletion. This is the key differentiator — we don't just delete old docs, we replace them with accurate ones.

#### Step 1: Identify what needs regeneration

For each flagged documentation file, classify it:

| Classification | Action |
|---------------|--------|
| **Accurate but stale** | Update with current information (version bumps, new endpoints, changed paths) |
| **Partially accurate** | Remove inaccurate sections, update the rest |
| **Wholly inaccurate** | Delete and regenerate from codebase analysis |
| **Orphaned but useful** | Link from README or main docs, then update |
| **Orphaned and useless** | Delete (with verification) |

#### Step 2: Regenerate from codebase analysis

For each file that needs regeneration, follow this process:

1. **Scan the actual codebase** — Read current routes, exports, config, package.json, etc.
2. **Extract ground truth** — What does the code ACTUALLY do right now?
3. **Generate fresh documentation** — Write new .md content that reflects reality
4. **Cross-reference** — Every claim in the new docs links to the source file/line that proves it
5. **Present for review** — Show old vs. new side-by-side, highlight what changed

#### Step 3: Documentation regeneration templates

Use these templates when regenerating common doc types:

**README.md regeneration:**
```markdown
# [Project Name]

> Regenerated from codebase analysis on YYYY-MM-DD

## Quick Start
[Install and run commands — verified against package.json scripts]

## Project Structure
[Directory tree — verified against actual filesystem]

## API Reference
[Endpoints — verified against route handlers]

## Configuration
[Env vars — verified against .env.example and source code]

## Contributing
[Verified against CONTRIBUTING.md and CI config]
```

**API docs regeneration:**
```markdown
# API Reference

> Regenerated from route handlers on YYYY-MM-DD

## Endpoints

### [METHOD] /path
- **Description:** [from handler comments]
- **Request:** [from request validation schema]
- **Response:** [from response types]
- **Auth:** [from middleware stack]
- **Source:** `[filepath:linenumber]`
```

**Architecture docs regeneration:**
```markdown
# Architecture

> Regenerated from codebase on YYYY-MM-DD

## System Overview
[From main entry points and config]

## Data Flow
[From route handlers → services → data layer]

## Dependencies
[From package.json/requirements.txt — verified still in use]

## Key Decisions
[From ADRs in docs/decisions/ if they exist]
```

#### Step 4: Verify regenerated docs

After regeneration, verify:

- [ ] Every file path mentioned actually exists (`ls` check)
- [ ] Every command mentioned actually works (dry-run where possible)
- [ ] Every version number matches current package versions
- [ ] Every link resolves (internal and external)
- [ ] No `TODO`, `TBD`, or placeholder markers remain
- [ ] The regenerated doc is linked from README or main docs index

## Verification Protocol

For EVERY proposed deletion, follow this verification checklist:

```
[ ] Is it imported anywhere?     → grep -r "name" . --include="*.{js,ts,py,go,rs,java,rb}"
[ ] Is it referenced in config?  → grep -r "name" . --include="*.{json,yaml,yml,toml,ini,cfg}"
[ ] Is it referenced in docs?     → grep -r "name" . --include="*.{md,txt,rst}"
[ ] Is it referenced in tests?    → grep -r "name" . --include="*test*"
[ ] Is it a git-tracked file?     → git ls-files | grep "name"
[ ] Is it in .gitignore?          → cat .gitignore | grep "name"
[ ] Is it referenced in scripts?  → grep -r "name" . --include="*.{sh,bash,ps1,bat,cmd}"
[ ] Is it referenced in CI/CD?    → grep -r "name" .github/ .gitlab-ci.yml Jenkinsfile
```

If ANY of these return results, the file/symbol is NOT safe to delete without further analysis.

**Minimum evidence threshold:** At least 2 independent verification methods must confirm the item is unused before proposing deletion.

## Output Format

Present findings as a structured proposal:

```markdown
# Clutter Report: [project-name]

**Scan date:** YYYY-MM-DD
**Files scanned:** N
**Potential clutter found:** M items

## High Confidence (safe to remove)

These items have been verified as unused by 2+ independent methods:

### [Category: e.g., Orphaned Files]

| # | Item | Evidence | Risk | Action |
|---|------|----------|------|--------|
| 1 | `src/deprecated/util.js` | Not imported anywhere (grep), not in git history for 6+ months | Low | Delete file |
| 2 | `import { unused } from './lib'` | `unused` not referenced in file (linter) | Low | Remove import |

## Medium Confidence (needs review)

These items are likely unused but need human judgment:

| # | Item | Evidence | Risk | Action |
|---|------|----------|------|--------|
| 3 | `src/legacy/adapter.ts` | Not imported in src/, but exists in a test fixture | Medium | Verify test is still needed |
| 4 | `TODO: refactor this` | 3 years old, no linked issue | Medium | Confirm intent before removing |

## Low Confidence (probably keep)

These items looked unused but have some references:

| # | Item | Evidence | Risk | Action |
|---|------|----------|------|--------|
| 5 | `src/utils/compat.ts` | Only imported by one test file | High | Keep — tests need it |
| 6 | `.env.staging` | Not referenced in code | High | Keep — environment-specific config |

## Documentation (regenerate, don't delete)

These docs are stale or orphaned and should be regenerated to reflect current codebase state:

| # | File | Issue | Action |
|---|------|-------|--------|
| 7 | `README.md` | References deleted files and old commands | Regenerate from codebase analysis |
| 8 | `docs/api.md` | Lists endpoints that no longer exist | Regenerate from route handlers |
| 9 | `docs/architecture.md` | Has TBD placeholders | Complete from codebase analysis |
| 10 | `docs/migration-guide.md` | Orphaned — not linked from README | Link from README, then update |

## Summary

- **Safe to remove:** X items (Y bytes)
- **Needs review:** Z items
- **Keep:** W items
- **Docs to regenerate:** N items
- **Estimated cleanup:** X% reduction in file count, Y% reduction in codebase size
```

## Execution Rules

1. **Always scan first.** Never propose changes without completing all 5 phases.
2. **Always verify.** Never propose a deletion without running the verification protocol.
3. **Always propose.** Never auto-apply changes. Present the report and wait for user confirmation.
4. **Always propose regeneration for docs.** Never delete a .md file without proposing what should replace it. Stale docs get regenerated, not removed.
5. **Batch confirmations.** Let the user confirm "High Confidence" items as a group, but require individual confirmation for "Medium Confidence" items.
6. **Preserve history.** When removing files, use `git rm` so the deletion is tracked. Never use `rm` directly.
7. **Commit per category.** Make one commit per clutter category (e.g., "chore: remove orphaned files", "chore: remove unused imports"), not one giant cleanup commit.
8. **Run tests after each batch.** After removing each category, run the project's test suite to confirm nothing broke.
9. **Regenerate docs before deleting.** For documentation clutter, generate the replacement FIRST, then delete the old version. Never leave a gap.

## What NOT to Remove

**NEVER propose removing these, even if they appear unused:**

- Test files and test directories
- Type declaration files (`.d.ts`, `.types.py`)
- Configuration templates (`.env.example`, `.env.template`)
- Git hooks (`.husky/`, `.git-hooks/`)
- CI/CD configuration (`.github/`, `.gitlab-ci.yml`, `Jenkinsfile`)
- Build configuration that might be environment-specific
- Feature flags that might be toggled in production
- Error handlers that might be triggered in edge cases
- Fallback/backup code paths
- Internationalization files (even if one language appears unused)
- License headers and copyright notices
- Security-related files (`.htaccess`, CSP headers, security configs)
- Anything in `.gitignore` that's intentionally untracked

**Documentation — SPECIAL RULES:**

- **NEVER delete documentation without proposing a replacement.** If a .md file is stale, propose regeneration instead of deletion.
- **NEVER delete README.md.** If stale, regenerate it from the current codebase.
- **NEVER delete CHANGELOG.md.** If stale, update it with missing entries from git history.
- **NEVER delete CONTRIBUTING.md or LICENSE.** These are always keepers.
- **Orphaned .md files** — if not linked from README, propose linking them first. If content is still accurate, keep and link. If inaccurate, propose regeneration.
- **Template .md files** (with TODO/TBD/PLACEHOLDER markers) — propose completing them, not deleting them.

## Integration with Other Skills

| Situation | Skill to activate |
|-----------|------------------|
| Found dead code that should be refactored instead of deleted | `code-simplification` |
| Found security issues during scan | `security-reviewer` |
| Found architectural problems | `architecture-designer` |
| Found performance bottlenecks from bloat | `performance-optimization` |
| Found test coverage gaps | `test-master` |
| About to make changes | `verification-before-completion` |
| Planning the cleanup | `writing-plans` |

## Quick Start

1. Confirm this skill is the right match (user asked about clutter, cleanup, dead code, unused files, stale docs)
2. Run all 5 scan phases (File-Level, Code-Level, Dependency, Config, Documentation)
3. Run verification protocol on every finding
4. For documentation findings, classify as: regenerate, update, or link-then-update
5. Present the clutter report with all 4 sections (High, Medium, Low, Documentation)
6. Wait for user confirmation
7. Apply code deletions in batches by category
8. Regenerate documentation before deleting old versions
9. Run tests after each batch
10. Commit per category

## Handoff Protocol

**Receives from:** Orchestrator, or direct invocation

**Hands off to:** `code-simplification` (for refactoring dead code into cleaner patterns), `security-reviewer` (if security issues found), `verification-before-completion` (before claiming cleanup is done)

## References

- **`references/detection-commands.md`** — Language-specific shell commands for detecting each type of clutter (orphaned files, unused imports, duplicate code, etc.)
- **`references/safe-deletion-checklist.md`** — Pre-deletion verification checklist, never-delete list, confidence levels, and commit strategy