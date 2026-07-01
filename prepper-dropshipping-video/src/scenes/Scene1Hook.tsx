import React from 'react';
import {AbsoluteFill} from 'remotion';
import {COLORS, FONT_SIZES} from '../constants';
import {SafeArea} from '../components/SafeArea';
import {SpringIn} from '../components/SpringIn';
import {interFont} from '../components/fonts';
import {FirstAidIcon, TentIcon, WarehouseIcon, WaterDropIcon} from '../components/Icons';

const BAR_HEIGHTS = [90, 140, 115, 190, 230];

export const Scene1Hook: React.FC = () => {
	return (
		<AbsoluteFill style={{backgroundColor: COLORS.background, fontFamily: interFont}}>
			{/* accent icons drifting in corners */}
			<SpringIn delay={4} from={{opacity: 0, scale: 0.6, y: -20}} style={{position: 'absolute', top: 190, left: 70}}>
				<TentIcon size={56} color={COLORS.muted} />
			</SpringIn>
			<SpringIn delay={14} from={{opacity: 0, scale: 0.6, y: -20}} style={{position: 'absolute', top: 190, right: 70}}>
				<WaterDropIcon size={56} />
			</SpringIn>
			<SpringIn delay={24} from={{opacity: 0, scale: 0.6, y: 20}} style={{position: 'absolute', bottom: 220, left: 90}}>
				<FirstAidIcon size={56} />
			</SpringIn>

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
						Prepping Is a
						<br />
						<span style={{color: COLORS.accent}}>$2B+ Market</span>
					</div>
				</SpringIn>

				<SpringIn delay={10} style={{marginTop: 36}}>
					<div
						style={{
							fontSize: FONT_SIZES.body,
							fontWeight: 400,
							color: COLORS.muted,
							textAlign: 'center',
							maxWidth: 880,
							lineHeight: 1.4,
						}}
					>
						Emergency gear demand is booming. You don't need a warehouse to
						sell it.
					</div>
				</SpringIn>

				{/* bar chart */}
				<div
					style={{
						display: 'flex',
						alignItems: 'flex-end',
						gap: 22,
						height: 260,
						marginTop: 70,
					}}
				>
					{BAR_HEIGHTS.map((h, i) => (
						<SpringIn
							key={i}
							delay={40 + i * 9}
							from={{opacity: 0, scale: 1, y: h}}
							style={{
								width: 64,
								height: h,
								borderRadius: 10,
								background:
									i === BAR_HEIGHTS.length - 1
										? `linear-gradient(180deg, ${COLORS.success}, ${COLORS.accent})`
										: COLORS.accent,
							}}
						>
							<div style={{width: '100%', height: '100%'}} />
						</SpringIn>
					))}
				</div>

				{/* warehouse struck through + $0 inventory badge */}
				<div
					style={{
						display: 'flex',
						alignItems: 'center',
						gap: 28,
						marginTop: 70,
					}}
				>
					<SpringIn delay={95} style={{position: 'relative'}}>
						<WarehouseIcon size={84} color={COLORS.muted} />
						<div
							style={{
								position: 'absolute',
								top: '50%',
								left: '50%',
								width: 100,
								height: 6,
								background: '#ef4444',
								transform: 'translate(-50%, -50%) rotate(-32deg)',
								borderRadius: 3,
							}}
						/>
					</SpringIn>

					<SpringIn delay={110}>
						<div
							style={{
								fontSize: FONT_SIZES.label,
								fontWeight: 800,
								color: COLORS.success,
								background: 'rgba(34,197,94,0.12)',
								border: `2px solid ${COLORS.success}`,
								borderRadius: 999,
								padding: '14px 26px',
								whiteSpace: 'nowrap',
							}}
						>
							$0 Inventory Needed
						</div>
					</SpringIn>
				</div>
			</SafeArea>
		</AbsoluteFill>
	);
};
