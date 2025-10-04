package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import imgui.ImGui;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.codec.ByteBufCodecs;

public record ColorEffect(Gradient color, boolean additive) implements ScreenEffect {
	public static final SimpleRegistryType<ColorEffect> TYPE = SimpleRegistryType.dynamic("color", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("color", Color.BLACK).forGetter(ColorEffect::color),
		Codec.BOOL.optionalFieldOf("additive", false).forGetter(ColorEffect::additive)
	).apply(instance, ColorEffect::new)), CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, ColorEffect::color,
		ByteBufCodecs.BOOL, ColorEffect::additive,
		ColorEffect::new
	));

	public static class Inst extends ScreenEffectInstance {
		public Gradient gradient;
		public boolean additive;
		private Color color, prevColor;

		public Inst(Gradient gradient, boolean additive) {
			this.gradient = gradient;
			this.additive = additive;
		}

		@Override
		public ScreenEffectShaderType shaderType() {
			return ScreenEffectShaderType.COLOR;
		}

		@Override
		public void snap() {
			super.snap();
			prevColor = color;
		}

		@Override
		public void update(KNumberContext ctx) {
			color = gradient.get(ctx.progress);
		}

		@Override
		public void upload(IntArrayList arr, float delta) {
			arr.add(prevColor.lerp(delta, color).argb()); // 1
			arr.add(additive ? 1 : 0); // 2
		}

		@Override
		public String getName() {
			return "Color";
		}

		@Override
		public void imgui(ImGraphics graphics) {
			super.imgui(graphics);

			var imGradient = new GradientImBuilder();
			imGradient.set(gradient);
			imGradient.imguiKey(graphics, "gradient", "Gradient");

			if (ImGui.checkbox("Additive", additive)) {
				additive = !additive;
			}
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public ScreenEffectInstance createInstance() {
		return new Inst(color, additive);
	}
}
