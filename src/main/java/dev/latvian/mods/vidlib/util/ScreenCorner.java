package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.data.DataType;
import net.minecraft.util.StringRepresentable;

public enum ScreenCorner implements StringRepresentable {
	TOP_LEFT("top_left"),
	TOP_RIGHT("top_right"),
	BOTTOM_LEFT("bottom_left"),
	BOTTOM_RIGHT("bottom_right");

	public static final DataType<ScreenCorner> DATA_TYPE = DataType.of(values());

	private final String name;

	ScreenCorner(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
