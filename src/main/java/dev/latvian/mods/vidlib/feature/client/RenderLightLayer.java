package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.data.DataType;
import net.minecraft.util.StringRepresentable;

public enum RenderLightLayer implements StringRepresentable {
	NORMAL("normal"),
	BRIGHT("bright"),
	BLOOM("bloom");

	public static final RenderLightLayer[] VALUES = values();
	public static final DataType<RenderLightLayer> DATA_TYPE = DataType.of(VALUES);

	private final String name;

	RenderLightLayer(final String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
