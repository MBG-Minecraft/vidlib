package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CustomCutsceneEvent;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.CustomEventCutsceneStep;
import net.neoforged.neoforge.common.NeoForge;

public class CustomEventCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<CustomEventCutsceneStep> FACTORY = new Factory<>(CustomEventCutsceneStep.TYPE, CustomEventCutsceneStepState::new, CustomEventCutsceneStepState::new);

	public String event;

	public CustomEventCutsceneStepState(CustomEventCutsceneStep step) {
		this.event = step.event();
	}

	public CustomEventCutsceneStepState() {
		this.event = "";
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
		if (!event.isEmpty()) {
			NeoForge.EVENT_BUS.post(new CustomCutsceneEvent.Tick(ctx, event));
		}
	}

	@Override
	public void exit(CutsceneState state, KNumberContext ctx) {
		if (!event.isEmpty()) {
			NeoForge.EVENT_BUS.post(new CustomCutsceneEvent.Exit(ctx, event));
		}
	}
}
