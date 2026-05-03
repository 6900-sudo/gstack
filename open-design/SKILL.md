---
name: open-design
preamble-tier: 2
version: 1.0.0
description: |
  Install and configure Open Design — a self-hosted, BYOK alternative to Claude Design.
  Clones nexu-io/open-design, detects existing coding-agent CLIs on your system
  (Claude Code, Cursor, Gemini CLI, etc.), sets up API keys, starts the local daemon
  and Next.js web app, and writes the project URL to CLAUDE.md for future sessions.
  Use when: "setup open-design", "install open-design", "run open-design locally",
  "configure open design", "self-host claude design".
triggers:
  - setup open design
  - install open design
  - run open design
allowed-tools:
  - Bash
  - Read
  - Write
  - Edit
  - AskUserQuestion
---
<!-- AUTO-GENERATED from SKILL.md.tmpl — do not edit directly -->
<!-- Regenerate: bun run gen:skill-docs -->

## Preamble (run first)

```bash
_UPD=$(~/.claude/skills/gstack/bin/gstack-update-check 2>/dev/null || .claude/skills/gstack/bin/gstack-update-check 2>/dev/null || true)
[ -n "$_UPD" ] && echo "$_UPD" || true
mkdir -p ~/.gstack/sessions
touch ~/.gstack/sessions/"$PPID"
_SESSIONS=$(find ~/.gstack/sessions -mmin -120 -type f 2>/dev/null | wc -l | tr -d ' ')
find ~/.gstack/sessions -mmin +120 -type f -exec rm {} + 2>/dev/null || true
_PROACTIVE=$(~/.claude/skills/gstack/bin/gstack-config get proactive 2>/dev/null || echo "true")
_PROACTIVE_PROMPTED=$([ -f ~/.gstack/.proactive-prompted ] && echo "yes" || echo "no")
_BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
echo "BRANCH: $_BRANCH"
_SKILL_PREFIX=$(~/.claude/skills/gstack/bin/gstack-config get skill_prefix 2>/dev/null || echo "false")
echo "PROACTIVE: $_PROACTIVE"
echo "PROACTIVE_PROMPTED: $_PROACTIVE_PROMPTED"
echo "SKILL_PREFIX: $_SKILL_PREFIX"
source <(~/.claude/skills/gstack/bin/gstack-repo-mode 2>/dev/null) || true
REPO_MODE=${REPO_MODE:-unknown}
echo "REPO_MODE: $REPO_MODE"
_LAKE_SEEN=$([ -f ~/.gstack/.completeness-intro-seen ] && echo "yes" || echo "no")
echo "LAKE_INTRO: $_LAKE_SEEN"
_TEL=$(~/.claude/skills/gstack/bin/gstack-config get telemetry 2>/dev/null || true)
_TEL_PROMPTED=$([ -f ~/.gstack/.telemetry-prompted ] && echo "yes" || echo "no")
_TEL_START=$(date +%s)
_SESSION_ID="$$-$(date +%s)"
echo "TELEMETRY: ${_TEL:-off}"
echo "TEL_PROMPTED: $_TEL_PROMPTED"
_EXPLAIN_LEVEL=$(~/.claude/skills/gstack/bin/gstack-config get explain_level 2>/dev/null || echo "default")
if [ "$_EXPLAIN_LEVEL" != "default" ] && [ "$_EXPLAIN_LEVEL" != "terse" ]; then _EXPLAIN_LEVEL="default"; fi
echo "EXPLAIN_LEVEL: $_EXPLAIN_LEVEL"
_QUESTION_TUNING=$(~/.claude/skills/gstack/bin/gstack-config get question_tuning 2>/dev/null || echo "false")
echo "QUESTION_TUNING: $_QUESTION_TUNING"
mkdir -p ~/.gstack/analytics
if [ "$_TEL" != "off" ]; then
echo '{"skill":"open-design","ts":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'","repo":"'$(basename "$(git rev-parse --show-toplevel 2>/dev/null)" 2>/dev/null || echo "unknown")'"}'  >> ~/.gstack/analytics/skill-usage.jsonl 2>/dev/null || true
fi
for _PF in $(find ~/.gstack/analytics -maxdepth 1 -name '.pending-*' 2>/dev/null); do
  if [ -f "$_PF" ]; then
    if [ "$_TEL" != "off" ] && [ -x "~/.claude/skills/gstack/bin/gstack-telemetry-log" ]; then
      ~/.claude/skills/gstack/bin/gstack-telemetry-log --event-type skill_run --skill _pending_finalize --outcome unknown --session-id "$_SESSION_ID" 2>/dev/null || true
    fi
    rm -f "$_PF" 2>/dev/null || true
  fi
  break
done
eval "$(~/.claude/skills/gstack/bin/gstack-slug 2>/dev/null)" 2>/dev/null || true
_LEARN_FILE="${GSTACK_HOME:-$HOME/.gstack}/projects/${SLUG:-unknown}/learnings.jsonl"
if [ -f "$_LEARN_FILE" ]; then
  _LEARN_COUNT=$(wc -l < "$_LEARN_FILE" 2>/dev/null | tr -d ' ')
  echo "LEARNINGS: $_LEARN_COUNT entries loaded"
  if [ "$_LEARN_COUNT" -gt 5 ] 2>/dev/null; then
    ~/.claude/skills/gstack/bin/gstack-learnings-search --limit 3 2>/dev/null || true
  fi
else
  echo "LEARNINGS: 0"
fi
~/.claude/skills/gstack/bin/gstack-timeline-log '{"skill":"open-design","event":"started","branch":"'"$_BRANCH"'","session":"'"$_SESSION_ID"'"}' 2>/dev/null &
_HAS_ROUTING="no"
if [ -f CLAUDE.md ] && grep -q "## Skill routing" CLAUDE.md 2>/dev/null; then
  _HAS_ROUTING="yes"
fi
_ROUTING_DECLINED=$(~/.claude/skills/gstack/bin/gstack-config get routing_declined 2>/dev/null || echo "false")
echo "HAS_ROUTING: $_HAS_ROUTING"
echo "ROUTING_DECLINED: $_ROUTING_DECLINED"
_VENDORED="no"
if [ -d ".claude/skills/gstack" ] && [ ! -L ".claude/skills/gstack" ]; then
  if [ -f ".claude/skills/gstack/VERSION" ] || [ -d ".claude/skills/gstack/.git" ]; then
    _VENDORED="yes"
  fi
fi
echo "VENDORED_GSTACK: $_VENDORED"
echo "MODEL_OVERLAY: claude"
_CHECKPOINT_MODE=$(~/.claude/skills/gstack/bin/gstack-config get checkpoint_mode 2>/dev/null || echo "explicit")
_CHECKPOINT_PUSH=$(~/.claude/skills/gstack/bin/gstack-config get checkpoint_push 2>/dev/null || echo "false")
echo "CHECKPOINT_MODE: $_CHECKPOINT_MODE"
echo "CHECKPOINT_PUSH: $_CHECKPOINT_PUSH"
[ -n "$OPENCLAW_SESSION" ] && echo "SPAWNED_SESSION: true" || true
```

If `PROACTIVE` is `"false"`, do not proactively suggest gstack skills AND do not
auto-invoke skills based on conversation context. Only run skills the user explicitly
types (e.g., /qa, /ship). If you would have auto-invoked a skill, instead briefly say:
"I think /skillname might help here — want me to run it?" and wait for confirmation.
The user opted out of proactive behavior.

If `SKILL_PREFIX` is `"true"`, the user has namespaced skill names. When suggesting
or invoking other gstack skills, use the `/gstack-` prefix (e.g., `/gstack-qa` instead
of `/qa`, `/gstack-ship` instead of `/ship`). Disk paths are unaffected — always use
`~/.claude/skills/gstack/[skill-name]/SKILL.md` for reading skill files.

If output shows `UPGRADE_AVAILABLE <old> <new>`: read `~/.claude/skills/gstack/gstack-upgrade/SKILL.md` and follow the "Inline upgrade flow" (auto-upgrade if configured, otherwise AskUserQuestion with 4 options, write snooze state if declined).

If output shows `JUST_UPGRADED <from> <to>` AND `SPAWNED_SESSION` is NOT set: tell
the user "Running gstack v{to} (just updated!)" and then check for new features to
surface.

If `SPAWNED_SESSION` is `"true"`, you are running inside a session spawned by an
AI orchestrator. In spawned sessions:
- Do NOT use AskUserQuestion for interactive prompts. Auto-choose the recommended option.
- Do NOT run upgrade checks, telemetry prompts, routing injection, or lake intro.
- Focus on completing the task and reporting results via prose output.
- End with a completion report: what shipped, decisions made, anything uncertain.

## Model-Specific Behavioral Patch (claude)

**Todo-list discipline.** Mark each task complete individually as you finish it.

**Think before heavy actions.** For complex operations, briefly state your approach before executing.

**Dedicated tools over Bash.** Prefer Read, Edit, Write, Glob, Grep over shell equivalents.

## Voice

You are GStack. Lead with the point. Sound like someone who shipped code today.

## AskUserQuestion Format

1. **Re-ground:** State the project, current branch, and current task.
2. **Simplify (ELI10):** Explain what's happening in plain English.
3. **Recommend:** Every question ends with `RECOMMENDATION: Choose [X] because [reason]`.
4. **Score completeness** when options differ in coverage.
5. **Options:** Lettered: `A) ... B) ... C) ...`

## Completeness Principle — Boil the Lake

Always recommend the complete option. AI makes completeness near-free.

## Completion Status Protocol

- **DONE** — All steps completed. Evidence provided.
- **DONE_WITH_CONCERNS** — Completed with issues. List each.
- **BLOCKED** — Cannot proceed. State what's blocking.
- **NEEDS_CONTEXT** — Missing information. State exactly what's needed.

## Telemetry (run last)

```bash
_TEL_END=$(date +%s)
_TEL_DUR=$(( _TEL_END - _TEL_START ))
rm -f ~/.gstack/analytics/.pending-"$_SESSION_ID" 2>/dev/null || true
~/.claude/skills/gstack/bin/gstack-timeline-log '{"skill":"open-design","event":"completed","branch":"'$(git branch --show-current 2>/dev/null || echo unknown)'","outcome":"OUTCOME","duration_s":"'"$_TEL_DUR"'","session":"'"$_SESSION_ID"'"}' 2>/dev/null || true
if [ "$_TEL" != "off" ]; then
echo '{"skill":"open-design","duration_s":"'"$_TEL_DUR"'","outcome":"OUTCOME","browse":"false","session":"'"$_SESSION_ID"'","ts":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"}' >> ~/.gstack/analytics/skill-usage.jsonl 2>/dev/null || true
fi
if [ "$_TEL" != "off" ] && [ -x ~/.claude/skills/gstack/bin/gstack-telemetry-log ]; then
  ~/.claude/skills/gstack/bin/gstack-telemetry-log \
    --skill "open-design" --duration "$_TEL_DUR" --outcome "OUTCOME" \
    --used-browse "false" --session-id "$_SESSION_ID" 2>/dev/null &
fi
```

Replace `OUTCOME` with success/error/abort based on workflow result.

# /setup-open-design — Install & Configure Open Design

You are helping the user get Open Design running locally. Open Design is a self-hosted
alternative to Claude Design that works with any coding-agent CLI already on the system —
Claude Code, Cursor, Gemini CLI, and more. No Anthropic account required unless the user
wants to use the Anthropic API endpoint directly.

After this skill runs, the user has a running Open Design instance at `http://localhost:3000`
and the URL is saved to CLAUDE.md.

## User-invocable
When the user types `/setup-open-design`, run this skill.

## Instructions

### Step 1: Check if Open Design is already installed

```bash
# Check for existing installation
ls -la ~/.od/ 2>/dev/null || echo "NO_OD_DIR"
ls -la ~/open-design/ 2>/dev/null || echo "NO_HOME_CLONE"
# Check if daemon is already running
curl -sf http://localhost:7701/health 2>/dev/null && echo "DAEMON_RUNNING" || echo "DAEMON_NOT_RUNNING"
# Check if web is already running
curl -sf http://localhost:3000 -o /dev/null -w "WEB_%{http_code}" 2>/dev/null || echo "WEB_NOT_RUNNING"
# Check CLAUDE.md config
grep -A 5 "## Open Design" CLAUDE.md 2>/dev/null || echo "NO_OD_CONFIG"
```

If Open Design is already running (daemon + web), show the existing config and ask:

- **Context:** Open Design is already installed and running.
- **RECOMMENDATION:** Choose C if everything is working correctly.
- A) Reinstall from scratch
- B) Restart services only
- C) Done — it's already running

If the user picks B, jump to Step 5 (Start Services).
If the user picks C, stop.

### Step 2: Check prerequisites

```bash
# Node.js
node --version 2>/dev/null || echo "NO_NODE"

# pnpm
pnpm --version 2>/dev/null || echo "NO_PNPM"

# git
git --version 2>/dev/null || echo "NO_GIT"

# Detect installed coding-agent CLIs
echo "=== CLI DETECTION ==="
which claude 2>/dev/null && claude --version 2>/dev/null | head -1 && echo "CLI:claude-code" || echo "CLI:claude-code:NOT_FOUND"
which cursor 2>/dev/null && echo "CLI:cursor" || echo "CLI:cursor:NOT_FOUND"
which gemini 2>/dev/null && gemini --version 2>/dev/null | head -1 && echo "CLI:gemini-cli" || echo "CLI:gemini-cli:NOT_FOUND"
which codex 2>/dev/null && echo "CLI:codex" || echo "CLI:codex:NOT_FOUND"
which aider 2>/dev/null && echo "CLI:aider" || echo "CLI:aider:NOT_FOUND"
which amp 2>/dev/null && echo "CLI:amp" || echo "CLI:amp:NOT_FOUND"
[ -n "$OPENAI_API_KEY" ] && echo "CLI:openai-api-key:SET" || echo "CLI:openai-api-key:NOT_SET"
[ -n "$ANTHROPIC_API_KEY" ] && echo "CLI:anthropic-api-key:SET" || echo "CLI:anthropic-api-key:NOT_SET"
echo "=== END CLI DETECTION ==="
```

**If Node.js is missing**, use AskUserQuestion:

> Open Design needs Node.js (v18+) and pnpm. They're not installed.

Options:
- A) Install via nvm: `curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.0/install.sh | bash && source ~/.nvm/nvm.sh && nvm install 20 && npm i -g pnpm`
- B) Install via Homebrew (macOS): `brew install node pnpm`
- C) I'll install manually — tell me what's needed and I'll come back

If A or B: run the install command, then re-run the version checks.
If C: print the manual install instructions and stop.

**If pnpm is missing (Node present):**
```bash
npm install -g pnpm
```

### Step 3: Clone Open Design

```bash
[ -d ~/open-design ] && echo "HOME_CLONE_EXISTS" || echo "HOME_CLONE_MISSING"
```

If `HOME_CLONE_EXISTS`, ask:

- **Context:** A clone already exists at `~/open-design`.
- **RECOMMENDATION:** Choose A to reuse the existing clone.
- A) Use the existing clone at ~/open-design
- B) Re-clone fresh (removes and replaces ~/open-design)
- C) Install in a custom path (I'll type it)

If B:
```bash
rm -rf ~/open-design
git clone https://github.com/nexu-io/open-design.git ~/open-design
```

If `HOME_CLONE_MISSING` or after fresh cloning:
```bash
git clone https://github.com/nexu-io/open-design.git ~/open-design
```

Show the last 3 commits:
```bash
git -C ~/open-design log --oneline -3
```

### Step 4: Install dependencies and configure

```bash
cd ~/open-design && pnpm install
```

If install fails, show the full error output and stop with BLOCKED status.

**Configure API keys**

```bash
[ -n "$ANTHROPIC_API_KEY" ] && echo "ANTHROPIC: set" || echo "ANTHROPIC: not set"
[ -n "$OPENAI_API_KEY" ] && echo "OPENAI: set" || echo "OPENAI: not set"
[ -f ~/open-design/.env ] && echo "ENV_FILE: exists" || echo "ENV_FILE: missing"
[ -f ~/open-design/.env.example ] && grep -v '#' ~/open-design/.env.example | grep '=' | head -10
```

Use AskUserQuestion:

> Open Design is BYOK (bring your own key). It auto-detects coding-agent CLIs first.
> If Claude Code is installed, no API key is needed. Keys are only required for the
> direct API fallback.

Options (show only relevant ones based on Step 2 detection):
- A) Use Claude Code CLI only — no API key needed  [show only if claude-code was found]
- B) Add Anthropic API key (direct API, fastest path without Claude Code)
- C) Add OpenAI-compatible API key (works with any OpenAI API endpoint)
- D) Skip — I'll configure keys manually in ~/open-design/.env

If B: prompt via AskUserQuestion ("Paste your Anthropic API key:"),
then write `ANTHROPIC_API_KEY=<value>` to `~/open-design/.env`.
If C: prompt for key and base URL, write to `~/open-design/.env`.
If D: show the .env.example path and continue.

**NEVER print API key values. Only confirm: "Key saved to ~/open-design/.env".**

### Step 5: Start services

Open Design runs two processes:
1. **Daemon** (port 7701) — agent spawning, skill registry, SQLite persistence
2. **Web** (port 3000) — Next.js frontend

```bash
lsof -i :7701 2>/dev/null | grep LISTEN && echo "PORT_7701_IN_USE" || echo "PORT_7701_FREE"
lsof -i :3000 2>/dev/null | grep LISTEN && echo "PORT_3000_IN_USE" || echo "PORT_3000_FREE"
```

If a port is in use by a non-Open-Design process, ask before killing it.

Start the daemon:
```bash
cd ~/open-design
pnpm tools-dev run daemon > /tmp/od-daemon.log 2>&1 &
echo "DAEMON_PID: $!"
for i in $(seq 1 30); do
  curl -sf http://localhost:7701/health 2>/dev/null && echo "DAEMON_READY" && break
  [ $i -eq 30 ] && echo "DAEMON_TIMEOUT"
  sleep 1
done
```

If `DAEMON_TIMEOUT`: `tail -30 /tmp/od-daemon.log` then stop with BLOCKED.

Start the web app:
```bash
cd ~/open-design
pnpm tools-dev run web > /tmp/od-web.log 2>&1 &
echo "WEB_PID: $!"
for i in $(seq 1 30); do
  status=$(curl -sf http://localhost:3000 -o /dev/null -w "%{http_code}" 2>/dev/null)
  echo "$status" | grep -qE '^(200|301|302)$' && echo "WEB_READY" && break
  [ $i -eq 30 ] && echo "WEB_TIMEOUT"
  sleep 1
done
```

If `WEB_TIMEOUT`: `tail -30 /tmp/od-web.log` then stop.

### Step 6: Verify and write config

```bash
curl -sf http://localhost:7701/health 2>/dev/null
curl -sf http://localhost:3000 -o /dev/null -w "WEB_HTTP_%{http_code}" 2>/dev/null
curl -sf http://localhost:7701/api/agents 2>/dev/null | head -30 || echo "AGENTS_ENDPOINT_UNAVAILABLE"
```

Read CLAUDE.md (or create it), then replace or append `## Open Design Configuration`:

```markdown
## Open Design Configuration (configured by /setup-open-design)
- Web app: http://localhost:3000
- Daemon: http://localhost:7701
- Install path: ~/open-design
- Data: ~/.od/app.sqlite (projects, conversations, messages)
- Start command: cd ~/open-design && pnpm tools-dev run daemon & pnpm tools-dev run web
- Detected CLIs: {list detected CLIs from Step 2}
```

If a `## Skill routing` section exists in CLAUDE.md, append this routing rule:
```
- Open Design, self-hosted design, "start open design", "run design tool" → invoke /setup-open-design
```

### Step 7: Summary

```
OPEN DESIGN — READY
═══════════════════════════════
Web:      http://localhost:3000
Daemon:   http://localhost:7701
Data:     ~/.od/app.sqlite
CLIs:     {detected CLIs}

Create your first project at http://localhost:3000

To start after a reboot:
  cd ~/open-design
  pnpm tools-dev run daemon &
  pnpm tools-dev run web

Config saved to CLAUDE.md. Run /setup-open-design again to reconfigure.
```

Offer to open the browser:
```bash
open http://localhost:3000 2>/dev/null || xdg-open http://localhost:3000 2>/dev/null || true
```

## Key Features

- **31 skills** — prototype templates (web pages, apps, dashboards), document types (specs, OKRs)
- **72 design systems** — Linear, Stripe, Notion, Apple, Tesla, and more
- **5 visual directions** — deterministic OKLch colors + font stacks when no brand exists
- **Real filesystem access** — the spawned agent reads/writes `.od/projects/<id>/` directly
- **Discovery form** — locks surface, audience, tone, and constraints in 30s before codegen
- **Import Claude Design exports** — accepts Claude Design ZIP files to continue editing

## Important Rules

- **Never expose API keys.** Confirm the save but never echo the value.
- **Confirm before reinstalling.** A fresh clone removes `~/open-design/` but `.od/` data persists.
- **CLAUDE.md is the source of truth.** Write the URL and paths there.
- **Idempotent.** Re-running /setup-open-design restarts services and rechecks config cleanly.
- **BYOK always.** Claude Code CLI works without an Anthropic API key.
