package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.screeneffect.FocusPoint;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectInstance;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectShaderType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilder;
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
		private KNumber vStrength;
		public FocusPoint vFocus;

		private float strength, prevStrength;
		private Vec2 focus, prevFocus;

		public Inst(KNumber vStrength, FocusPoint vFocus) {
			this.vStrength = vStrength;
			this.vFocus = vFocus;
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
			strength = (float) vStrength.getOr(ctx, 0D);
			focus = vFocus.get(ctx);
		}

		@Override
		public void upload(IntArrayList arr, float delta) {
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevStrength, strength))); // 1
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevFocus.x, focus.x))); // 2
			arr.add(Float.floatToIntBits(Mth.lerp(delta, prevFocus.y, focus.y))); // 3
		}

		@Override
		public void imgui(ImGraphics graphics) {
			super.imgui(graphics);

			var imStrengthNum = KNumberImBuilder.create(0D);
			imStrengthNum.set(vStrength);

			if (imStrengthNum.imguiKey(graphics, "Strength", "strength").isAny() && imStrengthNum.isValid()) {
				vStrength = imStrengthNum.build();
			}


		}
	}

	@Override
	public String getName() {
		return "Focused Chromatic Aberration";
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
		return new Inst(strength, focus);
	}
}
