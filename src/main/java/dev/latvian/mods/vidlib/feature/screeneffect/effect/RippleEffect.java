package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.screeneffect.FocusPoint;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

// https://www.shadertoy.com/view/ldBXDD
public record RippleEffect(FocusPoint focus, float scale, float intensity, float speed) implements ScreenEffect {
	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"ripple",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			FocusPoint.CODEC.optionalFieldOf("focus", FocusPoint.Screen.CENTER).forGetter(RippleEffect::focus),
			Codec.FLOAT.optionalFieldOf("scale", 0F).forGetter(RippleEffect::scale),
			Codec.FLOAT.optionalFieldOf("intensity", 0F).forGetter(RippleEffect::intensity),
			Codec.FLOAT.optionalFieldOf("speed", 0F).forGetter(RippleEffect::speed)
		).apply(instance, RippleEffect::new)),
		CompositeStreamCodec.of(
			KLibStreamCodecs.optional(FocusPoint.STREAM_CODEC, FocusPoint.Screen.CENTER), RippleEffect::focus,
			ByteBufCodecs.FLOAT, RippleEffect::scale,
			ByteBufCodecs.FLOAT, RippleEffect::intensity,
			ByteBufCodecs.FLOAT, RippleEffect::speed,
			RippleEffect::new
		)
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}
}
