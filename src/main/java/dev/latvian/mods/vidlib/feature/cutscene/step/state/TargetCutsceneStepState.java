package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.TargetCutsceneStep;

public class TargetCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<TargetCutsceneStep> FACTORY = new Factory<>(TargetCutsceneStep.TYPE, TargetCutsceneStepState::new, TargetCutsceneStepState::new);

	public Ref<KVector> position;

	public TargetCutsceneStepState(TargetCutsceneStep step) {
		this.position = step.position();
	}

	public TargetCutsceneStepState() {
		this.position = KVector.ZERO;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevTarget = state.target;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = position.value().get(ctx);

		if (v != null) {
			state.target = v;
			ctx.targetPos = v;
		}
	}
}
