package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.TopBarTextCutsceneStep;
import net.minecraft.network.chat.Component;

public class TopBarTextCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<TopBarTextCutsceneStep> FACTORY = new Factory<>(TopBarTextCutsceneStep.TYPE, TopBarTextCutsceneStepState::new, TopBarTextCutsceneStepState::new);

	public Component text;

	public TopBarTextCutsceneStepState(TopBarTextCutsceneStep step) {
		this.text = step.text();
	}

	public TopBarTextCutsceneStepState() {
		this.text = Component.empty();
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		state.topBar.add(text);
	}
}
