package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;

public enum WindType {
	ANGLED,
	CIRCULAR,
	SQUARE;

	public static final KnownCodec<WindType> KNOWN_CODEC = KnownCodec.registerEnum(Shimmer.id("wind_type"), values());
}
