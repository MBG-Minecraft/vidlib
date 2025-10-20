package dev.latvian.mods.vidlib.feature.decal;

public enum DecalFillType {
	SOLID(0),
	GRID(1),
	DIAGONAL(2),
	ANIMATED_DIAGONAL(3),

	;

	public static final DecalFillType[] VALUES = values();
	public static final DecalFillType[] UNIT = {SOLID};

	public final int shaderId;

	DecalFillType(int shaderId) {
		this.shaderId = shaderId;
	}
}
