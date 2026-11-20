package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.BarVisibilityCutsceneStep;

public class BarVisibilityCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<BarVisibilityCutsceneStep> FACTORY = new Factory<>(BarVisibilityCutsceneStep.TYPE, BarVisibilityCutsceneStepState::new, BarVisibilityCutsceneStepState::new);

	public final Ref<KNumber> visibility;

	public BarVisibilityCutsceneStepState(BarVisibilityCutsceneStep step) {
		this.visibility = step.visibility();
	}

	public BarVisibilityCutsceneStepState() {
		this.visibility = KNumber.ONE;
	}

	@Override
	public void start(CutsceneState state, KNumberContext ctx) {
		tick(state, ctx);

		if (snap) {
			state.prevBarVisibility = state.barVisibility;
		}
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var v = visibility.value().getOrNaN(ctx);

		if (!Double.isNaN(v)) {
			state.barVisibility = (float) v;
		}
	}
}
