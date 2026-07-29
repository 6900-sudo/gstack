# Prepper Dropshipping Video

A Remotion-based animated explainer video showcasing a dropshipping business model for emergency preparedness products (freeze-dried food, water filters, first-aid kits, etc.).

**Output**: 1080×1920 vertical video (~39 seconds @ 30fps) suitable for social media, landing pages, or educational content.

## Quick Start

```bash
# Install dependencies
npm install

# Launch Remotion Studio (interactive editor)
npm start

# Build video to out/prepper-dropshipping.mp4
npm run build

# Check for package updates
npm run upgrade
```

**Prerequisites**: Node.js 16+ (18+ recommended for performance)

---

## Project Structure

```
src/
├── index.ts              # Remotion registration entry point
├── Root.tsx              # Composition config (dimensions, FPS, duration)
├── HowTo.tsx             # Main timeline orchestration (all 5 scenes + transitions)
├── constants.ts          # Colors, fonts, timing, safe zones
├── components/
│   ├── SpringIn.tsx      # Entrance animation with spring physics
│   ├── AnimatedPath.tsx  # SVG stroke draw-on effect
│   ├── CountUp.tsx       # Animated number counter
│   ├── ParticleBackground.tsx  # Floating particle layer
│   ├── SafeArea.tsx      # Safe-zone wrapper (TV/phone margins)
│   ├── Icons.tsx         # 7 custom SVG icons (Tent, Water Drop, etc.)
│   └── fonts.ts          # Font imports (@fontsource/inter self-hosted)
├── scenes/
│   ├── Scene1Hook.tsx    # Hook: $2B+ market, bar chart, warehouse ✗
│   ├── Scene2Niche.tsx   # Niche selection → supplier flow diagram
│   ├── Scene3BuildStore.tsx  # Store builder UI mockup + checkout
│   ├── Scene4Pricing.tsx      # Cost → margin calculation walkthrough
│   └── Scene5Launch.tsx       # Checklist + CTA button
├── remotion.config.ts    # Remotion CLI config (JPEG format, overwrite)
├── package.json
├── tsconfig.json
└── .gitignore
```

---

## Scene Breakdown

| Scene | Duration | Key Elements | Message |
|-------|----------|--------------|---------|
| **1: Hook** | 240 frames (8s) | $2B+ headline, bar chart growth, warehouse ✗, $0 inventory badge | Market is large; no inventory needed |
| **2: Niche** | 240 frames (8s) | "Choose a Niche" → 3 options (Water/Food/First-Aid), flow to Supplier | Strategy: pick narrow category, find dropship partner |
| **3: Build Store** | 240 frames (8s) | Browser window mockup, 3 product cards, checkout button | Easy to set up (no coding required) |
| **4: Pricing** | 239 frames (8s) | Cost card ($10) → your price ($29.99), +199% margin badge | Profitability is straightforward |
| **5: Launch** | 239 frames (8s) | Checklist (3 items), particle background, "Start Today" CTA | Clear next steps; action-oriented |

**Transitions**: 12 frames fade between each scene (cuts 48 frames from total).  
**Total**: 1,150 frames at 30fps ≈ 38.3 seconds.

---

## Timing & Frame Calculations

```typescript
// constants.ts
export const SCENE_DURATIONS = [240, 240, 240, 239, 239];  // In frames
export const TRANSITION_FRAMES = 12;  // Fade duration
export const FPS = 30;

// Total calculation (HowTo.tsx):
// 240 + 240 + 240 + 239 + 239 = 1198 frames (raw)
// − (12 × 4 transitions) = 1150 frames (with overlaps)
// ÷ 30 fps = 38.3 seconds
```

**Why these durations?**
- Earlier cuts (190 frames/scene) felt rushed; staggered animations barely settled before fade.
- 240/239 gives readers 3–4 extra seconds per scene to absorb copy and visuals.
- Last scene slightly shorter to respect viewer attention decay.

---

## Animation Patterns

### SpringIn Component
Entrance animation with spring physics (no linear easing).
```tsx
<SpringIn delay={0} from={{opacity: 0, y: 40, scale: 0.9}}>
  {children}
</SpringIn>
```
- **delay**: Frame offset before animation starts
- **from**: Starting state (opacity, Y offset, scale)
- Uses `spring()` with `damping: 200` (medium-stiff)

### AnimatedPath Component
SVG stroke draws from invisible to full in a time window.
```tsx
<AnimatedPath
  d="M 50 15 L 90 85 H 10 Z"  // SVG path
  stroke="#6366f1"
  strokeWidth={4}
  startFrame={20}              // When to begin
  durationInFrames={22}        // How long to draw
/>
```
- Measures total path length on mount
- Uses `strokeDasharray` + `strokeDashoffset` for draw effect
- Clamps progress outside [startFrame, endFrame] to prevent overflow

### CountUp Component
Animated number counter with fixed-width font.
```tsx
<CountUp
  from={0} to={29.99}
  startFrame={95} durationInFrames={30}
  prefix="$" decimals={2}
/>
```
- Interpolates frame position to numeric value
- `fontVariantNumeric: 'tabular-nums'` prevents width shift during count

---

## Customization Guide

### Change Colors
Edit `src/constants.ts`:
```typescript
export const COLORS = {
  background: '#0a0a0a',  // Dark gray
  text: '#ffffff',        // White
  accent: '#6366f1',      // Indigo
  success: '#22c55e',     // Green
  muted: '#9ca3af',       // Gray-400
};
```

### Adjust Timing
- Scene durations: `SCENE_DURATIONS` array
- Transition fade speed: `TRANSITION_FRAMES`
- Individual element delays: `delay={}` prop in each `<SpringIn>`
- Spring stiffness: Edit `damping` in `src/components/SpringIn.tsx`

### Change Video Dimensions
```typescript
// src/constants.ts
export const WIDTH = 1080;   // Change for landscape/square
export const HEIGHT = 1920;  // 9:16 is mobile-optimized
export const FPS = 30;       // 24 or 60 also common
```

### Modify Safe Zone
For TV/streaming safe zone adjustment:
```typescript
export const SAFE = {
  top: 150,     // Adjust for title bar placement
  bottom: 170,  // Adjust for bottom bar/UI
  side: 60,     // Adjust for left/right margins
};
```

---

## Performance Tips

1. **Particle Background**: Limits to 13 particles (hardcoded in `src/components/ParticleBackground.tsx`). Increase to 20–25 if GPU allows, but monitor memory.

2. **Font Loading**: Uses `@fontsource/inter` (self-hosted) rather than Google Fonts CDN. Font files ship in `node_modules/`, avoiding network calls.

3. **SVG Icons**: Custom-drawn in JSX (not image files). Extremely lightweight and scalable.

4. **Avoid**: 
   - Large background images (use solid colors + particle layer instead)
   - Real video layers (use animation + shapes for smooth 30fps)
   - Network requests in render functions

---

## Build Output

```bash
npm run build
# → out/prepper-dropshipping.mp4 (H.264 video, JPEG frames)
```

**Configuration** (`remotion.config.ts`):
```typescript
Config.setVideoImageFormat('jpeg');  // Smaller files, acceptable quality
Config.setOverwriteOutput(true);     // Overwrite existing MP4 without prompt
```

**Output specs**:
- Codec: H.264 (widely compatible)
- Format: MP4 (works on all platforms)
- Resolution: 1080×1920 (vertical, Instagram/TikTok ready)
- Frame rate: 30fps
- File size: ~15–25 MB (depends on complexity)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **"Cannot find module @remotion/..."** | Run `npm install` (dependencies may be missing) |
| **Video plays at wrong speed** | Check `FPS` in `constants.ts` matches render command |
| **Text blurry on export** | Ensure font sizes are whole numbers; use `fontSmoothing: 'antialiased'` |
| **Animations jank/stutter** | Reduce `PARTICLE_COUNT` in `ParticleBackground.tsx` or simplify SVG paths |
| **Spring animations feel off** | Tweak `damping` in `SpringIn.tsx` (higher = stiffer, lower = bouncier) |
| **Memory errors during build** | Build on a machine with 8GB+ RAM; close other apps |

---

## Licensing & Usage

This video is designed as a **marketing/educational asset** for a dropshipping course or product landing page.

- **Music/SFX**: Not included (add via post-production or Remotion's sound API)
- **Fonts**: Inter (free, OFL 1.1 license via @fontsource)
- **Icons**: Custom-drawn (no external attribution required)

For commercial use, ensure all third-party assets (if added) are licensed appropriately.

---

## Development Workflow

1. **Edit scene content**: Modify `src/scenes/Scene*.tsx` files
2. **Preview in Studio**: `npm start` → click "Refresh" or save to hot-reload
3. **Adjust timing**: Open DevTools, check frame numbers in Remotion's player controls
4. **Export**: `npm run build` when satisfied
5. **Post-production**: Add music, sound effects, captions in your favorite video editor

---

## Further Reading

- [Remotion Documentation](https://www.remotion.dev/docs)
- [React Hooks in Remotion](https://www.remotion.dev/docs/react-hooks-rules)
- [SVG Animations Best Practices](https://www.remotion.dev/docs/svg)
- [@fontsource](https://fontsource.org/) for self-hosted fonts

---

**Last updated**: 2026-07-29  
**Remotion version**: 4.0.286  
**React version**: 18.3.1
