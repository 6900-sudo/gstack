import React from 'react';
import {interpolate, useCurrentFrame} from 'remotion';

export const CountUp: React.FC<{
	from: number;
	to: number;
	startFrame: number;
	durationInFrames: number;
	prefix?: string;
	suffix?: string;
	decimals?: number;
	style?: React.CSSProperties;
}> = ({from, to, startFrame, durationInFrames, prefix = '', suffix = '', decimals = 0, style}) => {
	const frame = useCurrentFrame();
	const value = interpolate(frame, [startFrame, startFrame + durationInFrames], [from, to], {
		extrapolateLeft: 'clamp',
		extrapolateRight: 'clamp',
	});

	return (
		<span
			style={{
				fontVariantNumeric: 'tabular-nums',
				fontFeatureSettings: '"tnum"',
				...style,
			}}
		>
			{prefix}
			{value.toFixed(decimals)}
			{suffix}
		</span>
	);
};
