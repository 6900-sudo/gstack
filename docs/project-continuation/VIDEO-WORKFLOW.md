# Video Production Workflow

Use this workflow for evidence-led vertical reels, shorts, YouTube pieces, and supporting public-interest video.

## 1. Define the deliverable

Record before production:

- platform(s)
- target aspect ratio
- target duration
- narration voice/tool
- source material
- caption requirements
- output filename/format

Default short-form target when no other requirement applies: vertical 9:16, typically 1080x1920, approximately 60–90 seconds.

## 2. Evidence first

Before writing the script, build a source list for every material factual claim. For controversial material, complete the sensitive-claim/legal-status check before recording narration.

Do not create a stronger spoken allegation than the source material supports.

## 3. Script

Preferred structure:

1. consequence/contradiction/evidence hook
2. essential context
3. strongest documentary evidence
4. what changed
5. who pays/benefits or who controls the data
6. unresolved question/counter-evidence
7. concise conclusion

Avoid generic introductions and unnecessary hype.

## 4. Duration check

Estimate narration duration before generating assets. Shorten or restructure the script before rendering if it materially exceeds the platform target.

Do not solve an overlong script simply by making narration unnaturally fast.

## 5. Scene plan

For every scene record:

- time range
- narration
- visual purpose
- source/asset
- on-screen text
- transition if needed

Use source screenshots selectively. Do not overload the screen with unreadable documents.

## 6. Asset validation

Before assembly:

- confirm every referenced asset exists
- confirm images/video decode successfully
- confirm fonts are available or use a safe fallback
- confirm source screenshots are legible
- confirm music licensing/usage is appropriate
- confirm narration file exists and duration is plausible

## 7. Test render first

Before a full MP4 render, produce a short representative segment that includes:

- narration
- captions
- at least one image/video transition
- headline/text overlay
- background audio if used

Inspect it visually and audibly.

Do not proceed to a full render if the test has missing assets, black frames, clipped captions, distorted audio, or timing errors.

## 8. Full render

Render only after the test segment passes.

For FFmpeg/Remotion or equivalent workflows, treat warnings/errors as evidence to inspect rather than noise to ignore.

## 9. Technical validation

Where `ffprobe` is available, inspect the final output for:

- duration
- width/height
- aspect ratio
- video codec
- frame rate
- audio codec
- audio channels

Also verify:

- file exists
- file is non-zero and plausible size
- narration is audible
- captions remain within safe areas
- no unexpected black/frozen frames
- end is not truncated

## 10. Editorial validation

Watch the complete result once before declaring it final.

Check:

- hook works without misleading
- claims match sources
- names/dates/numbers are correct
- captions match narration
- source attributions are readable
- pacing is understandable
- conclusion distinguishes established fact from inference

## 11. Failure recovery

If rendering fails:

1. preserve the script, assets, and last working project state
2. capture the exact error
3. identify whether it is a missing file, codec, filter, font, timing, dependency, memory, or path issue
4. reproduce with the smallest possible test
5. fix the isolated cause
6. rerun the short test
7. only then rerun the full production render

Do not rewrite the whole pipeline because of one filter/path error.

## 12. Final delivery checklist

- script finalised and duration checked
- source list retained
- narration generated and inspected
- captions generated/proofread
- assets verified
- short test render passed
- full render completed
- technical metadata checked
- complete video watched once
- exact output path recorded
- anything not tested clearly stated
