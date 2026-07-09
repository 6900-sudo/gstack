#!/bin/bash
# SessionStart hook for Claude Code on the web: import gstack skills into the
# session container so /gstack-ship, /gstack-review, /gstack-qa etc. are
# available. Local (non-web) sessions manage their own global install via
# ./setup, so this exits immediately there.
#
# Setup is run from a clone at ~/.gstack/repos/gstack rather than the
# workspace itself: ./setup patches the name: frontmatter of every SKILL.md
# at install time (gstack- prefix), which would dirty the workspace tree and
# fail test/skill-validation.test.ts. The clone absorbs those install-time
# patches; the workspace stays pristine.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

INSTALL_DIR="$HOME/.gstack/repos/gstack"
BRANCH="$(git -C "$CLAUDE_PROJECT_DIR" rev-parse --abbrev-ref HEAD 2>/dev/null || echo HEAD)"

if [ -d "$INSTALL_DIR/.git" ] && git -C "$INSTALL_DIR" fetch --quiet "$CLAUDE_PROJECT_DIR" "$BRANCH" 2>/dev/null; then
  git -C "$INSTALL_DIR" reset --hard --quiet FETCH_HEAD
else
  rm -rf "$INSTALL_DIR"
  mkdir -p "$(dirname "$INSTALL_DIR")"
  git clone --quiet "$CLAUDE_PROJECT_DIR" "$INSTALL_DIR"
fi

cd "$INSTALL_DIR"

bun install --frozen-lockfile 2>/dev/null || bun install

# The web container blocks browser downloads (cdn.playwright.dev is not on the
# proxy allowlist) but pre-installs Chromium under PLAYWRIGHT_BROWSERS_PATH.
# If the repo's pinned Playwright wants a different build revision, alias the
# pre-installed build to the expected directory layout so `chromium.launch()`
# works and ./setup doesn't attempt a doomed `playwright install`.
shim_preinstalled_chromium() {
  local bp="${PLAYWRIGHT_BROWSERS_PATH:-}"
  [ -n "$bp" ] && [ -d "$bp" ] && [ -w "$bp" ] || return 0
  local rev
  rev=$(bun -e 'console.log(JSON.parse(require("fs").readFileSync("node_modules/playwright-core/browsers.json","utf8")).browsers.find(b=>b.name==="chromium").revision)' 2>/dev/null) || return 0
  [ -n "$rev" ] || return 0

  local arch subdir_chrome subdir_shell
  arch="$(uname -m)"
  if [ "$arch" = "aarch64" ] || [ "$arch" = "arm64" ]; then
    subdir_chrome="chrome-linux" subdir_shell="chrome-linux"
  else
    subdir_chrome="chrome-linux64" subdir_shell="chrome-headless-shell-linux64"
  fi

  _alias_build() {
    local name="$1" subdir="$2" exe="$3" srcexe="$4"
    local target="$bp/${name}-${rev}"
    [ -e "$target/$subdir/$exe" ] && return 0
    local src srcbin
    src=$(ls -d "$bp/${name}-"* 2>/dev/null | head -1) || return 0
    [ -n "$src" ] || return 0
    srcbin=$(find "$src" \( -type f -o -type l \) -name "$srcexe" 2>/dev/null | head -1)
    [ -n "$srcbin" ] || return 0
    mkdir -p "$target/$subdir"
    ln -sf "$srcbin" "$target/$subdir/$exe"
    touch "$target/INSTALLATION_COMPLETE" "$target/DEPENDENCIES_VALIDATED"
    echo "aliased $srcbin -> $target/$subdir/$exe"
  }

  _alias_build chromium "$subdir_chrome" chrome chrome
  _alias_build chromium_headless_shell "$subdir_shell" chrome-headless-shell headless_shell
}
shim_preinstalled_chromium

# Build binaries + link every skill into ~/.claude/skills/ (idempotent; the
# container caches state after the hook completes, so rebuilds are skipped on
# subsequent sessions unless sources changed).
./setup --host claude --prefix --no-team --no-plan-tune-hooks
