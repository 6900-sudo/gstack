import React from 'react';
import {AbsoluteFill} from 'remotion';
import {COLORS, FONT_SIZES} from '../constants';
import {SafeArea} from '../components/SafeArea';
import {SpringIn} from '../components/SpringIn';
import {ParticleBackground} from '../components/ParticleBackground';
import {interFont} from '../components/fonts';
import {CheckIcon, RocketBadgeIcon} from '../components/Icons';

const CHECKLIST = ['Pick Your Niche', 'Build Your Store', 'Run Your First Ads'];

export const Scene5Launch: React.FC = () => {
	return (
		<AbsoluteFill style={{backgroundColor: COLORS.background, fontFamily: interFont}}>
			<ParticleBackground startFrame={0} />

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
						Launch. Market.
						<br />
						<span style={{color: COLORS.accent}}>Repeat.</span>
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
						Promote with targeted ads, then reinvest profits into more
						products.
					</div>
				</SpringIn>

				<div style={{display: 'flex', flexDirection: 'column', gap: 26, marginBottom: 70}}>
					{CHECKLIST.map((item, i) => (
						<SpringIn
							key={item}
							delay={30 + i * 10}
							from={{opacity: 0, y: 24, scale: 1}}
							style={{
								display: 'flex',
								alignItems: 'center',
								gap: 24,
								border: `2px solid ${COLORS.success}`,
								borderRadius: 16,
								padding: '20px 32px',
								background: 'rgba(34,197,94,0.08)',
								width: 640,
							}}
						>
							<CheckIcon size={44} />
							<div style={{fontSize: FONT_SIZES.body, fontWeight: 600, color: COLORS.text}}>
								{item}
							</div>
						</SpringIn>
					))}
				</div>

				<SpringIn delay={80} from={{opacity: 0, y: 20, scale: 0.7}}>
					<div
						style={{
							display: 'flex',
							flexDirection: 'column',
							alignItems: 'center',
							gap: 18,
							border: `3px solid ${COLORS.accent}`,
							borderRadius: 24,
							padding: '32px 56px',
							background: 'rgba(99,102,241,0.12)',
						}}
					>
						<RocketBadgeIcon size={80} />
						<div style={{fontSize: FONT_SIZES.subheadline, fontWeight: 800, color: COLORS.text}}>
							Start Today
						</div>
					</div>
				</SpringIn>
			</SafeArea>
		</AbsoluteFill>
	);
};
