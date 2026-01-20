package dev.latvian.mods.vidlib.feature.screeneffect.dof;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DepthOfFieldData(
	KVector focus,
	float focusRange,
	float blurRange,
	float strength,
	DepthOfFieldShape shape,
	DepthOfFieldBlurMode blurMode
) {
	public DepthOfFieldData withFocus(KVector focus) {
		return new DepthOfFieldData(focus, focusRange, blurRange, strength, shape, blurMode);
	}

	public static final Codec<DepthOfFieldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KVector.CODEC.fieldOf("focus").forGetter(DepthOfFieldData::focus),
		Codec.FLOAT.fieldOf("focus_range").forGetter(DepthOfFieldData::focusRange),
		Codec.FLOAT.fieldOf("blur_range").forGetter(DepthOfFieldData::blurRange),
		Codec.FLOAT.fieldOf("strength").forGetter(DepthOfFieldData::strength),
		DepthOfFieldShape.DATA_TYPE.codec().fieldOf("shape").forGetter(DepthOfFieldData::shape),
		DepthOfFieldBlurMode.DATA_TYPE.codec().fieldOf("blur_mode").forGetter(DepthOfFieldData::blurMode)
	).apply(instance, DepthOfFieldData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DepthOfFieldData> STREAM_CODEC = CompositeStreamCodec.of(
		KVector.STREAM_CODEC, DepthOfFieldData::focus,
		ByteBufCodecs.FLOAT, DepthOfFieldData::focusRange,
		ByteBufCodecs.FLOAT, DepthOfFieldData::blurRange,
		ByteBufCodecs.FLOAT, DepthOfFieldData::strength,
		DepthOfFieldShape.DATA_TYPE.streamCodec(), DepthOfFieldData::shape,
		DepthOfFieldBlurMode.DATA_TYPE.streamCodec(), DepthOfFieldData::blurMode,
		DepthOfFieldData::new
	);
}

