package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldBlurMode;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldShape;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

public record DepthOfFieldEffect(
	Ref<KVector> focus,
	float focusRange,
	float blurRange,
	float strength,
	DepthOfFieldShape shape,
	DepthOfFieldBlurMode blurMode
) implements ScreenEffect {
	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"depth_of_field",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			KVector.CODEC.fieldOf("focus").forGetter(DepthOfFieldEffect::focus),
			Codec.FLOAT.fieldOf("focus_range").forGetter(DepthOfFieldEffect::focusRange),
			Codec.FLOAT.fieldOf("blur_range").forGetter(DepthOfFieldEffect::blurRange),
			Codec.FLOAT.fieldOf("strength").forGetter(DepthOfFieldEffect::strength),
			DepthOfFieldShape.DATA_TYPE.codec().fieldOf("shape").forGetter(DepthOfFieldEffect::shape),
			DepthOfFieldBlurMode.DATA_TYPE.codec().fieldOf("blur_mode").forGetter(DepthOfFieldEffect::blurMode)
		).apply(instance, DepthOfFieldEffect::new)),
		CompositeStreamCodec.of(
			KVector.STREAM_CODEC, DepthOfFieldEffect::focus,
			ByteBufCodecs.FLOAT, DepthOfFieldEffect::focusRange,
			ByteBufCodecs.FLOAT, DepthOfFieldEffect::blurRange,
			ByteBufCodecs.FLOAT, DepthOfFieldEffect::strength,
			DepthOfFieldShape.DATA_TYPE.streamCodec(), DepthOfFieldEffect::shape,
			DepthOfFieldBlurMode.DATA_TYPE.streamCodec(), DepthOfFieldEffect::blurMode,
			DepthOfFieldEffect::new
		)
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}
}
