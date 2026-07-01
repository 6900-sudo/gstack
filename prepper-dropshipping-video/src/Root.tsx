import React from 'react';
import {Composition} from 'remotion';
import {FPS, HEIGHT, SCENE_DURATIONS, TRANSITION_FRAMES, WIDTH} from './constants';
import {HowTo} from './HowTo';

const TOTAL_DURATION =
	SCENE_DURATIONS.reduce((a, b) => a + b, 0) - TRANSITION_FRAMES * (SCENE_DURATIONS.length - 1);

export const Root: React.FC = () => {
	return (
		<>
			<Composition
				id="HowTo"
				component={HowTo}
				durationInFrames={TOTAL_DURATION}
				fps={FPS}
				width={WIDTH}
				height={HEIGHT}
			/>
		</>
	);
};
