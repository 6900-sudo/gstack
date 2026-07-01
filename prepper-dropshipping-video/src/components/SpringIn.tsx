import React from 'react';
import {spring, useCurrentFrame, useVideoConfig} from 'remotion';

export const SpringIn: React.FC<{
	children: React.ReactNode;
	delay?: number;
	from?: {opacity?: number; y?: number; scale?: number};
	style?: React.CSSProperties;
}> = ({children, delay = 0, from = {opacity: 0, y: 40, scale: 0.9}, style}) => {
	const frame = useCurrentFrame();
	const {fps} = useVideoConfig();

	const progress = spring({
		frame: frame - delay,
		fps,
		config: {damping: 200},
	});

	const opacity = from.opacity === undefined ? 1 : from.opacity + (1 - from.opacity) * progress;
	const y = from.y === undefined ? 0 : from.y * (1 - progress);
	const scale = from.scale === undefined ? 1 : from.scale + (1 - from.scale) * progress;

	return (
		<div
			style={{
				opacity,
				transform: `translateY(${y}px) scale(${scale})`,
				...style,
			}}
		>
			{children}
		</div>
	);
};
