package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.util.client.BlendFactorPair;

import java.util.List;
import java.util.Map;

public interface VLClientCodecs {
	Codec<BlendFunction> NAMED_BLEND_FUNCTION = VLCodecs.partialMap(Map.of(
		"lightning", BlendFunction.LIGHTNING,
		"glint", BlendFunction.GLINT,
		"overlay", BlendFunction.OVERLAY,
		"translucent", BlendFunction.TRANSLUCENT,
		"additive", BlendFunction.ADDITIVE,
		"panorama", BlendFunction.PANORAMA,
		"entity_outline_blit", BlendFunction.ENTITY_OUTLINE_BLIT
	), Codec.STRING, false);

	Codec<BlendFunction> DIRECT_BLEND_FUNCTION = RecordCodecBuilder.create(instance -> instance.group(
		BlendFactorPair.CODEC.optionalFieldOf("color", BlendFactorPair.TRANSLUCENT_COLOR).forGetter(BlendFactorPair::color),
		BlendFactorPair.CODEC.optionalFieldOf("alpha", BlendFactorPair.TRANSLUCENT_ALPHA).forGetter(BlendFactorPair::alpha)
	).apply(instance, BlendFactorPair::toFunction));

	Codec<BlendFunction> DIRECT_PAIR_BLEND_FUNCTION = BlendFactorPair.CODEC.xmap(BlendFactorPair::toFunction, BlendFactorPair::color);

	Codec<BlendFunction> BLEND_FUNCTION = VLCodecs.or(List.of(NAMED_BLEND_FUNCTION, DIRECT_PAIR_BLEND_FUNCTION, DIRECT_BLEND_FUNCTION));
}
