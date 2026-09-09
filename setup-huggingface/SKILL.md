---
name: setup-huggingface
version: 1.0.0
description: Register the Hugging Face MCP server with Claude Code so the agent can search models, datasets, and Spaces; read Hub files; run remote GPU jobs; and call HF Inference endpoints — all from within a... (gstack)
triggers:
  - setup huggingface
  - add huggingface mcp
  - connect huggingface
  - install hf mcp
  - setup hf tools
  - huggingface mcp
allowed-tools:
  - Bash
  - Read
---
<!-- AUTO-GENERATED from SKILL.md.tmpl — do not edit directly -->
<!-- Regenerate: bun run gen:skill-docs -->


## When to invoke this skill

Use when: "setup huggingface", "add huggingface mcp", "connect huggingface",
"install hf mcp", "setup hf tools".

# /setup-huggingface — Register the Hugging Face MCP Server

Connect your Claude Code session to the Hugging Face Hub so you can search
models, read datasets, inspect Spaces, and run remote GPU jobs without
leaving the agent.

```bash
mkdir -p ~/.gstack/analytics
echo '{"skill":"setup-huggingface","ts":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'","repo":"'$(basename "$(git rev-parse --show-toplevel 2>/dev/null)" 2>/dev/null || echo "unknown")'"}'  >> ~/.gstack/analytics/skill-usage.jsonl 2>/dev/null || true
```

## Step 1: Check current state

```bash
python3 -c "
import json, os
cfg = os.path.expanduser('~/.claude.json')
try:
    data = json.load(open(cfg))
    projects = data.get('projects', {})
    for proj, conf in projects.items():
        servers = conf.get('mcpServers', {})
        if 'hugging-face' in servers or 'Hugging_Face' in servers:
            print('ALREADY_REGISTERED')
            exit(0)
    print('NOT_REGISTERED')
except Exception as e:
    print(f'NOT_REGISTERED ({e})')
"
```

If the output is `ALREADY_REGISTERED`, skip to Step 3.

## Step 2: Register the HF MCP server

```bash
claude mcp add --transport http hugging-face https://huggingface.co/mcp
```

Verify it was written:

```bash
python3 -c "
import json, os
cfg = os.path.expanduser('~/.claude.json')
data = json.load(open(cfg))
for proj, conf in data.get('projects', {}).items():
    if conf.get('mcpServers', {}).get('hugging-face'):
        print('OK: registered under', proj)
"
```

## Step 3: Verify HuggingFace authentication

The HF MCP server uses your logged-in HuggingFace account. Check with:

```bash
huggingface-cli whoami 2>/dev/null || echo "huggingface-cli not installed — auth via HF_TOKEN env var or claude.ai connector settings"
```

If `huggingface-cli` is not installed, authenticate via:
- `pip install huggingface_hub && huggingface-cli login`, or
- Set `HF_TOKEN=<your_token>` in your shell environment, or
- Configure via claude.ai Settings → Connectors → Hugging Face

## Step 4: Confirm setup and show available tools

Tell the user:

```
Hugging Face MCP is registered. Restart Claude Code (or start a new session)
for the tools to load. Once loaded, you'll have access to:

  mcp__Hugging_Face__hf_fs         — browse Hub files, search models/datasets/Spaces
  mcp__Hugging_Face__hf_whoami     — check your HF identity
  mcp__Hugging_Face__hf_jobs       — run Python/GPU jobs on HF remote compute
  mcp__Hugging_Face__hub_repo_search  — search repos by query, tag, or sort
  mcp__Hugging_Face__hub_repo_details — inspect a specific repo
  mcp__Hugging_Face__create_repo   — create a new model/dataset/Space repo
  mcp__Hugging_Face__dynamic_space — run tasks on public Spaces (image gen, video, TTS)
  mcp__Hugging_Face__hf_fs_write   — write files back to Hub repos

Example: "search for trending text-to-video models on HuggingFace"
Example: "run a video generation job on HF GPU compute"
Example: "what datasets exist for fine-tuning a coding model"
```

## Notes

- The MCP server is registered per-project in `~/.claude.json`. It activates
  the next time you start Claude Code in that project directory.
- HF compute jobs (`hf_jobs`) require a HuggingFace Pro subscription or
  ZeroGPU credits. Free Space invocations (`dynamic_space`) do not.
- To remove: `claude mcp remove hugging-face`
