package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectInstance;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectShaderType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberFloatInstance;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public record AngledChromaticAberrationEffect(KNumber strength, KNumber angle) implements ScreenEffect {
	public static final SimpleRegistryType<AngledChromaticAberrationEffect> TYPE = SimpleRegistryType.dynamic("angled_chromatic_aberration", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("strength").forGetter(AngledChromaticAberrationEffect::strength),
		KNumber.CODEC.fieldOf("angle").forGetter(AngledChromaticAberrationEffect::angle)
	).apply(instance, AngledChromaticAberrationEffect::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, AngledChromaticAberrationEffect::strength,
		KNumber.STREAM_CODEC, AngledChromaticAberrationEffect::angle,
		AngledChromaticAberrationEffect::new
	));

	public static class Inst extends ScreenEffectInstance {
		public final KNumberFloatInstance strength;
		public final KNumberFloatInstance angle;

		public Inst(KNumber strength, KNumber angle) {
			this.strength = new KNumberFloatInstance(strength);
			this.angle = new KNumberFloatInstance(angle);
		}

		@Override
		public ScreenEffectShaderType shaderType() {
			return ScreenEffectShaderType.ANGLED_CHROMATIC_ABERRATION;
		}

		@Override
		public void snap() {
			super.snap();
			strength.snap();
			angle.snap();
		}

		@Override
		public void update(KNumberContext ctx) {
			strength.update(ctx);
			angle.update(ctx);
		}

		@Override
		public void upload(IntArrayList arr, float delta) {
			arr.add(Float.floatToIntBits(strength.get(delta))); // 1
			arr.add(Float.floatToIntBits(angle.getRadians(delta))); // 2
		}

		@Override
		public void imgui(ImGraphics graphics) {
			super.imgui(graphics);
			strength.imgui(graphics, "Strength", "strength");
			angle.imgui(graphics, "Angle", "angle");
		}
	}

	@Override
	public String getName() {
		return "Angled Chromatic Aberration";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.BLUR;
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public ScreenEffectInstance createInstance() {
		return new Inst(strength, angle);
	}
}
