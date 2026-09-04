# Project Rules

These rules apply to all software, GitHub, Colab, Android, Termux, FFmpeg, Python, JavaScript/TypeScript, shell, and automation work in this project.

## 1. Repository write boundary

The upstream/original gstack repository is **read-only**.

Permitted gstack write target: **`6900-sudo/gstack` only**.

Before any GitHub mutation, verify the repository identifier is exactly `6900-sudo/gstack`. A write includes creating/updating/deleting files, branches, refs, issues/PR metadata associated with code changes, commits, merges, or any equivalent push operation.

If repository identity is uncertain: **STOP. DO NOT WRITE.**

Never force-push upstream. Never redirect a branch or PR so that a fork-only change writes into upstream.

## 2. Inspect first

Before editing code:

1. Identify repository and branch.
2. Inspect the file tree.
3. Read README and relevant documentation.
4. Inspect dependency/build files.
5. Inspect relevant source files.
6. Inspect tests and CI.
7. Inspect open PR context if the work is PR-related.

Do not guess project structure.

## 3. Preserve the last known-good state

Before substantial changes, preserve working code with a branch, commit, or safe backup. Do not overwrite the only working version.

Prefer a descriptive working branch such as `fix/...`, `feat/...`, or `docs/...`.

## 4. Smallest-change rule

Prefer:

`one problem -> one change -> one test -> one verification`

Do not rewrite a whole application to fix one isolated defect unless the architecture truly requires it.

## 5. Validation matrix

- Python: parse/compile/import checks plus relevant tests.
- Shell: syntax check where supported, then controlled execution.
- JS/TS: lint/typecheck/tests/build as available.
- Android: Gradle checks plus actual APK build.
- JSON/YAML: parser/linter validation.
- Video: short test render before full render; inspect with ffprobe where available.

Passing unit tests alone does not prove the product builds or runs.

## 6. Baseline and regression

Run relevant existing tests before changes when possible and record pre-existing failures. After changes, repeat the same checks and add targeted tests for the changed behaviour.

Never attribute an existing failure to the new change without evidence.

## 7. Secrets

Never commit secrets. Prefer environment variables, Colab Secrets, GitHub Secrets, or ignored local configuration. Confirm `.gitignore` behaviour before creating local secret files. Never print a secret merely to prove it loaded.

## 8. Destructive operations

Avoid destructive commands and irreversible migrations. In particular, do not use `git reset --hard`, `git clean -fd`, broad `rm -rf`, destructive database migrations, or force-pushes unless explicitly required and the impact has been verified.

Prefer reversible changes and narrow paths.

## 9. Output inspection

A successful command is not enough. Check that expected files exist, are non-empty, use the expected format, contain required assets, and have plausible size/content.

For MP4 output, inspect duration, resolution, codecs, frame rate, and audio presence when tooling permits.

## 10. Failure handling

When a step fails:

1. Capture the exact error.
2. Identify the failing command/action.
3. Preserve current state.
4. Classify likely cause: syntax, dependency, path, permissions, authentication, version, network, API, or logic.
5. Fix the smallest root cause.
6. Repeat the failing check.
7. Run regression checks before continuing.

If a new change breaks a previously working path, revert that new change to the last known-good state before attempting a broader redesign.

## 11. Definition of done

Where applicable, DONE means:

- code saved
- syntax/type validation passed
- dependencies resolved
- relevant tests passed
- actual build passed
- output produced and inspected
- important failure paths tested
- secrets protected
- documentation updated
- exact changed files identified

Anything not tested must be stated explicitly.

## 12. Checkpoint format

For substantial technical work, report:

**Working:** what currently works.

**Tested:** checks actually run.

**Not yet tested:** remaining verification.

**Current risk:** concrete remaining risk.

**Next smallest step:** the next reversible action.
