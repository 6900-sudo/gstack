import React from 'react';
import {AbsoluteFill} from 'remotion';
import {COLORS, FONT_SIZES} from '../constants';
import {SafeArea} from '../components/SafeArea';
import {SpringIn} from '../components/SpringIn';
import {AnimatedPath} from '../components/AnimatedPath';
import {interFont} from '../components/fonts';
import {FirstAidIcon, FoodPouchIcon, WaterDropIcon} from '../components/Icons';

const DIAGRAM_W = 900;
const DIAGRAM_H = 560;

const NICHE_BOX = {x: 450, y: 55};
const NODES = [
	{x: 150, y: 260, label: 'Water Filters', Icon: WaterDropIcon, chosen: false},
	{x: 450, y: 260, label: 'Freeze-Dried Food', Icon: FoodPouchIcon, chosen: true},
	{x: 750, y: 260, label: 'Emergency Kits', Icon: FirstAidIcon, chosen: false},
];
const SUPPLIER_BOX = {x: 450, y: 480};

export const Scene2Niche: React.FC = () => {
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
						Choose a Niche,
						<br />
						<span style={{color: COLORS.accent}}>Find a Supplier</span>
					</div>
				</SpringIn>

				<SpringIn delay={10} style={{marginTop: 30, marginBottom: 30}}>
					<div
						style={{
							fontSize: FONT_SIZES.body,
							color: COLORS.muted,
							textAlign: 'center',
							maxWidth: 880,
							lineHeight: 1.4,
						}}
					>
						Go narrow, then connect to a dropship supplier that ships
						straight to your customer.
					</div>
				</SpringIn>

				<div style={{position: 'relative', width: DIAGRAM_W, height: DIAGRAM_H}}>
					<svg
						width={DIAGRAM_W}
						height={DIAGRAM_H}
						viewBox={`0 0 ${DIAGRAM_W} ${DIAGRAM_H}`}
						style={{position: 'absolute', top: 0, left: 0}}
					>
						{NODES.map((n, i) => (
							<AnimatedPath
								key={i}
								d={`M ${NICHE_BOX.x} ${NICHE_BOX.y + 40} L ${n.x} ${n.y - 55}`}
								stroke={COLORS.muted}
								strokeWidth={4}
								startFrame={20 + i * 12}
								durationInFrames={22}
							/>
						))}
						<AnimatedPath
							d={`M ${SUPPLIER_BOX.x} ${NODES[1].y + 55} L ${SUPPLIER_BOX.x} ${SUPPLIER_BOX.y - 40}`}
							stroke={COLORS.success}
							strokeWidth={5}
							startFrame={115}
							durationInFrames={20}
						/>
					</svg>

					{/* Niche box */}
					<SpringIn
						delay={0}
						style={{
							position: 'absolute',
							left: NICHE_BOX.x - 150,
							top: NICHE_BOX.y - 40,
							width: 300,
							height: 80,
						}}
					>
						<div
							style={{
								width: '100%',
								height: '100%',
								border: `3px solid ${COLORS.text}`,
								borderRadius: 16,
								display: 'flex',
								alignItems: 'center',
								justifyContent: 'center',
								fontSize: FONT_SIZES.label,
								fontWeight: 600,
								color: COLORS.text,
							}}
						>
							Your Niche
						</div>
					</SpringIn>

					{/* Icon nodes */}
					{NODES.map((n, i) => (
						<SpringIn
							key={n.label}
							delay={55 + i * 12}
							style={{
								position: 'absolute',
								left: n.x - 80,
								top: n.y - 55,
								width: 160,
								display: 'flex',
								flexDirection: 'column',
								alignItems: 'center',
								gap: 10,
							}}
						>
							<div
								style={{
									width: 110,
									height: 110,
									borderRadius: '50%',
									display: 'flex',
									alignItems: 'center',
									justifyContent: 'center',
									border: `4px solid ${n.chosen ? COLORS.accent : COLORS.muted}`,
									background: n.chosen ? 'rgba(99,102,241,0.15)' : 'transparent',
								}}
							>
								<n.Icon size={56} color={n.chosen ? COLORS.accent : COLORS.muted} />
							</div>
							<div
								style={{
									fontSize: FONT_SIZES.label,
									fontWeight: n.chosen ? 800 : 400,
									color: n.chosen ? COLORS.text : COLORS.muted,
									textAlign: 'center',
								}}
							>
								{n.label}
							</div>
						</SpringIn>
					))}

					{/* Supplier box */}
					<SpringIn
						delay={135}
						style={{
							position: 'absolute',
							left: SUPPLIER_BOX.x - 150,
							top: SUPPLIER_BOX.y - 40,
							width: 300,
							height: 80,
						}}
					>
						<div
							style={{
								width: '100%',
								height: '100%',
								border: `3px solid ${COLORS.success}`,
								background: 'rgba(34,197,94,0.12)',
								borderRadius: 16,
								display: 'flex',
								alignItems: 'center',
								justifyContent: 'center',
								fontSize: FONT_SIZES.label,
								fontWeight: 800,
								color: COLORS.success,
							}}
						>
							Dropship Supplier
						</div>
					</SpringIn>
				</div>
			</SafeArea>
		</AbsoluteFill>
	);
};
