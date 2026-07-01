import React from 'react';
import {SAFE} from '../constants';

export const SafeArea: React.FC<{
	children: React.ReactNode;
	style?: React.CSSProperties;
}> = ({children, style}) => {
	return (
		<div
			style={{
				position: 'absolute',
				top: SAFE.top,
				bottom: SAFE.bottom,
				left: SAFE.side,
				right: SAFE.side,
				display: 'flex',
				flexDirection: 'column',
				alignItems: 'center',
				justifyContent: 'center',
				...style,
			}}
		>
			{children}
		</div>
	);
};
