package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.util.Mth;

public record AngledChromaticAberrationEffect(KNumber strength, KNumber angle) implements ScreenEffect {
	public static final SimpleRegistryType<AngledChromaticAberrationEffect> TYPE = SimpleRegistryType.dynamic("angled_chromatic_aberration", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("strength").forGetter(AngledChromaticAberrationEffect::strength),
		KNumber.CODEC.fieldOf("angle").forGetter(AngledChromaticAberrationEffect::angle)
	).apply(instance, AngledChromaticAberrationEffect::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, AngledChromaticAberrationEffect::strength,
		KNumber.STREAM_CODEC, AngledChromaticAberrationEffect::angle,
		AngledChromaticAberrationEffect::new
	));

	public static class Inst implements ScreenEffectInstance {
		private final AngledChromaticAberrationEffect effect;
		private float strength, prevStrength;
		private float angle, prevAngle;

		public Inst(AngledChromaticAberrationEffect effect) {
			this.effect = effect;
		}

		@Override
		public ScreenEffectShaderType shaderType() {
			return ScreenEffectShaderType.ANGLED_CHROMATIC_ABERRATION;
		}

		@Override
		public void snap() {
			prevStrength = strength;
			prevAngle = angle;
		}

		@Override
		public void update(KNumberContext ctx) {
			strength = (float) effect.strength.getOr(ctx, 0D);
			angle = (float) effect.angle.getOr(ctx, 0D);
		}

		@Override
		public void upload(IntArrayList arr, float delta) {
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevStrength, strength))); // 1
			arr.add(Float.floatToIntBits((float) Math.toRadians(Mth.rotLerp(delta, prevAngle, angle)))); // 2
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
