#!/usr/bin/env python3
"""Generate a high-energy vertical video with PixVerse V5.6 via Replicate.

Usage:
    export REPLICATE_API_TOKEN=...   # get one at https://replicate.com/account/api-tokens
    pip install replicate
    python3 scripts/pixverse-generate.py

Prints the URL of the generated video when complete.
"""

import os
import sys

import replicate

# Token comes from the environment — never hardcode credentials in this file.
if not os.environ.get("REPLICATE_API_TOKEN"):
    sys.exit("REPLICATE_API_TOKEN is not set. Export it before running this script.")

# High-energy, visually striking prompt configuration
prompt_text = (
    "Cinematic, ultra-detailed 4k action shot. A cool, stylized ninja in a black techwear hoodie "
    "with glowing red eyes and twin katanas on his back. He performs rapid, gravity-defying "
    "freestyle soccer juggling in a packed, massive stadium. Dramatic volumetric stadium spotlights "
    "cut through a light rain, creating beautiful lens flares and wet ground reflections. "
    "He executes a powerful, high-speed acrobatic bicycle kick toward the camera with extreme energy "
    "and explosive physics. Dynamic, fast-paced camera tracking, high contrast, vivid colors."
)

negative_prompt_text = (
    "blurry, low quality, static camera, slow motion, boring background, deformed limbs, "
    "bad lighting, cartoonish, low resolution"
)

print("Sending request to PixVerse V5.6...")

output = replicate.run(
    "pixverse/pixverse-v5.6",
    input={
        "prompt": prompt_text,
        "negative_prompt": negative_prompt_text,
        "quality": "1080p",             # Force high-definition rendering
        "duration": 5,                  # High energy works best in a tight, fast 5s cut
        "aspect_ratio": "9:16",         # Perfect for mobile/TikTok/Shorts format
        "thinking_type": "auto",        # Lets PixVerse plan complex motion paths
        "generate_audio_switch": True,  # Automatically generates matching stadium sound effects!
    },
)

# Print the URL of your new high-energy video
print("Generation Complete! Your video is ready at:")
print(output)
