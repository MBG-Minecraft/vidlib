package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.data.DataType;
import net.minecraft.util.StringRepresentable;

public enum ColorBlendMode implements StringRepresentable {
	MULTIPLICATIVE("multiplicative", "Multiplicative"),
	ADDITIVE("additive", "Additive"),
	SUBTRACTIVE("subtractive", "Subtractive"),

	;

	public static final ColorBlendMode[] VALUES = values();
	public static final DataType<ColorBlendMode> DATA_TYPE = DataType.of(VALUES);

	private final String name;
	public final String displayName;

	ColorBlendMode(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
