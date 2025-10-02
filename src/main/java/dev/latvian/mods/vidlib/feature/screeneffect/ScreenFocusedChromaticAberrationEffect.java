package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.util.Mth;

public record ScreenFocusedChromaticAberrationEffect(KNumber focusX, KNumber focusY, KNumber strength) implements ScreenEffect {
	public static final SimpleRegistryType<ScreenFocusedChromaticAberrationEffect> TYPE = SimpleRegistryType.dynamic("screen_focused_chromatic_aberration", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("strength").forGetter(ScreenFocusedChromaticAberrationEffect::strength),
		KNumber.CODEC.fieldOf("focus_x").forGetter(ScreenFocusedChromaticAberrationEffect::focusX),
		KNumber.CODEC.fieldOf("focus_y").forGetter(ScreenFocusedChromaticAberrationEffect::focusY)
	).apply(instance, ScreenFocusedChromaticAberrationEffect::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, ScreenFocusedChromaticAberrationEffect::strength,
		KNumber.STREAM_CODEC, ScreenFocusedChromaticAberrationEffect::focusX,
		KNumber.STREAM_CODEC, ScreenFocusedChromaticAberrationEffect::focusY,
		ScreenFocusedChromaticAberrationEffect::new
	));

	public static class Inst implements ScreenEffectInstance {
		private final ScreenFocusedChromaticAberrationEffect effect;
		private float strength, prevStrength;
		private float x, prevX;
		private float y, prevY;

		public Inst(ScreenFocusedChromaticAberrationEffect effect) {
			this.effect = effect;
		}

		@Override
		public ScreenEffectShaderType shaderType() {
			return ScreenEffectShaderType.FOCUSED_CHROMATIC_ABERRATION;
		}

		@Override
		public void snap() {
			prevStrength = strength;
			prevX = x;
			prevY = y;
		}

		@Override
		public void update(KNumberContext ctx) {
			strength = (float) effect.strength.getOr(ctx, 0D);
			x = (float) effect.focusX.getOr(ctx, 0D);
			y = (float) effect.focusY.getOr(ctx, 0D);
		}

		@Override
		public void upload(IntArrayList arr, float delta) {
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevStrength, strength))); // 1
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevX, x))); // 2
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevY, y))); // 3
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public ScreenEffectInstance createInstance() {
		return new Inst(this);
	}
}
