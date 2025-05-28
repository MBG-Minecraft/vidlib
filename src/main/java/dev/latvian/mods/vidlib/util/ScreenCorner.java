package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import net.minecraft.util.StringRepresentable;

public enum ScreenCorner implements StringRepresentable {
	TOP_LEFT("top_left"),
	TOP_RIGHT("top_right"),
	BOTTOM_LEFT("bottom_left"),
	BOTTOM_RIGHT("bottom_right");

	public static final DataType<ScreenCorner> DATA_TYPE = DataType.of(values());
	public static final RegisteredDataType<ScreenCorner> REGISTERED_DATA_TYPE = RegisteredDataType.register(VidLib.id("screen_corner"), DATA_TYPE);

	private final String name;

	ScreenCorner(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
