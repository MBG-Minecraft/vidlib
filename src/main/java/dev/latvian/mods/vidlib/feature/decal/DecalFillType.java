package dev.latvian.mods.vidlib.feature.decal;

import net.minecraft.util.StringRepresentable;

public enum DecalFillType implements StringRepresentable {
	SOLID(0, "solid", "Solid"),
	GRID(1, "grid", "Grid"),
	DIAGONAL(2, "diagonal", "Diagonal"),
	ANIMATED_DIAGONAL(3, "animated_diagonal", "Animated Diagonal"),

	;

	public static final DecalFillType[] VALUES = values();
	public static final DecalFillType[] UNIT = {SOLID};

	public final int shaderId;
	public final String name;
	public final String displayName;

	DecalFillType(int shaderId, String name, String displayName) {
		this.shaderId = shaderId;
		this.name = name;
		this.displayName = displayName;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
