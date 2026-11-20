package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.BottomBarTextCutsceneStep;
import net.minecraft.network.chat.Component;

public class BottomBarTextCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<BottomBarTextCutsceneStep> FACTORY = new Factory<>(BottomBarTextCutsceneStep.TYPE, BottomBarTextCutsceneStepState::new, BottomBarTextCutsceneStepState::new);

	public Component text;

	public BottomBarTextCutsceneStepState(BottomBarTextCutsceneStep step) {
		this.text = step.text();
	}

	public BottomBarTextCutsceneStepState() {
		this.text = Component.empty();
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		state.bottomBar.add(text);
	}
}
