# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# gstack development

## Commands

```bash
bun install          # install dependencies
bun test             # run free tests (browse + snapshot + skill validation)
bun run test:evals   # run paid evals: LLM judge + E2E (diff-based, ~$4/run max)
bun run test:evals:all  # run ALL paid evals regardless of diff
bun run test:gate    # run gate-tier tests only (CI default, blocks merge)
bun run test:periodic  # run periodic-tier tests only (weekly cron / manual)
bun run test:e2e     # run E2E tests only (diff-based, ~$3.85/run max)
bun run test:e2e:all # run ALL E2E tests regardless of diff
bun run eval:select  # show which tests would run based on current diff
bun run dev <cmd>    # run CLI in dev mode, e.g. bun run dev goto https://example.com
bun run build        # gen docs + compile binaries
bun run gen:skill-docs  # regenerate SKILL.md files from templates
bun run skill:check  # health dashboard for all skills
bun run dev:skill    # watch mode: auto-regen + validate on change
bun run eval:list    # list all eval runs from ~/.gstack-dev/evals/
bun run eval:compare # compare two eval runs (auto-picks most recent)
bun run eval:summary # aggregate stats across all eval runs
bun run slop          # full slop-scan report (all files)
bun run slop:diff     # slop findings in files changed on this branch only
```

`test:evals` requires `ANTHROPIC_API_KEY`. Codex E2E tests (`test/codex-e2e.test.ts`)
use Codex's own auth from `~/.codex/` config — no `OPENAI_API_KEY` env var needed.

**Env keys in Conductor workspaces.** The `GSTACK_*` env-shim (v1.39.2.0+,
`lib/conductor-env-shim.ts`) promotes `GSTACK_ANTHROPIC_API_KEY` /
`GSTACK_OPENAI_API_KEY` to their canonical names inside gstack's TS binaries.
Tests run through gstack entrypoints inherit this promotion automatically.
Don't echo the key value to stdout, logs, or shell history. The historical
"never pass `env:` to `runAgentSdkTest`" rule is retired: the failure was
partial-env replacement (the SDK's `Options.env` REPLACES the child's entire
environment, so an object without the key broke auth). The runner now always
passes a COMPLETE hermetic env with per-test `env:` merged last, so per-test
overrides are safe; ambient `process.env.ANTHROPIC_API_KEY` mutation also
still works (the env builder reads process.env at call time).

**Hermetic local E2E (default).** Every E2E runner (claude -p, PTY, Agent
SDK, codex, gemini) spawns children through `test/helpers/hermetic-env.ts`:
allowlist-scrubbed env (operator `CONDUCTOR_*`, `CLAUDE_*`, `GSTACK_*`,
`MCP_*`, `GBRAIN_*`, and credentials like `GH_TOKEN` never reach children),
a fresh seeded `CLAUDE_CONFIG_DIR` (no operator `~/.claude` CLAUDE.md /
MCP servers / skills), a temp `GSTACK_HOME`, and `--strict-mcp-config`.
Local eval signal matches CI. Debug against real operator state with
`EVALS_HERMETIC=0` (restores the legacy env AND drops the strict-MCP flag).
Per-test `env:` overrides merge last, so deliberate contamination
(`CONDUCTOR_WORKSPACE_PATH`, per-test `GSTACK_HOME`) keeps working. Wiring
is pinned by `test/hermetic-wiring.test.ts` (static tripwire) and two
gate-tier canaries in `test/skill-e2e-hermetic-canary.test.ts`.

E2E tests stream progress in real-time (tool-by-tool via `--output-format stream-json
--verbose`). Results are persisted to `~/.gstack-dev/evals/` with auto-comparison
against the previous run.

**Diff-based test selection:** `test:evals` and `test:e2e` auto-select tests based
on `git diff` against the base branch. Each test declares its file dependencies in
`test/helpers/touchfiles.ts`. Changes to global touchfiles (session-runner, eval-store,
touchfiles.ts itself) trigger all tests. Use `EVALS_ALL=1` or the `:all` script
variants to force all tests. Run `eval:select` to preview which tests would run.

**Two-tier system:** Tests are classified as `gate` or `periodic` in `E2E_TIERS`
(in `test/helpers/touchfiles.ts`). CI runs only gate tests (`EVALS_TIER=gate`);
periodic tests run weekly via cron or manually. Use `EVALS_TIER=gate` or
`EVALS_TIER=periodic` to filter. When adding new E2E tests, classify them:
1. Safety guardrail or deterministic functional test? -> `gate`
2. Quality benchmark, Opus model test, or non-deterministic? -> `periodic`
3. Requires external service (Codex, Gemini)? -> `periodic`

## Testing

```bash
bun test             # run before every commit — free, <2s
bun run test:evals   # run before shipping — paid, diff-based (~$4/run max)
```

`bun test` runs skill validation, gen-skill-docs quality checks, and browse
integration tests. `bun run test:evals` runs LLM-judge quality evals and E2E
tests via `claude -p`. Both must pass before creating a PR.

## Project structure

```
gstack/
├── browse/          # Headless browser CLI (Playwright)
│   ├── src/         # CLI + server + commands
│   │   ├── commands.ts  # Command registry (single source of truth)
│   │   └── snapshot.ts  # SNAPSHOT_FLAGS metadata array
│   ├── test/        # Integration tests + fixtures
│   └── dist/        # Compiled binary
├── hosts/           # Typed host configs (one per AI agent)
│   ├── claude.ts    # Primary host config
│   ├── codex.ts, factory.ts, kiro.ts  # Existing hosts
│   ├── opencode.ts, slate.ts, cursor.ts, openclaw.ts  # IDE hosts
│   ├── hermes.ts, gbrain.ts  # Agent runtime hosts
│   └── index.ts     # Registry: exports all, derives Host type
├── scripts/         # Build + DX tooling
│   ├── gen-skill-docs.ts  # Template → SKILL.md generator (config-driven)
│   ├── host-config.ts     # HostConfig interface + validator
│   ├── host-config-export.ts  # Shell bridge for setup script
│   ├── host-adapters/     # Host-specific adapters (OpenClaw tool mapping)
│   ├── resolvers/   # Template resolver modules (preamble, design, review, gbrain, etc.)
│   ├── skill-check.ts     # Health dashboard
│   └── dev-skill.ts       # Watch mode
├── test/            # Skill validation + eval tests
│   ├── helpers/     # skill-parser.ts, session-runner.ts, llm-judge.ts, eval-store.ts
│   ├── fixtures/    # Ground truth JSON, planted-bug fixtures, eval baselines
│   ├── skill-validation.test.ts  # Tier 1: static validation (free, <1s)
│   ├── gen-skill-docs.test.ts    # Tier 1: generator quality (free, <1s)
│   ├── skill-llm-eval.test.ts   # Tier 3: LLM-as-judge (~$0.15/run)
│   └── skill-e2e-*.test.ts       # Tier 2: E2E via claude -p (~$3.85/run, split by category)
├── qa-only/         # /qa-only skill (report-only QA, no fixes)
├── plan-design-review/  # /plan-design-review skill (report-only design audit)
├── design-review/    # /design-review skill (design audit + fix loop)
├── ship/            # Ship workflow skill
├── review/          # PR review skill
├── plan-ceo-review/ # /plan-ceo-review skill
├── plan-eng-review/ # /plan-eng-review skill
├── autoplan/        # /autoplan skill (auto-review pipeline: CEO → design → eng)
├── benchmark/       # /benchmark skill (performance regression detection)
├── canary/          # /canary skill (post-deploy monitoring loop)
├── codex/           # /codex skill (multi-AI second opinion via OpenAI Codex CLI)
├── land-and-deploy/ # /land-and-deploy skill (merge → deploy → canary verify)
├── office-hours/    # /office-hours skill (YC Office Hours — startup diagnostic + builder brainstorm)
├── investigate/     # /investigate skill (systematic root-cause debugging)
├── spec/            # /spec skill (five-phase spec → GitHub issue, optional agent spawn, /ship auto-closes)
├── retro/           # Retrospective skill (includes /retro global cross-project mode)
├── bin/             # CLI utilities (gstack-repo-mode, gstack-slug, gstack-config, etc.)
├── document-release/ # /document-release skill (post-ship doc updates + Diataxis coverage map)
├── document-generate/ # /document-generate skill (Diataxis doc generator: tutorial/how-to/reference/explanation)
├── cso/             # /cso skill (OWASP Top 10 + STRIDE security audit)
├── design-consultation/ # /design-consultation skill (design system from scratch)
├── design-shotgun/  # /design-shotgun skill (visual design exploration)
├── open-gstack-browser/  # /open-gstack-browser skill (launch GStack Browser)
├── connect-chrome/  # symlink → open-gstack-browser (backwards compat)
├── design/          # Design binary CLI (GPT Image API)
│   ├── src/         # CLI + commands (generate, variants, compare, serve, etc.)
│   ├── test/        # Integration tests
│   └── dist/        # Compiled binary
├── extension/       # Chrome extension (side panel + activity feed + CSS inspector)
├── lib/             # Shared libraries (worktree.ts)
├── docs/designs/    # Design documents
├── setup-deploy/    # /setup-deploy skill (one-time deploy config)
├── .github/         # CI workflows + Docker image
│   ├── workflows/   # evals.yml (E2E on Ubicloud), skill-docs.yml, actionlint.yml
│   └── docker/      # Dockerfile.ci (pre-baked toolchain + Playwright/Chromium)
├── openclaw/        # Native OpenClaw skills (hand-crafted, published to ClawHub)
│   └── skills/      # gstack-openclaw-office-hours, ceo-review, investigate, retro
├── gstack-upgrade/  # /gstack-upgrade skill + migration scripts
│   └── migrations/  # Version migration scripts (vX.Y.Z.sh)
├── contrib/         # Contributor-only tools (never installed for users)
│   └── add-host/    # /gstack-contrib-add-host skill
├── setup            # One-time setup: build binary + symlink skills
├── SKILL.md         # Generated from SKILL.md.tmpl (don't edit directly)
├── SKILL.md.tmpl    # Template: edit this, run gen:skill-docs
├── ETHOS.md         # Builder philosophy (Boil the Ocean, Search Before Building)
└── package.json     # Build scripts for browse
```

## SKILL.md workflow

SKILL.md files are **generated** from `.tmpl` templates. To update docs:

1. Edit the `.tmpl` file (e.g. `SKILL.md.tmpl` or `browse/SKILL.md.tmpl`)
2. Run `bun run gen:skill-docs` (or `bun run build` which does it automatically)
3. Commit both the `.tmpl` and generated `.md` files

To add a new browse command: add it to `browse/src/commands.ts` and rebuild.
To add a snapshot flag: add it to `SNAPSHOT_FLAGS` in `browse/src/snapshot.ts` and rebuild.

**Token ceiling:** Generated SKILL.md files trip a warning above 160KB (~40K tokens).
This is a "watch for feature bloat" guardrail, not a hard gate. Modern flagship
models have 200K-1M context windows, so 40K is 4-20% of window, and prompt caching
makes the marginal cost of larger skills small. The ceiling exists to catch runaway
preamble/resolver growth, not to force compression on carefully-tuned big skills
(`ship`, `plan-ceo-review`, `office-hours` legitimately pack 25-35K tokens of
behavior). If you blow past 40K, the right fix is usually: (1) look at WHAT grew,
(2) if one resolver added 10K+ in a single PR, question whether it belongs inline
or as a reference doc, (3) only compress carefully-tuned prose as a last resort —
cuts to the coverage audit, review army, or voice directive have real quality cost.

**Merge conflicts on SKILL.md files:** NEVER resolve conflicts on generated SKILL.md
files by accepting either side. Instead: (1) resolve conflicts on the `.tmpl` templates
and `scripts/gen-skill-docs.ts` (the sources of truth), (2) run `bun run gen:skill-docs`
to regenerate all SKILL.md files, (3) stage the regenerated files. Accepting one side's
generated output silently drops the other side's template changes.

## Platform-agnostic design

Skills must NEVER hardcode framework-specific commands, file patterns, or directory
structures. Instead:

1. **Read CLAUDE.md** for project-specific config (test commands, eval commands, etc.)
2. **If missing, AskUserQuestion** — let the user tell you or let gstack search the repo
3. **Persist the answer to CLAUDE.md** so we never have to ask again

This applies to test commands, eval commands, deploy commands, and any other
project-specific behavior. The project owns its config; gstack reads it.

## Writing SKILL templates

SKILL.md.tmpl files are **prompt templates read by Claude**, not bash scripts.
Each bash code block runs in a separate shell — variables do not persist between blocks.

Rules:
- **Use natural language for logic and state.** Don't use shell variables to pass
  state between code blocks. Instead, tell Claude what to remember and reference
  it in prose (e.g., "the base branch detected in Step 0").
- **Don't hardcode branch names.** Detect `main`/`master`/etc dynamically via
  `gh pr view` or `gh repo view`. Use `{{BASE_BRANCH_DETECT}}` for PR-targeting
  skills. Use "the base branch" in prose, `<base>` in code block placeholders.
- **Keep bash blocks self-contained.** Each code block should work independently.
  If a block needs context from a previous step, restate it in the prose above.
- **Express conditionals as English.** Instead of nested `if/elif/else` in bash,
  write numbered decision steps: "1. If X, do Y. 2. Otherwise, do Z."

## Writing style (V1)

Default output from every tier-≥2 skill follows the Writing Style section in
`scripts/resolvers/preamble.ts`: jargon glossed on first use (curated list in
`scripts/jargon-list.json`, baked at gen-skill-docs time), questions framed in
outcome terms ("what breaks for your users if...") not implementation terms,
short sentences, decisions close with user impact. Power users who want the
tighter V0 prose set `gstack-config set explain_level terse` (binary switch,
no middle mode). See `docs/designs/PLAN_TUNING_V1.md` for the full design
rationale. The review pacing overhaul that originally tried to ride alongside
writing-style was extracted to V1.1 — see `docs/designs/PACING_UPDATES_V0.md`.

## Browser interaction

When you need to interact with a browser (QA, dogfooding, cookie setup), use the
`/browse` skill or run the browse binary directly via `$B <command>`. NEVER use
`mcp__claude-in-chrome__*` tools — they are slow, unreliable, and not what this
project uses.

**Sidebar architecture:** Before modifying `sidepanel.js`, `background.js`,
`content.js`, `terminal-agent.ts`, or sidebar-related server endpoints,
read `docs/designs/SIDEBAR_MESSAGE_FLOW.md`. The sidebar has one primary
surface — the **Terminal** pane (interactive `claude` PTY) — with
Activity / Refs / Inspector as debug overlays behind the footer's
`debug` toggle. The chat queue path was ripped once the PTY proved out;
`sidebar-agent.ts` and the `/sidebar-command` / `/sidebar-chat` /
`/sidebar-agent/event` endpoints are gone. The doc covers the WS auth
flow, dual-token model, and threat-model boundary — silent failures
here usually trace to not understanding the cross-component flow.

**Embedder terminal-agent ownership** (v1.42.1.0+, identity-based kill v1.44.0.0+).
`buildFetchHandler` in `browse/src/server.ts` accepts `ServerConfig.ownsTerminalAgent?:
boolean` (default `true`). When `true`, factory shutdown runs the full teardown:
identity-based kill via `killAgentByRecord(readAgentRecord(stateDir))` from
`browse/src/terminal-agent-control.ts` plus `safeUnlinkQuiet` on
`<stateDir>/terminal-port`, `<stateDir>/terminal-internal-token`, and
`<stateDir>/terminal-agent-pid` (the per-boot agent record introduced in v1.44).
Embedders (e.g. the gbrowser phoenix overlay) that pre-launch their own PTY
server must pass `false` so their discovery files survive gstack teardown cycles.
The flag is the third caller-owned teardown gate in `ServerConfig` (alongside
`xvfb?` and `proxyBridge?`); polarity is inverted (explicit bool vs presence) and
documented in the field's JSDoc. CLI `start()` always passes `true` explicitly —
the static-grep test in `browse/test/server-embedder-terminal-port.test.ts` fails
CI if a refactor drops it. Pre-v1.44 used `pkill -f terminal-agent\.ts` (regex
match) which would kill sibling gstack sessions on the same host; the new
`browse/test/terminal-agent-pid-identity.test.ts` static-grep tripwire fails CI
if any source file re-introduces `pkill ... terminal-agent` or `spawnSync('pkill', ...)`.

**WebSocket auth uses Sec-WebSocket-Protocol, not cookies.** Browsers
can't set `Authorization` on a WebSocket upgrade, but they CAN set
`Sec-WebSocket-Protocol` via `new WebSocket(url, [token])`. The agent
reads it, validates against `validTokens`, and MUST echo the protocol
back in the upgrade response — without the echo, Chromium closes the
connection immediately. `Set-Cookie: gstack_pty=...` is kept as a
fallback for non-browser callers (the cross-port `SameSite=Strict`
cookie path doesn't survive from a chrome-extension origin).

**Cross-pane PTY injection.** The toolbar's Cleanup button and the
Inspector's "Send to Code" action both pipe text into the live claude
PTY via `window.gstackInjectToTerminal(text)`, exposed by
`sidepanel-terminal.js`. No `/sidebar-command` POST — the live REPL is
the only execution surface in the sidebar now.

**`/health` MUST NOT surface any shell-grant token.** It already leaks
`AUTH_TOKEN` to localhost callers in headed mode (a v1.1+ TODO). Don't
make that worse by adding the PTY session token there. PTY auth flows
through `POST /pty-session` only.

**Transport-layer security** (v1.6.0.0+) and **Unicode sanitization at server
egress** (v1.38.0.0+) are documented in full in
[ARCHITECTURE.md](ARCHITECTURE.md#dual-listener-tunnel-architecture-v1600) and
[ARCHITECTURE.md](ARCHITECTURE.md#unicode-sanitization-at-server-egress-v13800)
respectively — read those before editing `server.ts`, `sse-session-cookie.ts`,
or `tunnel-denial-log.ts`. Short version: `pair-agent`'s ngrok tunnel runs
through a second, allowlisted HTTP listener so root tokens can never reach it
(403 if tried); SSE endpoints use their own 30-minute cookie, never valid
against `/command`. Every new SSE/WebSocket writer or HTTP response that
ships page-derived strings must sanitize lone UTF-16 surrogates before
`JSON.stringify` — `browse/test/server-sanitize-surrogates.test.ts` pins the
wiring with invariant tests, so bypasses fail CI.

**SSE endpoint helper** (v1.51.0.0+). New SSE endpoints in `server.ts` MUST route
through `createSseEndpoint(req, config)` from `browse/src/sse-helpers.ts`. The
helper owns the cleanup contract (abort + enqueue-throw + heartbeat-throw, all
idempotent) and bakes in `sanitizeLoneSurrogates` on every JSON.stringify, so
new subscribers can't accidentally regress either invariant. Inline
`ReadableStream` wiring leaked subscribers when the TCP connection died without
firing `req.signal.abort` (Chromium MV3 service-worker suspend, intermediate
proxy half-close). `/activity/stream`, `/inspector/events`, and `/memory`
(SSE-eligible) all route through it. `browse/test/sse-helpers.test.ts` pins the
cleanup contract.

**CDP session lifecycle** (v1.51.0.0+). Direct `page.context().newCDPSession(page)`
calls outside `browse/src/cdp-bridge.ts` fail CI via the static-grep tripwire in
`browse/test/cdp-session-cleanup.test.ts`. Use `withCdpSession(page, async (s) => {...})`
for one-shot CDP work (try/finally detach) or `getOrCreateCdpSession(page, cache)`
for cached sessions tied to a page's lifetime (close-detach via `Map<page, session>`).
Three sites migrated: cdp-bridge frame events, write-commands archive capture,
cdp-inspector. The helpers prevent the per-session leak class where successful-path
detach happened but error-path detach was missed.

**Setup symlink hardening** (v1.38.0.0+). Every link site in `setup` MUST route
through the `_link_or_copy SRC DST` helper near the `IS_WINDOWS` detection. On
Windows without Developer Mode, plain `ln -snf` produces frozen file copies that
don't refresh on `git pull` — silent staleness across every host adapter. The
helper preserves `ln -snf` on Unix and switches to `cp -R` / `cp -f` on Windows.
`test/setup-windows-fallback.test.ts` enforces a static invariant: a single raw
`ln` call outside the helper body fails CI. Windows users get a one-line note
from `_print_windows_copy_note_once` reminding them to re-run `./setup` after
every `git pull`.

**Sidebar security stack** (layered defense against prompt injection — L1-L3
content security, L4/L4b ML + transcript classifiers sidebar-agent-only, L5
canary, L6 ensemble combiner) is documented in full, including thresholds and
env knobs, in
[ARCHITECTURE.md](ARCHITECTURE.md#prompt-injection-defense-sidebar-agent).
Critical constraint to remember without opening the doc:
`security-classifier.ts` CANNOT be imported from the compiled browse binary —
`@huggingface/transformers` v4's `onnxruntime-node` fails to `dlopen` from
Bun compile's temp extract dir, so only `security.ts` (pure-string canary +
verdict combiner) is safe for `server.ts`.

## Dev symlink awareness

When developing gstack, `.claude/skills/gstack` may be a symlink back to this
working directory (gitignored). This means skill changes are **live immediately**,
great for rapid iteration, risky during big refactors where half-written skills
could break other Claude Code sessions using gstack concurrently.

**Check once per session:** Run `ls -la .claude/skills/gstack` to see if it's a
symlink or a real copy. If it's a symlink to your working directory, be aware that:
- Template changes + `bun run gen:skill-docs` immediately affect all gstack invocations
- Breaking changes to SKILL.md.tmpl files can break concurrent gstack sessions
- During large refactors, remove the symlink (`rm .claude/skills/gstack`) so the
  global install at `~/.claude/skills/gstack/` is used instead

**Prefix setting:** Setup creates real directories (not symlinks) at the top level
with a SKILL.md symlink inside (e.g., `qa/SKILL.md -> gstack/qa/SKILL.md`). This
ensures Claude discovers them as top-level skills, not nested under `gstack/`.
Names are either short (`qa`) or namespaced (`gstack-qa`), controlled by
`skill_prefix` in `~/.gstack/config.yaml`. Pass `--no-prefix` or `--prefix` to
skip the interactive prompt.

**Note:** Vendoring gstack into a project's repo is deprecated. Use global install
+ `./setup --team` instead. See README.md for team mode instructions.

**For plan reviews:** When reviewing plans that modify skill templates or the
gen-skill-docs pipeline, consider whether the changes should be tested in isolation
before going live (especially if the user is actively using gstack in other windows).

**Upgrade migrations:** When a change modifies on-disk state (directory structure,
config format, stale files) in ways that could break existing user installs, add a
migration script to `gstack-upgrade/migrations/`. Read CONTRIBUTING.md's "Upgrade
migrations" section for the format and testing requirements. The upgrade skill runs
these automatically after `./setup` during `/gstack-upgrade`.

## Compiled binaries — NEVER commit browse/dist/ or design/dist/

The `browse/dist/` and `design/dist/` directories contain compiled Bun binaries
(`browse`, `find-browse`, `design`, ~58MB each). These are Mach-O arm64 only — they
do NOT work on Linux, Windows, or Intel Macs. The `./setup` script already builds
from source for every platform, so the checked-in binaries are redundant. They are
tracked by git due to a historical mistake and should eventually be removed with
`git rm --cached`.

**NEVER stage or commit these files.** They show up as modified in `git status`
because they're tracked despite `.gitignore` — ignore them. When staging files,
always use specific filenames (`git add file1 file2`) — never `git add .` or
`git add -A`, which will accidentally include the binaries.

## Redaction guard (PII / secrets / legal content)

Shared redaction engine catches credentials, PII, and legal/damaging content
before it reaches an external sink (codex dispatch, GitHub issue/PR body, pushed
commit). It is a **guardrail, not airtight enforcement** — `git push --no-verify`,
direct `gh issue create`, and `GSTACK_REDACT_PREPUSH=skip` all bypass it. It
catches accidents and carelessness, the 99% case. Do not claim it stops a
determined leaker (a CHANGELOG line that does would fail a hostile screenshotter).

Engine lives in `lib/redact-patterns.ts` (3-tier taxonomy: HIGH blocks, MEDIUM
confirms via AskUserQuestion, LOW is FYI) + `lib/redact-engine.ts`; CLI is
`bin/gstack-redact` / `bin/gstack-redact-prepush`. Full taxonomy, scan-at-sink
discipline, visibility resolution order, and config keys are in
[docs/REDACTION_GUARD.md](docs/REDACTION_GUARD.md) — read it before touching
the engine, the CLI, or a skill's redaction-doc wiring.

## Commit style

**Always bisect commits.** Every commit should be a single logical change. When
you've made multiple changes (e.g., a rename + a rewrite + new tests), split them
into separate commits before pushing. Each commit should be independently
understandable and revertable.

Examples of good bisection:
- Rename/move separate from behavior changes
- Test infrastructure (touchfiles, helpers) separate from test implementations
- Template changes separate from generated file regeneration
- Mechanical refactors separate from new features

When the user says "bisect commit" or "bisect and push," split staged/unstaged
changes into logical commits and push.

## Slop-scan: AI code quality, not AI code hiding

We use [slop-scan](https://github.com/benvinegar/slop-scan) to catch patterns where
AI-generated code is genuinely worse than what a human would write. We are NOT trying
to pass as human code. We are AI-coded and proud of it. The goal is code quality.

```bash
npx slop-scan scan .          # human-readable report
npx slop-scan scan . --json   # machine-readable for diffing
```

Config: `slop-scan.config.json` at repo root (currently excludes `**/vendor/**`).
Full what-to-fix / what-not-to-fix guidance (empty catches, `safeUnlink`/`safeKill`
utilities, and the explicit "don't fix these" list) is in
[docs/SLOP_SCAN_GUIDE.md](docs/SLOP_SCAN_GUIDE.md) — read it before acting on a
slop-scan report. One-line version: fix empty catches around file/process ops
with the `safeUnlink`/`safeKill` helpers; don't "fix" error-message string
matching or best-effort cleanup catches — those are often the correct pattern.

## Community PR guardrails

When reviewing or merging community PRs, **always AskUserQuestion** before accepting
any commit that:

1. **Touches ETHOS.md** — this file is Garry's personal builder philosophy. No edits
   from external contributors or AI agents, period.
2. **Removes or softens promotional material** — YC references, founder perspective,
   and product voice are intentional. PRs that frame these as "unnecessary" or
   "too promotional" must be rejected.
3. **Changes Garry's voice** — the tone, humor, directness, and perspective in skill
   templates, CHANGELOG, and docs are not generic. PRs that rewrite voice to be
   more "neutral" or "professional" must be rejected.

Even if the agent strongly believes a change improves the project, these three
categories require explicit user approval via AskUserQuestion. No exceptions.
No auto-merging. No "I'll just clean this up."

## Checking out PRs from garrytan-agents

When the user says "check out <PR link>" and the PR is from `garrytan-agents/gstack`
(or any other fork that is NOT a collaborator on `garrytan/gstack`), do NOT just
`gh pr checkout`. Fork PRs don't receive base-repo secrets (`ANTHROPIC_API_KEY`,
`OPENAI_API_KEY`, etc.), so the eval/E2E CI jobs fail with empty-env auth errors
regardless of what's set on the base repo.

**Workflow:** push the branch to `garrytan/gstack` (the base repo) and re-target
the PR from there.

Concretely, after `gh pr checkout <N>`:

1. Note the original PR number and head branch name.
2. Push the same branch to the base repo: `git push origin HEAD:<branch-name>`
   (origin = `garrytan/gstack`, since the worktree is set up with that remote).
3. Close the fork PR (`gh pr close <N> --comment "moving to base-repo branch for secret access"`).
4. Open a new PR from the base-repo branch: `gh pr create --base main --head <branch-name>`.
5. New PR's workflows will get secrets automatically.

Why not fix it on the fork side? `garrytan-agents` isn't a collaborator on
`garrytan/gstack`. Adding it as a collaborator (option A) or flipping the
repo-wide "send secrets to fork PRs" toggle (option B) would let secrets reach
fork PRs from anyone — broader blast radius than just moving this one branch.
Option C (this section) keeps secret-distribution scope tight.

If the user asks you to skip the move (e.g., "just leave it as a fork PR"),
respect that — eval CI will fail with empty-env auth, but check-freshness,
workflow-lint, and windows-tests will still pass on the fork PR.

## CHANGELOG + VERSION style

Full rules — versioning invariants, scale-aware bump guidance, branch-scoped
CHANGELOG discipline, the release-summary format, and the itemized-changes
section — live in **[docs/CHANGELOG_STYLE.md](docs/CHANGELOG_STYLE.md)**.
Read it in full before writing or editing a CHANGELOG entry or VERSION bump.

The load-bearing rules, compressed:

- VERSION is a monotonic release identifier, not strict semver. Bump level
  (patch/minor/major) communicates scale — when the diff is big, bump MINOR
  or MAJOR, not PATCH. Don't undersell a substantial change as a PATCH.
- VERSION and CHANGELOG are **branch-scoped**: every feature branch gets its
  own bump and its own entry, written at `/ship` time (Step 13), covering ALL
  commits vs the base branch — never mid-branch, never folded into an entry
  that already landed on main.
- **The entry is the diff between main and the shipping branch — not how the
  branch got there.** No branch-internal version numbers, no "we caught and
  fixed a bug mid-branch," no plan-review narrative. Describe the released
  system's properties, not the path to them.
- Merging origin/main does NOT mean adopting its version — your branch still
  bumps its own version on top, and your entry stays the topmost entry.
  Branch-internal version bumps that never reached main get collapsed into
  one final entry, not back-filled as gaps.
- CHANGELOG.md is for **users**: lead with what they can now do, plain
  language, no jargon, contributor/internal notes go in a separate "For
  contributors" section. Always credit community PRs with `Contributed by
  @username`.
- Every `## [X.Y.Z]` entry starts with a release-summary section (two-line
  verdict headline, lead paragraph, a numbers-that-matter table with
  before/after/Δ, a "what this means for you" close — no em dashes, no AI
  vocabulary, real numbers only) followed by `### Itemized changes` below it.

## AI effort compression

When estimating or discussing effort, always show both human-team and CC+gstack time:

| Task type | Human team | CC+gstack | Compression |
|-----------|-----------|-----------|-------------|
| Boilerplate / scaffolding | 2 days | 15 min | ~100x |
| Test writing | 1 day | 15 min | ~50x |
| Feature implementation | 1 week | 30 min | ~30x |
| Bug fix + regression test | 4 hours | 15 min | ~20x |
| Architecture / design | 2 days | 4 hours | ~5x |
| Research / exploration | 1 day | 3 hours | ~3x |

Completeness is cheap. Don't recommend shortcuts when the complete implementation
is achievable. Boil the ocean — the complete thing is the goal; only genuinely
unrelated multi-quarter migrations are separate scope, never an excuse for a
shortcut. See the Completeness Principle in the skill preamble for the full
philosophy.

## Search before building

Before designing any solution that involves concurrency, unfamiliar patterns,
infrastructure, or anything where the runtime/framework might have a built-in:

1. Search for "{runtime} {thing} built-in"
2. Search for "{thing} best practice {current year}"
3. Check official runtime/framework docs

Three layers of knowledge: tried-and-true (Layer 1), new-and-popular (Layer 2),
first-principles (Layer 3). Prize Layer 3 above all. See ETHOS.md for the full
builder philosophy.

## Local plans

Contributors can store long-range vision docs and design documents in `~/.gstack-dev/plans/`.
These are local-only (not checked in). When reviewing TODOS.md, check `plans/` for candidates
that may be ready to promote to TODOs or implement.

## E2E eval failure blame protocol

When an E2E eval fails during `/ship` or any other workflow, **never claim "not
related to our changes" without proving it.** These systems have invisible couplings —
a preamble text change affects agent behavior, a new helper changes timing, a
regenerated SKILL.md shifts prompt context.

**Required before attributing a failure to "pre-existing":**
1. Run the same eval on main (or base branch) and show it fails there too
2. If it passes on main but fails on the branch — it IS your change. Trace the blame.
3. If you can't run on main, say "unverified — may or may not be related" and flag it
   as a risk in the PR body

"Pre-existing" without receipts is a lazy claim. Prove it or don't say it.

## Long-running tasks: don't give up

When running evals, E2E tests, or any long-running background task, **poll until
completion**. Use `sleep 180 && echo "ready"` + `TaskOutput` in a loop every 3
minutes. Never switch to blocking mode and give up when the poll times out. Never
say "I'll be notified when it completes" and stop checking — keep the loop going
until the task finishes or the user tells you to stop.

The full E2E suite can take 30-45 minutes. That's 10-15 polling cycles. Do all of
them. Report progress at each check (which tests passed, which are running, any
failures so far). The user wants to see the run complete, not a promise that
you'll check later.

## Running evals as an agent: always detach (SIGTERM-proof)

When **you (an agent/harness)** launch a long eval/benchmark run, run it through
`bin/gstack-detach` — NEVER as a plain backgrounded Bash task. A plain background
task lives in the harness's process group, so a SIGTERM ("polite quit") on a turn
boundary, a stopped Monitor, or an interruption kills the run mid-flight (observed:
`script "test:gate" was terminated by signal SIGTERM` ~40 min into a run). On macOS
the run can also die to idle-sleep. `gstack-detach` fixes both: a fresh session
(escapes the group SIGTERM) wrapped in `caffeinate -i` (blocks idle-sleep).

- Use the `eval:bg*` scripts (`eval:bg`, `eval:bg:all`, `eval:bg:gate`,
  `eval:bg:periodic`) — they wrap the eval command in `gstack-detach` with the
  machine-wide `gstack-evals` lock (concurrent worktrees serialize instead of
  saturating the shared model API), a per-tier watchdog, and a **run-scoped** log
  under `~/.gstack-dev/eval-runs/` (no shared-`/tmp` collision). Each prints its
  log path. Or call `gstack-detach [--lock NAME] [--timeout SECS] [--label LBL] --
  <cmd>` directly for any long agent job. Export `ANTHROPIC_API_KEY` first (never
  pass keys in argv).
- Then **poll the printed logfile** with a death-aware watcher: break on the
  guaranteed `### gstack-detach EXIT=<code> ###` sentinel (success AND failure are
  both marked, so silence is never mistaken for success). The detached run survives
  even if your watcher gets reaped, so re-checking the log always works.
- Why the lock: a shared dev box with several Conductor worktrees will rate-limit
  the model API if two eval suites run at once (15-way concurrency each), which
  mass-times-out E2E tests. The lock makes the second run WAIT, not collide.
- Humans running `bun run test:evals` foreground in their own terminal don't need
  this — Ctrl-C is intended there. Detachment is for agent-launched runs only.

## E2E test fixtures: extract, don't copy

**NEVER copy a full SKILL.md file into an E2E test fixture.** SKILL.md files are
1500-2000 lines. When `claude -p` reads a file that large, context bloat causes
timeouts, flaky turn limits, and tests that take 5-10x longer than necessary.

Instead, extract only the section the test actually needs:

```typescript
// BAD — agent reads 1900 lines, burns tokens on irrelevant sections
fs.copyFileSync(path.join(ROOT, 'ship', 'SKILL.md'), path.join(dir, 'ship-SKILL.md'));

// GOOD — agent reads ~60 lines, finishes in 38s instead of timing out
const full = fs.readFileSync(path.join(ROOT, 'ship', 'SKILL.md'), 'utf-8');
const start = full.indexOf('## Review Readiness Dashboard');
const end = full.indexOf('\n---\n', start);
fs.writeFileSync(path.join(dir, 'ship-SKILL.md'), full.slice(start, end > start ? end : undefined));
```

Also when running targeted E2E tests to debug failures:
- Run in **foreground** (`bun test ...`), not background with `&` and `tee`
- Never `pkill` running eval processes and restart — you lose results and waste money
- One clean run beats three killed-and-restarted runs

## Publishing native OpenClaw skills to ClawHub

Native OpenClaw skills live in `openclaw/skills/gstack-openclaw-*/SKILL.md`
and publish to ClawHub via `clawhub publish` (NOT `clawhub skill publish`).
Full command, auth, update, and verification steps are in
[docs/OPENCLAW_PUBLISHING.md](docs/OPENCLAW_PUBLISHING.md) — read it before
publishing or updating one of these skills.

## Deploying to the active skill

The active skill lives at `~/.claude/skills/gstack/`. After making changes:

1. Push your branch
2. Fetch and reset in the skill directory: `cd ~/.claude/skills/gstack && git fetch origin && git reset --hard origin/main`
3. Rebuild: `cd ~/.claude/skills/gstack && bun run build`

**If you use gbrain:** the `git reset --hard` in step 2 reverts the brain-aware
(`GBRAIN_CONTEXT_LOAD` / `GBRAIN_SAVE_RESULTS`) blocks that `gstack-config
gbrain-refresh` renders into the install (those generated blocks differ from
`main` by design). After deploying, re-run `gstack-config gbrain-refresh` to
restore them across all your projects' Claude sessions. It's idempotent.

Or copy the binaries directly:
- `cp browse/dist/browse ~/.claude/skills/gstack/browse/dist/browse`
- `cp design/dist/design ~/.claude/skills/gstack/design/dist/design`

## Skill routing

When the user's request matches an available skill, invoke it via the Skill tool. When in doubt, invoke the skill.

Key routing rules:
- Product ideas/brainstorming → invoke /office-hours
- Strategy/scope → invoke /plan-ceo-review
- Architecture → invoke /plan-eng-review
- Design system/plan review → invoke /design-consultation or /plan-design-review
- Full review pipeline → invoke /autoplan
- Bugs/errors → invoke /investigate
- QA/testing site behavior → invoke /qa or /qa-only
- Code review/diff check → invoke /review
- Visual polish → invoke /design-review
- Ship/deploy/PR → invoke /ship or /land-and-deploy
- Save progress → invoke /context-save
- Resume context → invoke /context-restore

## Cross-session decision memory

Durable decisions and their rationale are captured in an append-only, event-sourced
store at `~/.gstack/projects/<slug>/decisions.jsonl` so neither you nor the user
re-litigates a settled call or loses the "why" across sessions. This is the reliable,
file-only path: it works with gbrain OFF. (gbrain semantic recall is an optional
enhancement layered on top, never a dependency.)

- **Resurface** active decisions before re-deciding: `bin/gstack-decision-search`
  (`--recent N`, `--scope repo|branch|issue`, `--query KW`, `--all`, `--json`).
  Add `--semantic` (with `--query`) to append related hits from gbrain memory when
  it's up; it degrades silently to the reliable file results when gbrain is off.
  Session start already surfaces scope-relevant active decisions via Context Recovery.
  If a decision is listed, treat it as settled with its rationale; if you're about to
  reverse it, say so explicitly.
- **Capture** a DURABLE decision when you or the user make one:
  `bin/gstack-decision-log '{"decision":"...","rationale":"...","scope":"repo|branch|issue","source":"user|skill|agent","confidence":1-10}'`.
  Reverse a prior call with `--supersede <id>`; expunge an accidental secret with
  `--redact <id>`; rewrite the log to the active set with `--compact`. Non-interactive
  (never prompts), injection-sanitized, and HIGH-secret-blocking on write.
- **Durable means:** architecture choice, scope cut, tool/vendor choice, or a reversal
  of a prior call. NOT a turn-level edit, a phrasing tweak, or anything trivially
  re-derivable. Capture is curated at the source — log durable decisions only, or the
  store becomes noise.

## GBrain Search Guidance (configured by /sync-gbrain)
<!-- gstack-gbrain-search-guidance:start -->

GBrain is set up and synced on this machine. The agent should prefer gbrain
over Grep when the question is semantic or when you don't know the exact
identifier yet.

**This worktree is pinned to a worktree-scoped code source** via the
`.gbrain-source` file in the repo root (kubectl-style context). Any
`gbrain code-def`, `code-refs`, `code-callers`, `code-callees`, or `query`
call from anywhere under this worktree routes to that source by default —
no `--source` flag needed. Conductor sibling worktrees of the same repo
each have their own pin and their own indexed pages, so semantic results
match the actual code on disk in this worktree.

Two indexed corpora available via the `gbrain` CLI:
- This worktree's code (auto-pinned via `.gbrain-source`).
- `~/.gstack/` curated memory (registered as `gstack-brain-<user>` source via
  the existing federation pipeline).

Prefer gbrain when:
- "Where is X handled?" / semantic intent, no exact string yet:
    `gbrain search "<terms>"` or `gbrain query "<question>"`
- "Where is symbol Y defined?" / symbol-based code questions:
    `gbrain code-def <symbol>` or `gbrain code-refs <symbol>`
- "What calls Y?" / "What does Y depend on?":
    `gbrain code-callers <symbol>` / `gbrain code-callees <symbol>`
- "What did we decide last time?" / past plans, retros, learnings:
    `gbrain search "<terms>" --source gstack-brain-<user>`

Grep is still right for known exact strings, regex, multiline patterns, and
file globs. Run `/sync-gbrain` after meaningful code changes; for ongoing
auto-sync across all worktrees, run `gbrain autopilot --install` once per
machine — gbrain's daemon handles incremental refresh on a schedule.

Safety: don't run `/sync-gbrain` while `gbrain autopilot` is active — the
orchestrator refuses destructive source ops when it detects a running autopilot
to avoid racing it (#1734). Prefer registering user repos with `gbrain sources
add --path <dir>` (no `--url`): URL-managed sources can auto-reclone, and the
sync code walk for them requires an explicit `--allow-reclone` opt-in.

<!-- gstack-gbrain-search-guidance:end -->
