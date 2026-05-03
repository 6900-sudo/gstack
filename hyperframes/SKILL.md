---
name: hyperframes
preamble-tier: 2
version: 0.1.0
description: |
  Create video compositions, animations, title cards, overlays, captions,
  voiceovers, audio-reactive visuals, and scene transitions in HyperFrames HTML.
  Use when asked to build any HTML-based video content, add captions or subtitles
  synced to audio, generate text-to-speech narration, create audio-reactive
  animation (beat sync, glow, pulse driven by music), add animated text
  highlighting (marker sweeps, hand-drawn circles, burst lines, scribble,
  sketchout), or add transitions between scenes (crossfades, wipes, reveals,
  shader transitions). Covers composition authoring, timing, media, and the
  full video production workflow. For CLI commands (init, lint, preview,
  render, transcribe, tts) see the hyperframes-cli skill.
  Source: heygen-com/hyperframes (Apache-2.0)
triggers:
  - hyperframes composition
  - render video from html
  - html to video
  - create video composition
allowed-tools:
  - Bash
  - Read
  - Write
  - Edit
  - AskUserQuestion
---
<!-- AUTO-GENERATED from SKILL.md.tmpl — do not edit directly -->
<!-- Source: https://github.com/heygen-com/hyperframes (Apache-2.0) -->
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
_BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
echo "BRANCH: $_BRANCH"
_SKILL_PREFIX=$(~/.claude/skills/gstack/bin/gstack-config get skill_prefix 2>/dev/null || echo "false")
echo "PROACTIVE: $_PROACTIVE"
echo "SKILL_PREFIX: $_SKILL_PREFIX"
_TEL=$(~/.claude/skills/gstack/bin/gstack-config get telemetry 2>/dev/null || true)
_TEL_START=$(date +%s)
_SESSION_ID="$$-$(date +%s)"
echo "TELEMETRY: ${_TEL:-off}"
_EXPLAIN_LEVEL=$(~/.claude/skills/gstack/bin/gstack-config get explain_level 2>/dev/null || echo "default")
if [ "$_EXPLAIN_LEVEL" != "default" ] && [ "$_EXPLAIN_LEVEL" != "terse" ]; then _EXPLAIN_LEVEL="default"; fi
echo "EXPLAIN_LEVEL: $_EXPLAIN_LEVEL"
mkdir -p ~/.gstack/analytics
if [ "$_TEL" != "off" ]; then
echo '{"skill":"hyperframes","ts":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'","repo":"'$(basename "$(git rev-parse --show-toplevel 2>/dev/null)" 2>/dev/null || echo "unknown")'"}'  >> ~/.gstack/analytics/skill-usage.jsonl 2>/dev/null || true
fi
eval "$(~/.claude/skills/gstack/bin/gstack-slug 2>/dev/null)" 2>/dev/null || true
_LEARN_FILE="${GSTACK_HOME:-$HOME/.gstack}/projects/${SLUG:-unknown}/learnings.jsonl"
if [ -f "$_LEARN_FILE" ]; then
  _LEARN_COUNT=$(wc -l < "$_LEARN_FILE" 2>/dev/null | tr -d ' ')
  echo "LEARNINGS: $_LEARN_COUNT entries loaded"
else
  echo "LEARNINGS: 0"
fi
~/.claude/skills/gstack/bin/gstack-timeline-log '{"skill":"hyperframes","event":"started","branch":"'"$_BRANCH"'","session":"'"$_SESSION_ID"'"}' 2>/dev/null &
[ -n "$OPENCLAW_SESSION" ] && echo "SPAWNED_SESSION: true" || true
```

If `PROACTIVE` is `"false"`, do not proactively suggest gstack skills. Only run skills the user explicitly invokes.

If `SPAWNED_SESSION` is `"true"`: auto-choose recommended options, skip interactive prompts, report results via prose output.

## Model-Specific Behavioral Patch (claude)

**Todo-list discipline.** Mark each task complete individually as you finish it.

**Think before heavy actions.** For complex operations, briefly state your approach before executing.

**Dedicated tools over Bash.** Prefer Read, Edit, Write over shell equivalents.

## Completion Status Protocol

- **DONE** — All steps completed. Evidence provided.
- **DONE_WITH_CONCERNS** — Completed with issues. List each.
- **BLOCKED** — Cannot proceed. State what's blocking.
- **NEEDS_CONTEXT** — Missing information. State exactly what's needed.

## Telemetry (run last)

```bash
_TEL_END=$(date +%s)
_TEL_DUR=$(( _TEL_END - _TEL_START ))
~/.claude/skills/gstack/bin/gstack-timeline-log '{"skill":"hyperframes","event":"completed","branch":"'$(git branch --show-current 2>/dev/null || echo unknown)'","outcome":"OUTCOME","duration_s":"'"$_TEL_DUR"'","session":"'"$_SESSION_ID"'"}' 2>/dev/null || true
if [ "$_TEL" != "off" ]; then
echo '{"skill":"hyperframes","duration_s":"'"$_TEL_DUR"'","outcome":"OUTCOME","browse":"false","session":"'"$_SESSION_ID"'","ts":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"}' >> ~/.gstack/analytics/skill-usage.jsonl 2>/dev/null || true
fi
```

Replace `OUTCOME` with success/error/abort.

# HyperFrames: Video Composition Framework

HyperFrames is a system for creating video content using HTML as the source of truth. Compositions are HTML files with timing attributes, GSAP animations, and CSS styling that the framework renders into video.

## Core Workflow

**Step 1: Design System** — Check for `design.md` (the brand source of truth). If it doesn't exist, choose a named style from `visual-styles.md`, use the design picker, or pick defaults from `house-style.md`.

**Step 2: Prompt Expansion** — Ground the request against design and style guidelines using `references/prompt-expansion.md`.

**Step 3: Plan** — Define narrative arc, structure, rhythm, timing, and layout before writing code.

## Key Principles

**Layout Before Animation** — Position all elements at their most visible moment as static HTML+CSS first. Use `width: 100%; height: 100%; padding: Npx;` on `.scene-content` with flexbox. Then animate FROM offscreen/invisible TO those CSS positions using `gsap.from()`.

**Scene Transitions** — Multi-scene compositions must follow strict rules: always use transitions between scenes, entrance animations on every element (no jump cuts), and NO exit animations except on the final scene. "The transition IS the exit."

**Data Attributes** — Every clip needs `id`, `data-start`, `data-duration`, and `data-track-index`. Compositions also need `data-composition-id`, `data-width`, and `data-height`.

## Non-Negotiable Rules

- All timelines start `{ paused: true }` and register to `window.__timelines`
- Only animate visual properties (opacity, transforms, color) — never `display`, `visibility`, or media playback
- No `repeat: -1` — calculate exact repeat count from duration
- No async/setTimeout in timeline construction — the capture engine reads timelines synchronously
- No `<br>` in content text — use `max-width` for natural wrapping
- Use `gsap.from()` for entrances, never set initial state to offscreen and tween toward camera

## Quality Checks

- **Lint & Validate**: `npx hyperframes lint` and `npx hyperframes validate`
- **Layout Inspect**: `npx hyperframes inspect` for overflow/collision detection
- **Contrast**: WCAG AA compliance (4.5:1 normal, 3:1 large text)
- **Animation Map**: Verify choreography and timeline pacing via `animation-map.mjs`

---

## Approach

### Discovery (exploratory requests only)

For open-ended requests ("make me a product launch video", "create something for our brand") where the user hasn't committed to a direction, understand intent before picking colors:

- **Audience** — who watches this? Developers? Executives? General consumers?
- **Platform** — where does it play? Social (15s), website hero, product demo, internal?
- **Priority** — what matters most? Motion quality? Content accuracy? Brand fidelity? Speed?
- **Variations** — does the user want options, or a single best shot?

For specific requests ("add a title card", "fix the timing on scene 3"), skip discovery.

For exploratory requests, consider offering 2-3 variations that differ meaningfully — not just color swaps, but different pacing, energy levels, or structural approaches.

### Step 1: Design system

If `design.md` or `DESIGN.md` exists in the project, read it first. It's the source of truth for brand colors, fonts, and constraints. Use its exact values — don't invent colors or substitute fonts.

If no `design.md` exists, offer the user a choice:

1. **User named a style or mood?** → Read `visual-styles.md` for the 8 named presets.
2. **Want to browse options visually?** → Read `references/design-picker.md` for the design picker workflow.
3. **Want to skip and go fast?** → Ask: mood, light or dark, any brand colors/fonts? Then pick from `house-style.md`.

**design.md defines the brand. It does not define video composition rules.**

### Step 2: Prompt expansion

Always run on every composition (except single-scene pieces and trivial edits). Read `references/prompt-expansion.md` for the full process.

### Step 3: Plan

Before writing HTML:

1. **What** — narrative arc, key moments, emotional beats.
2. **Structure** — how many compositions, which are sub-compositions vs inline, what tracks carry what.
3. **Rhythm** — declare scene rhythm before implementing. Read `references/beat-direction.md` for rhythm templates.
4. **Timing** — which clips drive the duration, where do transitions land.
5. **Layout** — build the end-state first.
6. **Animate** — then add motion.

<HARD-GATE>
Before writing ANY composition HTML — verify you have a visual identity from Step 1. If you're reaching for `#333`, `#3b82f6`, or `Roboto`, you skipped it.
</HARD-GATE>

## Layout Before Animation

Position every element where it should be at its **most visible moment** — the frame where it's fully entered, correctly placed, and not yet exiting. Write this as static HTML+CSS first. No GSAP yet.

### The process

1. **Identify the hero frame** for each scene.
2. **Write static CSS** for that frame. The `.scene-content` container MUST fill the full scene using `width: 100%; height: 100%; padding: Npx;` with `display: flex; flex-direction: column; gap: Npx; box-sizing: border-box`. Reserve `position: absolute` for decoratives only.
3. **Add entrances with `gsap.from()`** — animate FROM offscreen/invisible TO the CSS position.
4. **Add exits with `gsap.to()`** — animate TO offscreen/invisible FROM the CSS position.

```css
.scene-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 120px 160px;
  gap: 24px;
  box-sizing: border-box;
}
```

```js
tl.from(".title", { y: 60, opacity: 0, duration: 0.6, ease: "power3.out" }, 0);
tl.from(".subtitle", { y: 40, opacity: 0, duration: 0.5, ease: "power3.out" }, 0.2);
tl.from(".logo", { scale: 0.8, opacity: 0, duration: 0.4, ease: "power2.out" }, 0.3);
// Exits — FINAL SCENE ONLY
tl.to(".title", { y: -40, opacity: 0, duration: 0.4, ease: "power2.in" }, 3);
```

## Data Attributes

### All Clips

| Attribute          | Required                          | Values                                                 |
| ------------------ | --------------------------------- | ------------------------------------------------------ |
| `id`               | Yes                               | Unique identifier                                      |
| `data-start`       | Yes                               | Seconds or clip ID reference (`"el-1"`, `"intro + 2"`) |
| `data-duration`    | Required for img/div/compositions | Seconds. Video/audio defaults to media duration.       |
| `data-track-index` | Yes                               | Integer. Same-track clips cannot overlap.              |
| `data-media-start` | No                                | Trim offset into source (seconds)                      |
| `data-volume`      | No                                | 0-1 (default 1)                                        |

`data-track-index` does **not** affect visual layering — use CSS `z-index`.

### Composition Clips

| Attribute                    | Required | Values                                       |
| ---------------------------- | -------- | -------------------------------------------- |
| `data-composition-id`        | Yes      | Unique composition ID                        |
| `data-start`                 | Yes      | Start time (root composition: use `"0"`)     |
| `data-duration`              | Yes      | Takes precedence over GSAP timeline duration |
| `data-width` / `data-height` | Yes      | Pixel dimensions (1920x1080 or 1080x1920)    |
| `data-composition-src`       | No       | Path to external HTML file                   |

## Composition Structure

Sub-compositions use a `<template>` wrapper. **Standalone compositions do NOT use `<template>`.**

```html
<template id="my-comp-template">
  <div data-composition-id="my-comp" data-width="1920" data-height="1080">
    <style>[data-composition-id="my-comp"] { /* scoped styles */ }</style>
    <script src="https://cdn.jsdelivr.net/npm/gsap@3.14.2/dist/gsap.min.js"></script>
    <script>
      window.__timelines = window.__timelines || {};
      const tl = gsap.timeline({ paused: true });
      window.__timelines["my-comp"] = tl;
    </script>
  </div>
</template>
```

Load in root:
```html
<div id="el-1" data-composition-id="my-comp" data-composition-src="compositions/my-comp.html"
     data-start="0" data-duration="10" data-track-index="1"></div>
```

## Video and Audio

```html
<video id="el-v" data-start="0" data-duration="30" data-track-index="0"
       src="video.mp4" muted playsinline></video>
<audio id="el-a" data-start="0" data-duration="30" data-track-index="2"
       src="video.mp4" data-volume="1"></audio>
```

## Timeline Contract

- All timelines start `{ paused: true }` — the player controls playback
- Register: `window.__timelines["<composition-id>"] = tl`
- Framework auto-nests sub-timelines — do NOT manually add them
- Duration comes from `data-duration`, not GSAP timeline length
- Never create empty tweens to set duration

## Rules (Non-Negotiable)

**Deterministic:** No `Math.random()`, `Date.now()`, or time-based logic. Use seeded PRNG (e.g. mulberry32) if needed.

**GSAP:** Only animate visual properties. Do NOT animate `visibility`, `display`, or call `video.play()`/`audio.play()`.

**No `repeat: -1`:** `repeat: Math.ceil(duration / cycleDuration) - 1`

**Synchronous timeline construction:** Never inside `async`/`await`, `setTimeout`, or Promises.

**Never do:**
1. Forget `window.__timelines` registration
2. Use video for audio — always muted video + separate `<audio>`
3. Nest video inside a timed div
4. Use `data-layer` or `data-end`
5. Animate video dimensions — animate a wrapper
6. Call play/pause/seek on media
7. Use `repeat: -1`
8. Build timelines asynchronously
9. Use `gsap.set()` on later-scene clips — use `tl.set()` inside the timeline
10. Use `<br>` in content text

## Scene Transitions (Non-Negotiable)

1. **ALWAYS use transitions between scenes.** No jump cuts.
2. **ALWAYS entrance-animate every element** via `gsap.from()`.
3. **NEVER exit-animate** except on the final scene. The transition IS the exit.
4. **Final scene only** may fade out.

```js
// WRONG — empties scene before transition
tl.to("#s1-title", { opacity: 0, y: -40, duration: 0.4 }, 6.5);

// RIGHT — entrance only, transition handles exit
tl.from("#s1-title", { y: 50, opacity: 0, duration: 0.7, ease: "power3.out" }, 0.3);
tl.from("#s2-heading", { x: -40, opacity: 0, duration: 0.6, ease: "expo.out" }, 8.0);
```

## Animation Guardrails

- Offset first animation 0.1-0.3s (not t=0)
- Vary eases — at least 3 different eases per scene
- Don't repeat an entrance pattern within a scene
- Avoid full-screen linear gradients on dark backgrounds (H.264 banding)
- 60px+ headlines, 20px+ body, 16px+ data labels
- `font-variant-numeric: tabular-nums` on number columns

## Typography and Assets

- **Built-in fonts:** Write `font-family` — the compiler embeds supported fonts automatically.
- **Custom fonts:** Provide `.woff2` files in `fonts/`. Warn if missing before writing HTML.
- Add `crossorigin="anonymous"` to external media
- Dynamic text: `window.__hyperframes.fitTextFontSize(text, { maxWidth, fontFamily, fontWeight })`
- All files at project root; sub-compositions use `../`

## Editing Existing Compositions

- **Read actual files, don't guess.** Extract exact hex codes and easing patterns from source.
- Match existing fonts, colors, animation patterns
- Only change what was requested
- Preserve timing of unrelated clips

## Output Checklist

**Fast (block on results):**
- [ ] `npx hyperframes lint` passes
- [ ] `npx hyperframes validate` passes
- [ ] Design adherence verified if design.md exists

**Slow (run in parallel):**
- [ ] `npx hyperframes inspect` passes
- [ ] Contrast warnings addressed
- [ ] Animation map reviewed

## Quality Checks

### Visual Inspect

```bash
npx hyperframes inspect
npx hyperframes inspect --json           # machine-readable
npx hyperframes inspect --samples 15     # dense videos
npx hyperframes inspect --at 1.5,4,7.25 # specific frames
```

Fix overflows: increase container size/padding, reduce font size, add `max-width`, or use `fitTextFontSize()`. Mark intentional overflow with `data-layout-allow-overflow`, decoratives with `data-layout-ignore`.

### Contrast

`hyperframes validate` runs WCAG audit. Adjust existing palette colors — don't invent new ones. Re-run until clean.

### Design Adherence

If `design.md` exists, verify: colors, typography, corners, spacing, depth, avoidance rules. Fix all before serving.

If no `design.md`: verify palette consistency and check against house-style.md's "Lazy Defaults to Question" list.

### Animation Map

```bash
node skills/hyperframes/scripts/animation-map.mjs <composition-dir> \
  --out <composition-dir>/.hyperframes/anim-map
```

Outputs `animation-map.json` with per-tween summaries, ASCII Gantt chart, stagger detection, dead zones, element lifecycles, scene snapshots, and flags (`offscreen`, `collision`, `invisible`, `paced-fast`, `paced-slow`). Fix or justify every flag.

Skip on small edits. Run on new compositions and significant animation changes.

---

## References (loaded on demand)

- `references/captions.md` — Captions, subtitles, lyrics, karaoke synced to audio
- `references/tts.md` — Text-to-speech with Kokoro-82M
- `references/audio-reactive.md` — Audio-reactive animation (frequency bands → GSAP)
- `references/css-patterns.md` — CSS+GSAP highlighting: marker, circle, burst, scribble, sketchout
- `references/video-composition.md` — Video-medium rules. **Always read.**
- `references/beat-direction.md` — Beat planning, rhythm templates. **Always read for multi-scene.**
- `references/typography.md` — Font pairing, OpenType features. **Always read.**
- `references/motion-principles.md` — Motion design, load-bearing GSAP rules. **Always read.**
- `references/techniques.md` — 11 visual techniques: SVG, Canvas, CSS 3D, kinetic type, Lottie, video compositing, typing effect, variable fonts, MotionPath, velocity transitions, audio-reactive
- `references/narration.md` — Pacing, tone, script structure
- `references/design-picker.md` — Create design.md via visual picker
- `visual-styles.md` — 8 named visual styles with hex palettes and GSAP easing signatures
- `house-style.md` — Default motion, sizing, and color palettes
- `patterns.md` — PiP, title cards, slideshow patterns
- `data-in-motion.md` — Data, stats, infographic patterns
- `references/transitions.md` — Scene transitions: crossfades, wipes, reveals, shaders. **Always read for multi-scene.**
- `references/dynamic-techniques.md` — Dynamic caption animation techniques

GSAP patterns and effects: `/gsap` skill.
