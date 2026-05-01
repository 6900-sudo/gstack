/**
 * Auth resolution for Replicate API access.
 *
 * Resolution order:
 * 1. ~/.gstack/replicate.json → { "api_token": "r8_..." }
 * 2. REPLICATE_API_TOKEN environment variable
 * 3. null (caller handles guided setup or fallback)
 */

import fs from "fs";
import path from "path";

const CONFIG_PATH = path.join(process.env.HOME || "~", ".gstack", "replicate.json");

export function resolveReplicateToken(): string | null {
  try {
    if (fs.existsSync(CONFIG_PATH)) {
      const content = fs.readFileSync(CONFIG_PATH, "utf-8");
      const config = JSON.parse(content);
      if (config.api_token && typeof config.api_token === "string") {
        return config.api_token;
      }
    }
  } catch {
    // Fall through to env var
  }

  if (process.env.REPLICATE_API_TOKEN) {
    return process.env.REPLICATE_API_TOKEN;
  }

  return null;
}

export function saveReplicateToken(token: string): void {
  const dir = path.dirname(CONFIG_PATH);
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(CONFIG_PATH, JSON.stringify({ api_token: token }, null, 2));
  fs.chmodSync(CONFIG_PATH, 0o600);
}

export function requireReplicateToken(): string {
  const token = resolveReplicateToken();
  if (!token) {
    console.error("No Replicate API token found.");
    console.error("");
    console.error("Save to ~/.gstack/replicate.json: { \"api_token\": \"r8_...\" }");
    console.error("  or set REPLICATE_API_TOKEN environment variable");
    console.error("");
    console.error("Get a token at: https://replicate.com/account/api-tokens");
    process.exit(1);
  }
  return token;
}
