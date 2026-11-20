package dev.latvian.mods.vidlib.feature.cutscene.step.state;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ColorBlendMode;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.cutscene.step.ScreenEffectCutsceneStep;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ScreenTintEffect;

public class ScreenEffectCutsceneStepState extends CutsceneStepState {
	@ClientAutoRegister
	public static final Factory<ScreenEffectCutsceneStep> FACTORY = new Factory<>(ScreenEffectCutsceneStep.TYPE, ScreenEffectCutsceneStepState::new, ScreenEffectCutsceneStepState::new);

	public Ref<ScreenEffect> effect;

	public ScreenEffectCutsceneStepState(ScreenEffectCutsceneStep step) {
		this.effect = step.effect();
	}

	public ScreenEffectCutsceneStepState() {
		this.effect = new ScreenTintEffect(Color.BLACK.withAlpha(0).gradient(Color.BLACK).ref(), ColorBlendMode.MULTIPLICATIVE).ref();
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
	}
}
