package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectInstance;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectShaderType;
import imgui.ImGui;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

public record ColorEffect(Ref<Gradient> color, boolean additive) implements ScreenEffect {
	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"color",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			Gradient.CODEC.optionalFieldOf("color", Gradient.BLACK).forGetter(ColorEffect::color),
			Codec.BOOL.optionalFieldOf("additive", false).forGetter(ColorEffect::additive)
		).apply(instance, ColorEffect::new)),
		CompositeStreamCodec.of(
			Gradient.STREAM_CODEC, ColorEffect::color,
			ByteBufCodecs.BOOL, ColorEffect::additive,
			ColorEffect::new
		)
	);

	public static class Inst extends ScreenEffectInstance {
		public Ref<Gradient> vColor;
		public boolean additive;

		private Color color, prevColor;

		public Inst(Ref<Gradient> vColor, boolean additive) {
			this.vColor = vColor;
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
			if (ctx.progress != null) {
				color = vColor.value().get(ctx.progress.floatValue());
			}
		}

		@Override
		public void upload(IntArrayList arr, float delta) {
			arr.add(prevColor.lerp(delta, color).argb()); // 1
			arr.add(additive ? 1 : 0); // 2
		}

		@Override
		public void imgui(ImGraphics graphics) {
			super.imgui(graphics);

			var imColor = new GradientImBuilder();
			imColor.set(vColor);
			imColor.imguiKey(graphics, "color", "Color");

			if (imColor.isValid()) {
				vColor = imColor.build();
			}

			if (ImGui.checkbox("Additive", additive)) {
				additive = !additive;
			}
		}
	}

	@Override
	public String getName() {
		return "Color";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.PALETTE;
	}

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}

	@Override
	public ScreenEffectInstance createInstance() {
		return new Inst(color, additive);
	}
}
