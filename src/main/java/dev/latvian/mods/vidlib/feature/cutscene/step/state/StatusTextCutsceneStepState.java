package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.StatusTextCutsceneStep;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class StatusTextCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<StatusTextCutsceneStep> FACTORY = new Factory<>(StatusTextCutsceneStep.TYPE, StatusTextCutsceneStepState::new, StatusTextCutsceneStepState::new);

	public Component text;

	public StatusTextCutsceneStepState(StatusTextCutsceneStep step) {
		this.text = step.text();
	}

	public StatusTextCutsceneStepState() {
		this.text = Component.empty();
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		var mc = Minecraft.getInstance();

		if (mc.player != null) {
			mc.player.sendOverlayMessage(text);
		}
	}

	@Override
	public void exit(CutsceneState state, KNumberContext ctx) {
		if (snap) {
			var mc = Minecraft.getInstance();

			if (mc.player != null) {
				mc.player.sendOverlayMessage(Component.empty());
			}
		}
	}
}
