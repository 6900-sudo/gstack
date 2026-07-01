import React from 'react';
import {interpolate, random, useCurrentFrame, useVideoConfig} from 'remotion';
import {COLORS, HEIGHT, WIDTH} from '../constants';

const PARTICLE_COUNT = 13;

export const ParticleBackground: React.FC<{startFrame?: number}> = ({startFrame = 0}) => {
	const frame = useCurrentFrame();
	const {durationInFrames} = useVideoConfig();
	const localFrame = Math.max(0, frame - startFrame);
	const activeDuration = Math.max(1, durationInFrames - startFrame);

	const particles = new Array(PARTICLE_COUNT).fill(0).map((_, i) => {
		const seed = `particle-${i}`;
		const x = random(seed + '-x') * WIDTH;
		const size = 6 + random(seed + '-size') * 18;
		const speed = 0.6 + random(seed + '-speed') * 0.9;
		const startY = HEIGHT + random(seed + '-start') * HEIGHT * 0.5;
		const travel = HEIGHT * 1.6 * speed;
		const y = startY - (localFrame / activeDuration) * travel * ((activeDuration / 30) * 0.4 + 1);
		const wrappedY = ((y % (HEIGHT + 200)) + (HEIGHT + 200)) % (HEIGHT + 200) - 100;
		const opacity = interpolate(
			wrappedY,
			[-50, 200, HEIGHT - 200, HEIGHT],
			[0, 0.7, 0.7, 0],
			{extrapolateLeft: 'clamp', extrapolateRight: 'clamp'},
		);
		const isAccent = i % 3 === 0;

		return (
			<div
				key={seed}
				style={{
					position: 'absolute',
					left: x,
					top: wrappedY,
					width: size,
					height: size,
					borderRadius: '50%',
					background: isAccent ? COLORS.accent : COLORS.success,
					opacity: opacity * (isAccent ? 0.5 : 0.35),
					filter: 'blur(0.5px)',
				}}
			/>
		);
	});

	return <div style={{position: 'absolute', inset: 0, overflow: 'hidden'}}>{particles}</div>;
};
