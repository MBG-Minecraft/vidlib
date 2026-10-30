package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ScreenEffectCutsceneStep extends CutsceneStep {
	public static final Codec<ScreenEffectCutsceneStep> CODEC = ScreenEffect.CODEC.xmap(ScreenEffectCutsceneStep::new, s -> s.effect);

	public static final StreamCodec<RegistryFriendlyByteBuf, ScreenEffectCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ScreenEffect.STREAM_CODEC, o -> o.effect,
		ScreenEffectCutsceneStep::new
	);

	public final Ref<ScreenEffect> effect;

	public ScreenEffectCutsceneStep(Ref<ScreenEffect> effect) {
		this.effect = effect;
	}

	public ScreenEffectCutsceneStep() {
		this.effect = new ColorEffect(Color.BLACK.withAlpha(0).gradient(Color.BLACK).ref(), false).ref();
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.SCREEN_EFFECT;
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
	}
}
