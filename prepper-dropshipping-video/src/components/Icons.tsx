import React from 'react';
import {COLORS} from '../constants';

type IconProps = {
	size?: number;
	color?: string;
	style?: React.CSSProperties;
};

export const TentIcon: React.FC<IconProps> = ({size = 80, color = COLORS.text, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={style}>
		<path d="M50 15 L90 85 H10 Z" stroke={color} strokeWidth={5} strokeLinejoin="round" />
		<path d="M50 15 L50 85" stroke={color} strokeWidth={5} />
		<path d="M35 85 L50 55 L65 85" stroke={color} strokeWidth={5} strokeLinejoin="round" />
	</svg>
);

export const WaterDropIcon: React.FC<IconProps> = ({size = 80, color = COLORS.accent, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={style}>
		<path
			d="M50 10 C50 10 20 50 20 68 C20 85.5 33.4 96 50 96 C66.6 96 80 85.5 80 68 C80 50 50 10 50 10 Z"
			stroke={color}
			strokeWidth={5}
			strokeLinejoin="round"
		/>
	</svg>
);

export const FirstAidIcon: React.FC<IconProps> = ({size = 80, color = COLORS.success, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={style}>
		<rect x="12" y="25" width="76" height="55" rx="10" stroke={color} strokeWidth={5} />
		<path d="M50 38 V67 M35.5 52.5 H64.5" stroke={color} strokeWidth={7} strokeLinecap="round" />
	</svg>
);

export const WarehouseIcon: React.FC<IconProps> = ({size = 80, color = COLORS.text, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={style}>
		<path d="M10 45 L50 15 L90 45" stroke={color} strokeWidth={5} strokeLinejoin="round" />
		<rect x="18" y="45" width="64" height="42" stroke={color} strokeWidth={5} />
		<rect x="42" y="60" width="16" height="27" stroke={color} strokeWidth={5} />
	</svg>
);

export const FoodPouchIcon: React.FC<IconProps> = ({size = 80, color = COLORS.text, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={style}>
		<path
			d="M28 15 H72 L78 30 V80 C78 87 72 92 65 92 H35 C28 92 22 87 22 80 V30 Z"
			stroke={color}
			strokeWidth={5}
			strokeLinejoin="round"
		/>
		<path d="M22 45 H78" stroke={color} strokeWidth={5} />
	</svg>
);

export const BrowserWindowIcon: React.FC<{
	size?: number;
	color?: string;
}> = ({size = 300, color = COLORS.accent}) => (
	<svg width={size} height={size * 0.7} viewBox="0 0 300 210" fill="none">
		<rect x="4" y="4" width="292" height="202" rx="12" stroke={color} strokeWidth={4} />
		<line x1="4" y1="36" x2="296" y2="36" stroke={color} strokeWidth={4} />
		<circle cx="22" cy="20" r="5" fill={color} />
		<circle cx="40" cy="20" r="5" fill={color} />
		<circle cx="58" cy="20" r="5" fill={color} />
	</svg>
);

export const CheckIcon: React.FC<IconProps> = ({size = 48, color = COLORS.success, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={style}>
		<circle cx="50" cy="50" r="45" stroke={color} strokeWidth={6} />
		<path d="M28 52 L44 68 L74 34" stroke={color} strokeWidth={7} strokeLinecap="round" strokeLinejoin="round" />
	</svg>
);

export const ArrowRightIcon: React.FC<IconProps> = ({size = 60, color = COLORS.accent, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 40" fill="none" style={style}>
		<path d="M0 20 H85" stroke={color} strokeWidth={6} strokeLinecap="round" />
		<path d="M65 4 L86 20 L65 36" stroke={color} strokeWidth={6} strokeLinecap="round" strokeLinejoin="round" />
	</svg>
);

export const RocketBadgeIcon: React.FC<IconProps> = ({size = 90, color = COLORS.success, style}) => (
	<svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={style}>
		<path
			d="M50 8 C64 20 68 40 64 58 L50 70 L36 58 C32 40 36 20 50 8 Z"
			stroke={color}
			strokeWidth={5}
			strokeLinejoin="round"
		/>
		<circle cx="50" cy="36" r="7" stroke={color} strokeWidth={4} />
		<path d="M36 58 L24 74 L38 70 Z" stroke={color} strokeWidth={4} strokeLinejoin="round" />
		<path d="M64 58 L76 74 L62 70 Z" stroke={color} strokeWidth={4} strokeLinejoin="round" />
		<path d="M44 70 L40 88 L50 78 L60 88 L56 70" stroke={color} strokeWidth={4} strokeLinejoin="round" />
	</svg>
);
