package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.data.DataType;
import net.minecraft.util.StringRepresentable;

public enum DecalType implements StringRepresentable {
	// Static types
	NONE("none", 0),
	SPHERE("sphere", 1),
	CYLINDER("cylinder", 2),
	SQUARE("square", 3),
	HEXAGON("hexagon", 4),
	// FILLED
	FILLED_CYLINDER("filled_cylinder", 5),
	CUBE("cube", 6),
	OCTAGON("octagon", 7),
	// Dynamic types
	DANGER("danger", -1),

	;

	public static final DecalType[] VALUES = values();
	public static final DecalType[] UNIT = {NONE};
	public static final DataType<DecalType> DATA_TYPE = DataType.of(VALUES);

	public final String name;
	public final int shaderId;

	DecalType(String name, int shaderId) {
		this.name = name;
		this.shaderId = shaderId;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
