import React from 'react';
import {AbsoluteFill} from 'remotion';
import {COLORS, FONT_SIZES} from '../constants';
import {SafeArea} from '../components/SafeArea';
import {SpringIn} from '../components/SpringIn';
import {AnimatedPath} from '../components/AnimatedPath';
import {interFont} from '../components/fonts';
import {FirstAidIcon, FoodPouchIcon, WaterDropIcon} from '../components/Icons';

const FRAME_W = 900;
const FRAME_H = 720;

const PRODUCTS = [
	{Icon: WaterDropIcon, label: 'Filter'},
	{Icon: FoodPouchIcon, label: 'Food Kit'},
	{Icon: FirstAidIcon, label: 'First Aid'},
];

export const Scene3BuildStore: React.FC = () => {
	return (
		<AbsoluteFill style={{backgroundColor: COLORS.background, fontFamily: interFont}}>
			<SafeArea>
				<SpringIn delay={0}>
					<div
						style={{
							fontSize: FONT_SIZES.headline,
							fontWeight: 800,
							color: COLORS.text,
							textAlign: 'center',
							lineHeight: 1.15,
						}}
					>
						Build Your Store
						<br />
						<span style={{color: COLORS.accent}}>in an Afternoon</span>
					</div>
				</SpringIn>

				<SpringIn delay={10} style={{marginTop: 30, marginBottom: 40}}>
					<div
						style={{
							fontSize: FONT_SIZES.body,
							color: COLORS.muted,
							textAlign: 'center',
							maxWidth: 880,
							lineHeight: 1.4,
						}}
					>
						Pick a theme, add your products, and set up checkout. No
						code required.
					</div>
				</SpringIn>

				<div style={{position: 'relative', width: FRAME_W, height: FRAME_H}}>
					<svg
						width={FRAME_W}
						height={FRAME_H}
						viewBox={`0 0 ${FRAME_W} ${FRAME_H}`}
						style={{position: 'absolute', top: 0, left: 0}}
					>
						<AnimatedPath
							d={`M 24 100
							  L 24 24 Q 24 8 40 8
							  L ${FRAME_W - 40} 8 Q ${FRAME_W - 24} 8 ${FRAME_W - 24} 24
							  L ${FRAME_W - 24} ${FRAME_H - 24} Q ${FRAME_W - 24} ${FRAME_H - 8} ${FRAME_W - 40} ${FRAME_H - 8}
							  L 40 ${FRAME_H - 8} Q 24 ${FRAME_H - 8} 24 ${FRAME_H - 24}
							  L 24 100`}
							stroke={COLORS.accent}
							strokeWidth={4}
							startFrame={0}
							durationInFrames={45}
						/>
						<AnimatedPath
							d={`M 24 84 L ${FRAME_W - 24} 84`}
							stroke={COLORS.accent}
							strokeWidth={4}
							startFrame={30}
							durationInFrames={15}
						/>
						{[0, 1, 2].map((i) => (
							<circle key={i} cx={56 + i * 28} cy={46} r={8} fill={COLORS.muted} opacity={0.6} />
						))}
					</svg>

					{/* product cards */}
					<div
						style={{
							position: 'absolute',
							top: 140,
							left: 44,
							right: 44,
							display: 'flex',
							gap: 24,
							justifyContent: 'center',
						}}
					>
						{PRODUCTS.map((p, i) => (
							<SpringIn key={p.label} delay={50 + i * 12} style={{width: 250}}>
								<div
									style={{
										border: `3px solid ${COLORS.muted}`,
										borderRadius: 16,
										padding: '36px 20px',
										display: 'flex',
										flexDirection: 'column',
										alignItems: 'center',
										gap: 16,
										background: 'rgba(255,255,255,0.03)',
									}}
								>
									<p.Icon size={64} />
									<div style={{fontSize: FONT_SIZES.label, fontWeight: 600, color: COLORS.text}}>
										{p.label}
									</div>
								</div>
							</SpringIn>
						))}
					</div>

					{/* checkout button */}
					<SpringIn
						delay={115}
						style={{
							position: 'absolute',
							bottom: 60,
							left: 44,
							right: 44,
							display: 'flex',
							justifyContent: 'center',
						}}
					>
						<div
							style={{
								background: COLORS.success,
								color: '#04120a',
								fontSize: FONT_SIZES.body,
								fontWeight: 800,
								padding: '22px 60px',
								borderRadius: 999,
							}}
						>
							Checkout →
						</div>
					</SpringIn>
				</div>
			</SafeArea>
		</AbsoluteFill>
	);
};
