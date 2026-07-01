import React from 'react';
import {AbsoluteFill} from 'remotion';
import {TransitionSeries, linearTiming} from '@remotion/transitions';
import {fade} from '@remotion/transitions/fade';
import {COLORS, SCENE_DURATIONS, TRANSITION_FRAMES} from './constants';
import {Scene1Hook} from './scenes/Scene1Hook';
import {Scene2Niche} from './scenes/Scene2Niche';
import {Scene3BuildStore} from './scenes/Scene3BuildStore';
import {Scene4Pricing} from './scenes/Scene4Pricing';
import {Scene5Launch} from './scenes/Scene5Launch';

export const HowTo: React.FC = () => {
	return (
		<AbsoluteFill style={{backgroundColor: COLORS.background}}>
			<TransitionSeries>
				<TransitionSeries.Sequence durationInFrames={SCENE_DURATIONS[0]}>
					<Scene1Hook />
				</TransitionSeries.Sequence>

				<TransitionSeries.Transition
					presentation={fade()}
					timing={linearTiming({durationInFrames: TRANSITION_FRAMES})}
				/>

				<TransitionSeries.Sequence durationInFrames={SCENE_DURATIONS[1]}>
					<Scene2Niche />
				</TransitionSeries.Sequence>

				<TransitionSeries.Transition
					presentation={fade()}
					timing={linearTiming({durationInFrames: TRANSITION_FRAMES})}
				/>

				<TransitionSeries.Sequence durationInFrames={SCENE_DURATIONS[2]}>
					<Scene3BuildStore />
				</TransitionSeries.Sequence>

				<TransitionSeries.Transition
					presentation={fade()}
					timing={linearTiming({durationInFrames: TRANSITION_FRAMES})}
				/>

				<TransitionSeries.Sequence durationInFrames={SCENE_DURATIONS[3]}>
					<Scene4Pricing />
				</TransitionSeries.Sequence>

				<TransitionSeries.Transition
					presentation={fade()}
					timing={linearTiming({durationInFrames: TRANSITION_FRAMES})}
				/>

				<TransitionSeries.Sequence durationInFrames={SCENE_DURATIONS[4]}>
					<Scene5Launch />
				</TransitionSeries.Sequence>
			</TransitionSeries>
		</AbsoluteFill>
	);
};
