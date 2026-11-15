package dev.latvian.mods.vidlib.feature.screeneffect.ripple;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.screeneffect.FocusPoint;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

public record RippleEffect(FocusPoint focus, float detail, float intensity, float speed) implements ScreenEffect {
	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"ripple",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			FocusPoint.CODEC.optionalFieldOf("focus", FocusPoint.Screen.CENTER).forGetter(RippleEffect::focus),
			Codec.FLOAT.optionalFieldOf("intensity", 1F).forGetter(RippleEffect::intensity),
			Codec.FLOAT.optionalFieldOf("scale", 50F).forGetter(RippleEffect::detail),
			Codec.FLOAT.optionalFieldOf("speed", 4F).forGetter(RippleEffect::speed)
		).apply(instance, RippleEffect::new)),
		CompositeStreamCodec.of(
			KLibStreamCodecs.optional(FocusPoint.STREAM_CODEC, FocusPoint.Screen.CENTER), RippleEffect::focus,
			KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 1F), RippleEffect::intensity,
			KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 50F), RippleEffect::detail,
			KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 4F), RippleEffect::speed,
			RippleEffect::new
		)
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}
}
