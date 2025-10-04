package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public record FocusedChromaticAberrationEffect(KNumber strength, FocusPoint focus) implements ScreenEffect {
	public static final SimpleRegistryType<FocusedChromaticAberrationEffect> TYPE = SimpleRegistryType.dynamic("focused_chromatic_aberration", RecordCodecBuilder.mapCodec(instance -> instance.group(
		KNumber.CODEC.fieldOf("strength").forGetter(FocusedChromaticAberrationEffect::strength),
		FocusPoint.CODEC.fieldOf("focus").forGetter(FocusedChromaticAberrationEffect::focus)
	).apply(instance, FocusedChromaticAberrationEffect::new)), CompositeStreamCodec.of(
		KNumber.STREAM_CODEC, FocusedChromaticAberrationEffect::strength,
		FocusPoint.STREAM_CODEC, FocusedChromaticAberrationEffect::focus,
		FocusedChromaticAberrationEffect::new
	));

	public static class Inst extends ScreenEffectInstance {
		private final KNumber strengthNum;
		public FocusPoint focusNum;
		private float strength, prevStrength;
		private Vec2 focus, prevFocus;

		public Inst(KNumber strengthNum, FocusPoint focusNum) {
			this.strengthNum = strengthNum;
			this.focusNum = focusNum;
		}

		@Override
		public ScreenEffectShaderType shaderType() {
			return ScreenEffectShaderType.FOCUSED_CHROMATIC_ABERRATION;
		}

		@Override
		public void snap() {
			super.snap();
			prevStrength = strength;
			prevFocus = focus;
		}

		@Override
		public void update(KNumberContext ctx) {
			strength = (float) strengthNum.getOr(ctx, 0D);
			focus = focusNum.get(ctx);
		}

		@Override
		public void upload(IntArrayList arr, float delta) {
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevStrength, strength))); // 1
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevFocus.x, focus.x))); // 2
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevFocus.y, focus.y))); // 3
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public ScreenEffectInstance createInstance() {
		return new Inst(strength, focus);
	}
}
