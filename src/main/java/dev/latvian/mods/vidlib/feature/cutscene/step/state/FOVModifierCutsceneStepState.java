package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.FOVModifierCutsceneStep;

public class FOVModifierCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<FOVModifierCutsceneStep> FACTORY = new Factory<>(FOVModifierCutsceneStep.TYPE, FOVModifierCutsceneStepState::new, FOVModifierCutsceneStepState::new);

	public Ref<KNumber> fov;

	public FOVModifierCutsceneStepState(FOVModifierCutsceneStep step) {
		this.fov = step.fov();
	}

	public FOVModifierCutsceneStepState() {
		this.fov = KNumber.ONE;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevFovMod = state.fovMod;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = fov.value().getOrNaN(ctx);

		if (!Double.isNaN(v)) {
			state.fovMod = (float) v;
		}
	}
}
