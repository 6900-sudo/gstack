import React from 'react';
import {AbsoluteFill} from 'remotion';
import {COLORS, FONT_SIZES} from '../constants';
import {SafeArea} from '../components/SafeArea';
import {SpringIn} from '../components/SpringIn';
import {CountUp} from '../components/CountUp';
import {interFont} from '../components/fonts';
import {ArrowRightIcon, WaterDropIcon} from '../components/Icons';

export const Scene4Pricing: React.FC = () => {
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
						Import Products,
						<br />
						<span style={{color: COLORS.accent}}>Set Your Markup</span>
					</div>
				</SpringIn>

				<SpringIn delay={10} style={{marginTop: 30, marginBottom: 56}}>
					<div
						style={{
							fontSize: FONT_SIZES.body,
							color: COLORS.muted,
							textAlign: 'center',
							maxWidth: 880,
							lineHeight: 1.4,
						}}
					>
						An import app pulls products straight from your supplier.
						You set the price.
					</div>
				</SpringIn>

				{/* product card */}
				<SpringIn delay={30} from={{opacity: 0, y: 0, scale: 0.85}} style={{marginBottom: 56}}>
					<div
						style={{
							border: `3px solid ${COLORS.muted}`,
							borderRadius: 20,
							padding: '28px 44px',
							display: 'flex',
							alignItems: 'center',
							gap: 24,
							background: 'rgba(255,255,255,0.03)',
						}}
					>
						<WaterDropIcon size={56} />
						<div style={{fontSize: FONT_SIZES.body, fontWeight: 600, color: COLORS.text}}>
							Emergency Water Filter
						</div>
					</div>
				</SpringIn>

				{/* price markup row */}
				<div style={{display: 'flex', alignItems: 'center', gap: 40}}>
					<SpringIn delay={55}>
						<div style={{textAlign: 'center'}}>
							<div style={{fontSize: FONT_SIZES.label, color: COLORS.muted, marginBottom: 8}}>
								Supplier Cost
							</div>
							<div style={{fontSize: FONT_SIZES.headline, fontWeight: 800, color: COLORS.muted}}>
								<CountUp from={0} to={10} startFrame={55} durationInFrames={24} prefix="$" decimals={0} />
							</div>
						</div>
					</SpringIn>

					<SpringIn delay={75}>
						<ArrowRightIcon size={70} />
					</SpringIn>

					<SpringIn delay={85}>
						<div style={{textAlign: 'center'}}>
							<div style={{fontSize: FONT_SIZES.label, color: COLORS.success, marginBottom: 8}}>
								Your Price
							</div>
							<div style={{fontSize: FONT_SIZES.headline, fontWeight: 800, color: COLORS.success}}>
								<CountUp from={0} to={29.99} startFrame={85} durationInFrames={30} prefix="$" decimals={2} />
							</div>
						</div>
					</SpringIn>
				</div>

				{/* margin badge */}
				<SpringIn delay={125} style={{marginTop: 56}}>
					<div
						style={{
							fontSize: FONT_SIZES.body,
							fontWeight: 800,
							color: COLORS.success,
							background: 'rgba(34,197,94,0.12)',
							border: `2px solid ${COLORS.success}`,
							borderRadius: 999,
							padding: '18px 40px',
						}}
					>
						+<CountUp from={0} to={199} startFrame={125} durationInFrames={30} decimals={0} />% Margin
					</div>
				</SpringIn>
			</SafeArea>
		</AbsoluteFill>
	);
};
