package dev.latvian.mods.vidlib.feature.screeneffect.dof;

import dev.latvian.mods.klib.data.DataType;
import net.minecraft.util.StringRepresentable;

public enum DepthOfFieldShape implements StringRepresentable {
	SPHERE("sphere"),
	CYLINDER("cylinder");

	public static final DepthOfFieldShape[] VALUES = values();
	public static final DataType<DepthOfFieldShape> DATA_TYPE = DataType.of(VALUES);

	public final String name;

	DepthOfFieldShape(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
