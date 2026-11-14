package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ColorBlendMode;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record ScreenTintEffect(Ref<Gradient> color, ColorBlendMode blendMode) implements ScreenEffect {
	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"screen_tint",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			Gradient.CODEC.optionalFieldOf("color", Color.BLACK.toGradient().ref()).forGetter(ScreenTintEffect::color),
			ColorBlendMode.CODEC.optionalFieldOf("blend_mode", ColorBlendMode.MULTIPLICATIVE).forGetter(ScreenTintEffect::blendMode)
		).apply(instance, ScreenTintEffect::new)),
		CompositeStreamCodec.of(
			Gradient.STREAM_CODEC, ScreenTintEffect::color,
			ColorBlendMode.STREAM_CODEC, ScreenTintEffect::blendMode,
			ScreenTintEffect::new
		)
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}
}
