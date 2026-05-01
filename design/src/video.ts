/**
 * Video generation via Replicate API using ByteDance Seedance 2.0.
 *
 * Seedance 2.0 supports: text-to-video, image-to-video, multimodal reference
 * inputs (images, videos, audio), native audio generation, and intelligent
 * duration control.
 *
 * Replicate predictions are async. We POST to create the prediction, then poll
 * every 5s until status is "succeeded" or "failed", then download the MP4.
 */

import fs from "fs";
import path from "path";
import { requireReplicateToken } from "./replicate-auth";

const MODEL = "bytedance/seedance-2.0";
const REPLICATE_API = "https://api.replicate.com/v1";
const POLL_INTERVAL_MS = 5_000;
const TIMEOUT_MS = 600_000; // 10 minutes — video gen can be slow

export interface VideoOptions {
  prompt: string;
  image?: string;           // Path or URL for first frame (image-to-video)
  lastFrameImage?: string;  // Path or URL for last frame
  duration?: number;        // Seconds, or -1 for intelligent duration
  resolution?: string;      // "480p" | "720p"
  aspectRatio?: string;     // "16:9" | "9:16" | "4:3" | "1:1" | "3:4" | "21:9" | "adaptive"
  generateAudio?: boolean;  // Default true
  seed?: number;
  output: string;           // Output file path (.mp4)
}

export interface VideoResult {
  outputPath: string;
  predictionId: string;
  durationMs: number;
}

type PredictionStatus = "starting" | "processing" | "succeeded" | "failed" | "canceled";

interface Prediction {
  id: string;
  status: PredictionStatus;
  output?: string;
  error?: string;
  urls: { get: string };
}

/**
 * Convert a local file to a base64 data URI for Replicate input.
 * If the input is already a URL, return it unchanged.
 */
function toReplicateInput(filePath: string): string {
  if (filePath.startsWith("http://") || filePath.startsWith("https://") || filePath.startsWith("data:")) {
    return filePath;
  }
  const data = fs.readFileSync(filePath);
  const ext = path.extname(filePath).slice(1).toLowerCase();
  const mime = ext === "jpg" || ext === "jpeg" ? "image/jpeg"
    : ext === "png" ? "image/png"
    : ext === "webp" ? "image/webp"
    : "application/octet-stream";
  return `data:${mime};base64,${data.toString("base64")}`;
}

/**
 * Create a Replicate prediction for Seedance 2.0.
 */
async function createPrediction(token: string, input: Record<string, unknown>): Promise<Prediction> {
  const response = await fetch(`${REPLICATE_API}/models/${MODEL}/predictions`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ input }),
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(`Replicate API error (${response.status}): ${error.slice(0, 300)}`);
  }

  return response.json() as Promise<Prediction>;
}

/**
 * Poll a prediction until it succeeds, fails, or times out.
 */
async function pollPrediction(token: string, prediction: Prediction): Promise<Prediction> {
  const deadline = Date.now() + TIMEOUT_MS;
  let current = prediction;

  while (current.status === "starting" || current.status === "processing") {
    if (Date.now() > deadline) {
      throw new Error(`Prediction timed out after ${TIMEOUT_MS / 1000}s (id: ${current.id})`);
    }

    await new Promise(resolve => setTimeout(resolve, POLL_INTERVAL_MS));

    const response = await fetch(current.urls.get, {
      headers: { "Authorization": `Bearer ${token}` },
    });

    if (!response.ok) {
      throw new Error(`Poll error (${response.status}): ${await response.text().then(t => t.slice(0, 200))}`);
    }

    current = await response.json() as Prediction;
    process.stderr.write(`  status: ${current.status}\n`);
  }

  return current;
}

/**
 * Download the generated video to disk.
 */
async function downloadVideo(url: string, outputPath: string): Promise<void> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Download failed (${response.status}): ${url}`);
  }
  const buffer = await response.arrayBuffer();
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, Buffer.from(buffer));
}

/**
 * Generate a video using Seedance 2.0 via Replicate.
 */
export async function generateVideo(options: VideoOptions): Promise<VideoResult> {
  const token = requireReplicateToken();

  const input: Record<string, unknown> = {
    prompt: options.prompt,
    duration: options.duration ?? -1,
    resolution: options.resolution ?? "720p",
    aspect_ratio: options.aspectRatio ?? "16:9",
    generate_audio: options.generateAudio ?? true,
  };

  if (options.seed !== undefined) {
    input.seed = options.seed;
  }

  if (options.image) {
    input.image = toReplicateInput(options.image);
  }

  if (options.lastFrameImage) {
    if (!options.image) {
      throw new Error("--last-frame requires --image (first frame must also be specified)");
    }
    input.last_frame_image = toReplicateInput(options.lastFrameImage);
  }

  console.error(`Creating Seedance 2.0 prediction...`);
  const startTime = Date.now();

  const prediction = await createPrediction(token, input);
  console.error(`  prediction id: ${prediction.id}`);
  console.error(`  polling every ${POLL_INTERVAL_MS / 1000}s...`);

  const completed = await pollPrediction(token, prediction);

  if (completed.status !== "succeeded") {
    throw new Error(`Prediction failed (${completed.status}): ${completed.error || "unknown error"}`);
  }

  const videoUrl = completed.output;
  if (!videoUrl || typeof videoUrl !== "string") {
    throw new Error(`No output URL in completed prediction (id: ${completed.id})`);
  }

  console.error(`  downloading video...`);
  await downloadVideo(videoUrl, options.output);

  const durationMs = Date.now() - startTime;
  const sizeMb = (fs.statSync(options.output).size / 1024 / 1024).toFixed(1);
  console.error(`Generated (${(durationMs / 1000).toFixed(1)}s, ${sizeMb}MB) → ${options.output}`);

  const result: VideoResult = {
    outputPath: options.output,
    predictionId: completed.id,
    durationMs,
  };

  console.log(JSON.stringify(result, null, 2));
  return result;
}
