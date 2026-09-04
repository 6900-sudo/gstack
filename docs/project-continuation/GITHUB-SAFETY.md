# GitHub Safety Policy

## Non-negotiable repository boundary

For gstack work:

- `6900-sudo/gstack` = writable fork.
- Original/upstream gstack = read-only reference.

No operation may write to upstream/original gstack.

## Pre-write gate

Before every GitHub mutation or local push, verify all of the following:

1. Repository full name is exactly `6900-sudo/gstack`.
2. Current branch is intentional.
3. The planned change is understood.
4. Relevant diff/status has been inspected where available.
5. Tests/builds appropriate to the change have run or are explicitly marked not run.

If any check fails or cannot be established, do not write.

## Local Git remote pattern

When working from a local clone, use a topology where the user's fork is the push destination and upstream is fetch/reference only. Example:

```bash
git remote -v
```

Expected intent:

```text
origin   https://github.com/6900-sudo/gstack.git (fetch)
origin   https://github.com/6900-sudo/gstack.git (push)
upstream <original-gstack-url> (fetch)
```

If upstream has a push URL, remove/disable that push path before doing normal work. One safe Git mechanism is to assign a deliberately invalid upstream push URL locally:

```bash
git remote set-url --push upstream DISABLED
```

Then verify again:

```bash
git remote -v
```

Do not copy these commands blindly into another repository; first verify repository identity and remote names.

## Branch strategy

Prefer feature/documentation branches in `6900-sudo/gstack` instead of direct edits to `main` for non-trivial changes.

Examples:

```text
docs/project-continuation-pack
fix/<specific-problem>
feat/<specific-feature>
```

## Before push

Run:

```bash
git status
git branch --show-current
git remote -v
git diff --check
```

Then run the project-appropriate tests/build.

Only push when the displayed destination is the user's `6900-sudo/gstack` fork.

## Forbidden actions

Unless a later explicit instruction changes the fork itself, never:

- push to upstream/original gstack
- force-push upstream
- create commits directly in upstream through an API
- update upstream refs
- merge a fork branch into upstream
- create or update upstream files
- use credentials to bypass the write boundary

## Recovery if a wrong-target write is suspected

Do not attempt a destructive correction immediately.

1. Stop further writes.
2. Record repository, branch, commit SHA, and operation.
3. Inspect whether a write actually occurred.
4. Preserve the fork's known-good state.
5. Choose the least destructive correction appropriate to the exact event.
6. Verify history and remotes afterwards.

Never use a force-push as an automatic cleanup mechanism.
