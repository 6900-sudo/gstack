#!/usr/bin/env bash
# Install The Agency AI specialists from msitarzewski/agency-agents
# into .claude/agents/ for use as Claude Code subagents.
set -euo pipefail

REPO_URL="https://github.com/msitarzewski/agency-agents.git"
AGENTS_DIR="${AGENTS_DIR:-.claude/agents}"
TMPDIR="$(mktemp -d)"
trap 'rm -rf "$TMPDIR"' EXIT

echo "Installing The Agency specialists..."

git clone --depth 1 --quiet "$REPO_URL" "$TMPDIR/src"

mkdir -p "$AGENTS_DIR"

# Division list from agency-agents/divisions.json
DIVISIONS=(
  academic design engineering finance game-development gis
  healthcare marketing paid-media product project-management
  research sales security spatial-computing specialized support testing
)

installed=0
for dir in "${DIVISIONS[@]}"; do
  src="$TMPDIR/src/$dir"
  [ -d "$src" ] || continue
  for f in "$src"/*.md; do
    [ -f "$f" ] || continue
    cp "$f" "$AGENTS_DIR/"
    installed=$((installed + 1))
  done
done

echo "Installed $installed agents to $AGENTS_DIR/"
echo ""
echo "Activate in Claude Code:"
echo '  "Activate Frontend Developer and help me build a React component."'
echo '  "Use the Reality Checker to verify this feature is production-ready."'
echo '  "Use the Security Engineer to audit this endpoint."'
echo ""
echo "Run this script again to update to the latest agents."
