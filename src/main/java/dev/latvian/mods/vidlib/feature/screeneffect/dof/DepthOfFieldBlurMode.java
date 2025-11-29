package dev.latvian.mods.vidlib.feature.screeneffect.dof;

import dev.latvian.mods.klib.data.DataType;
import net.minecraft.util.StringRepresentable;

public enum DepthOfFieldBlurMode implements StringRepresentable {
	DEPTH("depth"),
	EDGE("edge"),
	FAST("fast");

	public static final DepthOfFieldBlurMode[] VALUES = values();
	public static final DataType<DepthOfFieldBlurMode> DATA_TYPE = DataType.of(VALUES);

	public final String name;

	DepthOfFieldBlurMode(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
