#!/usr/bin/env bash
# hygiene-scan.sh — deterministic fact collector for the /hygiene skill.
#
# Prints FACT: / WARN: / OK: lines about Claude Code configuration and repo
# hygiene. It never prints secret VALUES — only names, counts, and file sizes.
# The skill (Claude) interprets this output and writes the advisory report;
# this script only gathers evidence, so its output must stay stable and
# grep-friendly.
#
# Safe to run anywhere: every check degrades to a SKIP line when the tool or
# file it needs is missing. Exit code is always 0 — findings are content, not
# failures.
set -uo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
CLAUDE_DIR="${CLAUDE_CONFIG_DIR:-$HOME/.claude}"

section() { printf '\n=== %s ===\n' "$1"; }

# ── Claude settings files ───────────────────────────────────────────────
section "CLAUDE SETTINGS"
for f in "$CLAUDE_DIR/settings.json" "$CLAUDE_DIR/settings.local.json" \
         "$REPO_ROOT/.claude/settings.json" "$REPO_ROOT/.claude/settings.local.json"; do
  if [ -f "$f" ]; then
    echo "FACT: settings file $f ($(wc -c < "$f") bytes)"
  fi
done
[ -f "$REPO_ROOT/.claude/settings.local.json" ] && \
  echo "WARN: .claude/settings.local.json exists — local overrides drift silently; review whether entries belong in the shared settings.json"

# Permission sprawl: count allow entries and flag broad globs.
if command -v python3 >/dev/null 2>&1; then
  for f in "$CLAUDE_DIR/settings.json" "$REPO_ROOT/.claude/settings.json" \
           "$REPO_ROOT/.claude/settings.local.json"; do
    [ -f "$f" ] || continue
    python3 - "$f" <<'PY'
import json, sys
try:
    d = json.load(open(sys.argv[1]))
except Exception:
    print(f"WARN: {sys.argv[1]} is not valid JSON")
    sys.exit(0)
perms = d.get("permissions", {})
allow = perms.get("allow", [])
deny = perms.get("deny", [])
print(f"FACT: {sys.argv[1]}: {len(allow)} allow rules, {len(deny)} deny rules")
broad = [r for r in allow if r in ("Bash", "Bash(*)", "Bash(*:*)", "WebFetch", "Write")]
for r in broad:
    print(f"WARN: broad permission rule '{r}' in {sys.argv[1]} — grants more than one command family; prefer specific patterns like Bash(npm test:*)")
hooks = d.get("hooks", {})
if hooks:
    print(f"FACT: {sys.argv[1]} defines hooks for events: {', '.join(hooks.keys())}")
PY
  done
else
  echo "SKIP: python3 not found — permission-rule analysis skipped"
fi

# ── Hooks ───────────────────────────────────────────────────────────────
section "HOOKS"
for d in "$CLAUDE_DIR" "$REPO_ROOT/.claude/hooks"; do
  [ -d "$d" ] || continue
  find "$d" -maxdepth 1 -type f \( -name '*.sh' -o -name '*.py' \) 2>/dev/null | while read -r h; do
    perms=$(ls -l "$h" | cut -c1-10)
    echo "FACT: hook script $h ($perms)"
    case "$perms" in
      *w?) echo "WARN: $h is world-writable — any local process can change what runs in your sessions; chmod o-w it" ;;
    esac
  done
done

# ── MCP servers ─────────────────────────────────────────────────────────
section "MCP"
if [ -f "$REPO_ROOT/.mcp.json" ]; then
  echo "FACT: project .mcp.json present ($(wc -c < "$REPO_ROOT/.mcp.json") bytes)"
  grep -qE '"(env|headers)"' "$REPO_ROOT/.mcp.json" && \
    echo "WARN: .mcp.json contains env/headers blocks — verify no literal tokens are committed; use \${ENV_VAR} expansion instead"
else
  echo "FACT: no project .mcp.json"
fi

# ── Environment credentials (names only) ────────────────────────────────
section "ENV CREDENTIALS"
env | grep -iE '^[A-Z0-9_]*(KEY|TOKEN|SECRET|PASSWORD)[A-Z0-9_]*=' | cut -d= -f1 | sort | \
  while read -r name; do echo "FACT: credential-shaped env var: $name (value not shown)"; done

# ── Context weight ──────────────────────────────────────────────────────
section "CONTEXT WEIGHT"
for f in "$REPO_ROOT/CLAUDE.md" "$CLAUDE_DIR/CLAUDE.md"; do
  if [ -f "$f" ]; then
    bytes=$(wc -c < "$f")
    echo "FACT: $f is $bytes bytes (~$((bytes / 4)) tokens, loaded every session)"
    [ "$bytes" -gt 20000 ] && echo "WARN: $f exceeds 20KB — this is injected into every session; consider moving reference material into linked docs"
  fi
done
if [ -d "$CLAUDE_DIR/skills" ]; then
  echo "FACT: $(find "$CLAUDE_DIR/skills" -maxdepth 1 -mindepth 1 | wc -l | tr -d ' ') skills installed in $CLAUDE_DIR/skills"
fi

# ── Git hygiene ─────────────────────────────────────────────────────────
section "GIT"
if git -C "$REPO_ROOT" rev-parse --git-dir >/dev/null 2>&1; then
  branch=$(git -C "$REPO_ROOT" branch --show-current 2>/dev/null || echo "?")
  default=$(git -C "$REPO_ROOT" symbolic-ref --short refs/remotes/origin/HEAD 2>/dev/null | sed 's|origin/||')
  [ -z "$default" ] && default=$(git -C "$REPO_ROOT" branch -r 2>/dev/null | grep -oE 'origin/(main|master)$' | head -1 | sed 's|origin/||')
  echo "FACT: on branch '$branch' (default branch: '${default:-unknown}')"
  if [ -n "$default" ] && [ "$branch" = "$default" ]; then
    echo "WARN: working directly on the default branch — use a feature branch + PR so '$default' stays fast-forwardable"
  fi
  dirty=$(git -C "$REPO_ROOT" status --porcelain 2>/dev/null | wc -l | tr -d ' ')
  [ "$dirty" -gt 0 ] && echo "FACT: $dirty uncommitted change(s) in working tree"

  # Large binaries and key material tracked by git.
  git -C "$REPO_ROOT" ls-files -z 2>/dev/null | tr '\0' '\n' | \
    grep -E '\.(apk|aab|jks|keystore|p12|pem|pfx)$' | \
    while read -r f; do echo "WARN: tracked file '$f' — binaries and key material should not live in git"; done
  git -C "$REPO_ROOT" ls-files -z 2>/dev/null | while IFS= read -r -d '' f; do
    [ -f "$REPO_ROOT/$f" ] || continue
    sz=$(wc -c < "$REPO_ROOT/$f")
    [ "$sz" -gt 10485760 ] && echo "WARN: tracked file '$f' is $((sz / 1048576))MB — large blobs bloat every clone; consider git-lfs or removal"
  done
else
  echo "SKIP: not a git repository"
fi

# ── .gitignore coverage per detected ecosystem ──────────────────────────
section "GITIGNORE COVERAGE"
GI="$REPO_ROOT/.gitignore"
need() { # need <pattern-regex> <label>
  if [ -f "$GI" ] && grep -qE "$1" "$GI"; then :; else
    echo "WARN: .gitignore missing $2 — add before the first local build commits noise or keys"
  fi
}
if find "$REPO_ROOT" -maxdepth 3 -name 'build.gradle*' -not -path '*/node_modules/*' 2>/dev/null | grep -q .; then
  echo "FACT: Android/Gradle project detected"
  need '(^|/)build/' "Gradle 'build/' output dirs"
  need 'local\.properties' "'local.properties' (holds SDK paths)"
  need '\*\.(jks|keystore)|\.jks|\.keystore' "'*.jks / *.keystore' signing keys"
  need '\.gradle' "'.gradle/' cache dir"
fi
if [ -f "$REPO_ROOT/package.json" ]; then
  echo "FACT: Node/Bun project detected"
  need 'node_modules' "'node_modules/'"
fi
if find "$REPO_ROOT" -maxdepth 2 \( -name 'pyproject.toml' -o -name 'requirements.txt' \) 2>/dev/null | grep -q .; then
  echo "FACT: Python project detected"
  need '__pycache__|\.venv|venv/' "'__pycache__/' and virtualenv dirs"
fi
need '^\.env$|/\.env$|\.env\b' "'.env' files"

# ── Secret patterns in tracked files ────────────────────────────────────
section "SECRET SCAN (tracked files, high-signal patterns only)"
if git -C "$REPO_ROOT" rev-parse --git-dir >/dev/null 2>&1; then
  # Only unambiguous live-credential prefixes; low-FP by design. Values are
  # never echoed — only file:line locations.
  git -C "$REPO_ROOT" grep -nIlE 'sk-ant-[a-zA-Z0-9_-]{20}|AKIA[0-9A-Z]{16}|ghp_[A-Za-z0-9]{36}|github_pat_[A-Za-z0-9_]{22}|-----BEGIN (RSA|EC|OPENSSH|PGP) PRIVATE KEY' -- \
    ':!*.md' ':!*test*' ':!*fixture*' 2>/dev/null | \
    while read -r f; do echo "WARN: possible live credential pattern in tracked file '$f' — inspect and rotate if real"; done
  echo "FACT: secret scan complete"
else
  echo "SKIP: not a git repository"
fi

echo
echo "=== SCAN COMPLETE ==="
