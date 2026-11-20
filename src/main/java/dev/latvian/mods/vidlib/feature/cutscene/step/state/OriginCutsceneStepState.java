package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.OriginCutsceneStep;

public class OriginCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<OriginCutsceneStep> FACTORY = new Factory<>(OriginCutsceneStep.TYPE, OriginCutsceneStepState::new, OriginCutsceneStepState::new);

	public Ref<KVector> position;

	public OriginCutsceneStepState(OriginCutsceneStep step) {
		this.position = step.position();
	}

	public OriginCutsceneStepState() {
		this.position = KVector.ZERO;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevOrigin = state.origin;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = position.value().get(ctx);

		if (v != null) {
			state.origin = v;
		}
	}
}
