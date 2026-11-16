# Redaction guard (PII / secrets / legal content)

Full engine/taxonomy/config reference for the redaction guard. Summoned from
CLAUDE.md's "Redaction guard" section — read this before touching
`lib/redact-patterns.ts`, `lib/redact-engine.ts`, `bin/gstack-redact*`, or a
skill's redaction-doc wiring.

Shared redaction engine catches credentials, PII, and legal/damaging content
before it reaches an external sink (codex dispatch, GitHub issue/PR body,
pushed commit). It is a **guardrail, not airtight enforcement** — `git push
--no-verify`, direct `gh issue create`, and `GSTACK_REDACT_PREPUSH=skip` all
bypass it. It catches accidents and carelessness, the 99% case. Do not claim
it stops a determined leaker (a CHANGELOG line that does would fail a
hostile screenshotter).

- **Engine + taxonomy:** `lib/redact-patterns.ts` (the single source of truth —
  3 tiers; HIGH = genuinely-secret credentials that block, MEDIUM = PII/legal/
  internal + high-FP credential shapes that confirm via AskUserQuestion, LOW =
  FYI) and `lib/redact-engine.ts` (pure `scan()` + `applyRedactions()`).
  Calibration matters: a gate that cries wolf gets ignored, so context-variable
  shapes (Stripe `pk_live_`, Google `AIza`, JWT, env `*_KEY=`) sit at MEDIUM.
- **CLI:** `bin/gstack-redact` (exit 0 clean / 2 MEDIUM / 3 HIGH; `--json`,
  `--auto-redact`, `--repo-visibility`, `--from-file`). `bin/gstack-redact-prepush`
  is the opt-in git hook.
- **Skill docs are generated** from `scripts/resolvers/redact-doc.ts`
  (`{{REDACT_TAXONOMY_TABLE}}`, `{{REDACT_INVOCATION_BLOCK:<sink>}}`) so /spec,
  /cso, /ship, /document-release, /document-generate never drift from the engine.
- **Scan-at-sink:** always scan the EXACT bytes that will be sent — write to a
  temp file, scan that file, pass the SAME file to `gh`/`git`. Never scan a string
  then re-render (that reopens a scan-vs-send gap).
- **Visibility (no tier promotion):** resolve once per run, order = local config
  (`gstack-config get redact_repo_visibility`, ~/.gstack so never committed) → gh
  → glab → unknown(=public-strict). Public repos get STERNER per-finding
  confirmation (no batch-acknowledge, no silent-proceed); MEDIUM is never
  auto-promoted to HIGH.
- **Tool-attributed fences:** wrap Codex/Greptile/eval output in ` ```codex-review `
  / ` ```greptile ` fences so example credentials those tools quote WARN-degrade
  instead of blocking. A live-format credential inside the fence still blocks.
- **Config keys:** `redact_repo_visibility` (public|private|unknown, local-only
  override for repos gh/glab can't read), `redact_prepush_hook` (true|false).
  There is intentionally NO key to disable HIGH blocking.
- **Audit:** the /spec semantic pass appends a content-free record (categories +
  body sha256, no spec text) to `~/.gstack/security/semantic-reviews.jsonl` (0600).
