package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.util.Mth;

public record WorldFocusedChromaticAberrationEffect(KNumber strength, KVector focus) implements ScreenEffect {
	public static final SimpleRegistryType<WorldFocusedChromaticAberrationEffect> TYPE = SimpleRegistryType.dynamic("world_focused_chromatic_aberration", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("strength").forGetter(WorldFocusedChromaticAberrationEffect::strength),
		KVector.CODEC.fieldOf("focus").forGetter(WorldFocusedChromaticAberrationEffect::focus)
	).apply(instance, WorldFocusedChromaticAberrationEffect::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, WorldFocusedChromaticAberrationEffect::strength,
		KVector.STREAM_CODEC, WorldFocusedChromaticAberrationEffect::focus,
		WorldFocusedChromaticAberrationEffect::new
	));

	public static class Inst implements ScreenEffectInstance {
		private final WorldFocusedChromaticAberrationEffect effect;
		private float strength, prevStrength;
		private float x, prevX;
		private float y, prevY;

		public Inst(WorldFocusedChromaticAberrationEffect effect) {
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
			x = 0F;
			y = 0F;
			// fixme
			// x = (float) effect.focusX.getOr(ctx, 0D);
			// y = (float) effect.focusY.getOr(ctx, 0D);
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
