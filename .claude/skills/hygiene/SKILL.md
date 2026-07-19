---
name: hygiene
description: Audit Claude Code settings and repo hygiene, then report prioritized best-practice advice. Use whenever the user asks for a hygiene check, settings audit, config review, security review of their Claude setup, "scan my Claude app", "is my setup clean", permission cleanup, or wants best practices for their Claude Code environment — even if they don't say the word "hygiene". Also use after major config changes (new hooks, new MCP servers, new connectors) to re-check the baseline.
---

# Hygiene — Claude Code settings and repo audit

Run a repeatable audit of the user's Claude Code configuration and the current
repository, then deliver a prioritized advisory report. This is a **report-only**
skill: never change settings, delete files, or edit `.gitignore` unless the user
explicitly asks for the fix afterwards.

## Step 1 — Run the scanner

```bash
bash .claude/skills/hygiene/scripts/hygiene-scan.sh
```

The script prints `FACT:` (neutral evidence), `WARN:` (candidate finding), and
`SKIP:` (check unavailable) lines. It never prints secret values. It covers:
settings files and permission-rule counts, hooks and their file permissions,
MCP config, credential-shaped env var names, CLAUDE.md context weight,
installed-skill count, branch discipline, tracked binaries/key material,
per-ecosystem `.gitignore` coverage, and a low-false-positive secret-pattern
scan of tracked files.

## Step 2 — Investigate what the scanner can't judge

The script collects facts; you supply judgment. Follow up on anything the scan
surfaced, plus these checks that need reasoning rather than grep:

1. **Recent commit discipline.** `git log --oneline -10` — look for commits
   straight to the default branch, commit messages that don't match their
   content, and unrelated concerns mixed into one repo (e.g., an app committed
   into a fork of a tool).
2. **Fork drift.** If the repo is a fork, compare its version/changelog date
   against how stale it feels; flag when upstream syncing is overdue or has
   become risky because unrelated work landed on the fork's default branch.
3. **Connector/MCP surface.** Consider what connectors and MCP servers are
   attached to the session. Flag combinations that widen the
   prompt-injection blast radius — especially mailbox, calendar, or payment
   access alongside tools that read untrusted content (web, PRs, issues).
4. **Session-scoped context.** If the environment is ephemeral/remote, don't
   flag launcher-managed files (`launcher-settings.json`, managed hooks,
   proxied credential env vars) — those are platform-owned and correct.

## Step 3 — Report

Use this exact structure:

```markdown
## Hygiene report — <repo or scope> (<date>)

**Verdict:** <one sentence: clean / mostly clean with N issues / needs attention>

### Fix now
<numbered findings that risk data loss, secret exposure, or broken sessions.
For each: what, why it matters in outcome terms, and the one-line fix.>

### Fix soon
<findings that cost time or context but aren't dangerous.>

### Already good
<2-4 things that are correct and should NOT be "fixed". This prevents
well-meaning cleanup from breaking working config.>
```

Rules for the report:

- **Prioritize by consequence, not by count.** A missing keystore ignore rule
  outranks ten stylistic nits. If everything is fine, say so in one short
  report — do not pad findings to look thorough.
- **Every finding gets a concrete fix**, stated as the exact command, file
  edit, or setting change. "Consider improving X" is not a finding.
- **Distinguish user-owned from platform-owned.** Only advise on things the
  user can actually change.
- **Never echo secret values**, even partially, even redacted-with-prefix.
  Name the variable or file and stop there.
- Frame impact in outcome terms: "signing keys end up in git history and every
  clone" beats "missing gitignore entry".

## When the user says "fix it"

Apply only the specific findings they approve, smallest diff first, and re-run
the scanner afterwards to show the before/after. Never batch-fix "Fix now" and
"Fix soon" together without asking — the user may want the risky items only.
