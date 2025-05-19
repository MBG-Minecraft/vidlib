package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;

public record BlendFactorPair(SourceFactor src, DestFactor dst) {
	public static final Codec<SourceFactor> SRC_FACTOR_CODEC = VLCodecs.anyEnumCodec(SourceFactor.values());
	public static final Codec<DestFactor> DST_FACTOR_CODEC = VLCodecs.anyEnumCodec(DestFactor.values());

	public static final Codec<BlendFactorPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SRC_FACTOR_CODEC.fieldOf("src").forGetter(BlendFactorPair::src),
		DST_FACTOR_CODEC.fieldOf("dst").forGetter(BlendFactorPair::dst)
	).apply(instance, BlendFactorPair::new));

	public static BlendFunction toFunction(BlendFactorPair color, BlendFactorPair alpha) {
		return new BlendFunction(color.src, color.dst, alpha.src, alpha.dst);
	}

	public static BlendFactorPair color(BlendFunction function) {
		return new BlendFactorPair(function.sourceColor(), function.destColor());
	}

	public static BlendFactorPair alpha(BlendFunction function) {
		return new BlendFactorPair(function.sourceAlpha(), function.destAlpha());
	}

	public static final BlendFactorPair TRANSLUCENT_COLOR = color(BlendFunction.TRANSLUCENT);
	public static final BlendFactorPair TRANSLUCENT_ALPHA = alpha(BlendFunction.TRANSLUCENT);

	public BlendFunction toFunction() {
		return new BlendFunction(src, dst);
	}
}
