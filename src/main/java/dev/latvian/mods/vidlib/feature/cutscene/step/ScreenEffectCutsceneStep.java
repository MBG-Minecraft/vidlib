package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneState;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorEffect;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class ScreenEffectCutsceneStep extends CutsceneStep {
	public static final Codec<ScreenEffectCutsceneStep> CODEC = ScreenEffect.CODEC.xmap(ScreenEffectCutsceneStep::new, s -> s.effect);

	public static final StreamCodec<RegistryFriendlyByteBuf, ScreenEffectCutsceneStep> STREAM_CODEC = CompositeStreamCodec.of(
		ScreenEffect.STREAM_CODEC, o -> o.effect,
		ScreenEffectCutsceneStep::new
	);

	public final ScreenEffect effect;

	public static class Builder extends CutsceneStepImBuilder {
		@Override
		public void set(@Nullable CutsceneStep value) {
			if (value instanceof ScreenEffectCutsceneStep s) {
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public CutsceneStep build() {
			return new ScreenEffectCutsceneStep();
		}
	}

	public ScreenEffectCutsceneStep(ScreenEffect effect) {
		this.effect = effect;
	}

	public ScreenEffectCutsceneStep() {
		this.effect = new ColorEffect(Color.BLACK.withAlpha(0).gradient(Color.BLACK), false);
	}

	@Override
	public CutsceneStepType type() {
		return CutsceneStepType.SCREEN_EFFECT;
	}

	@Override
	public void tick(CutsceneState state, KNumberContext ctx) {
	}

	@Override
	public CutsceneStepImBuilder createBuilder() {
		return new Builder();
	}
}
