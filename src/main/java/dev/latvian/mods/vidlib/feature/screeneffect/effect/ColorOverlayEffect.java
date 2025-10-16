package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectInstance;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectShaderType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;

public record ColorOverlayEffect(Gradient color) implements ScreenEffect {
	public static final SimpleRegistryType<ColorOverlayEffect> TYPE = SimpleRegistryType.dynamic("color_overlay", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("color", Color.BLACK).forGetter(ColorOverlayEffect::color)
	).apply(instance, ColorOverlayEffect::new)), CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, ColorOverlayEffect::color,
		ColorOverlayEffect::new
	));

	public static class Inst extends ScreenEffectInstance {
		public Gradient vColor;

		private Color color, prevColor;

		public Inst(Gradient vColor) {
			this.vColor = vColor;
		}

		@Override
		public ScreenEffectShaderType shaderType() {
			return ScreenEffectShaderType.NONE;
		}

		@Override
		public void snap() {
			super.snap();
			prevColor = color;
		}

		@Override
		public void update(KNumberContext ctx) {
			if (ctx.progress != null) {
				color = vColor.get(ctx.progress.floatValue());
			}
		}

		@Override
		public void imgui(ImGraphics graphics) {
			super.imgui(graphics);

			var imGradient = new GradientImBuilder();
			imGradient.set(vColor);
			imGradient.imguiKey(graphics, "color", "Color");

			if (imGradient.isValid()) {
				vColor = imGradient.build();
			}
		}

		public Color getColor(float delta) {
			return prevColor.lerp(delta, color);
		}
	}

	@Override
	public String getName() {
		return "Color Overlay";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.PALETTE;
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public ScreenEffectInstance createInstance() {
		return new Inst(color);
	}
}
