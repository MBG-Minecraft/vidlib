package dev.beast.mods.shimmer.util;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.util.StringRepresentable;

public enum ScreenCorner implements StringRepresentable {
	TOP_LEFT("top_left"),
	TOP_RIGHT("top_right"),
	BOTTOM_LEFT("bottom_left"),
	BOTTOM_RIGHT("bottom_right");

	public static final KnownCodec<ScreenCorner> KNOWN_CODEC = KnownCodec.registerEnum(Shimmer.id("screen_corner"), values());

	private final String name;

	ScreenCorner(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
