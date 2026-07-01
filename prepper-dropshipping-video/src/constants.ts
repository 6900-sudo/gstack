export const FPS = 30;
export const WIDTH = 1080;
export const HEIGHT = 1920;

export const COLORS = {
	background: '#0a0a0a',
	text: '#ffffff',
	accent: '#6366f1',
	success: '#22c55e',
	muted: '#9ca3af',
};

export const FONT_SIZES = {
	headline: 72,
	subheadline: 56,
	body: 40,
	label: 30,
};

// Safe zone: 150px top, 170px bottom, 60px sides (min).
export const SAFE = {
	top: 150,
	bottom: 170,
	side: 60,
};

export const TRANSITION_FRAMES = 12;

// Scene durations (in frames) chosen so that, after 4 fade transitions of
// TRANSITION_FRAMES each overlap the timeline, the final sequence is
// exactly 900 frames (30s at 30fps): 190+190+190+189+189 - 4*12 = 900.
export const SCENE_DURATIONS = [190, 190, 190, 189, 189];
