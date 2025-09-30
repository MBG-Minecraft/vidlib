package dev.latvian.mods.vidlib.feature.decal;

public enum DecalType {
	// Static types
	NONE(0),
	SPHERE(1),
	CYLINDER(2),
	FILLED_CYLINDER(3),
	SQUARE(4),
	CUBE(5),
	UNDEFINED_6(6),
	UNDEFINED_7(7),
	// Dynamic types
	DANGER(-1),

	;

	public static final DecalType[] VALUES = {NONE, SPHERE, CYLINDER, FILLED_CYLINDER, SQUARE, CUBE, DANGER};
	public static final DecalType[] UNIT = {NONE};

	public final int shaderId;

	DecalType(int shaderId) {
		this.shaderId = shaderId;
	}
}
