import React, {useEffect, useRef, useState} from 'react';
import {continueRender, delayRender, interpolate, useCurrentFrame} from 'remotion';

export const AnimatedPath: React.FC<{
	d: string;
	stroke: string;
	strokeWidth: number;
	startFrame: number;
	durationInFrames: number;
	fill?: string;
}> = ({d, stroke, strokeWidth, startFrame, durationInFrames, fill = 'none'}) => {
	const pathRef = useRef<SVGPathElement>(null);
	const [length, setLength] = useState<number | null>(null);
	const [handle] = useState(() => delayRender('Measuring SVG path length for draw-on animation'));
	const frame = useCurrentFrame();

	useEffect(() => {
		if (pathRef.current) {
			setLength(pathRef.current.getTotalLength());
			continueRender(handle);
		}
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [handle]);

	const progress =
		length === null
			? 0
			: interpolate(frame, [startFrame, startFrame + durationInFrames], [0, 1], {
					extrapolateLeft: 'clamp',
					extrapolateRight: 'clamp',
			  });

	return (
		<path
			ref={pathRef}
			d={d}
			stroke={stroke}
			strokeWidth={strokeWidth}
			fill={fill}
			strokeDasharray={length ?? 1}
			strokeDashoffset={length === null ? 0 : length * (1 - progress)}
			strokeLinecap="round"
			strokeLinejoin="round"
		/>
	);
};
