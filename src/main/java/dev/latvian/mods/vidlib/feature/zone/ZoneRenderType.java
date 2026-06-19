package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.util.StringRepresentable;

@AutoInit
public enum ZoneRenderType implements StringRepresentable {
	NORMAL("normal"),
	COLLISIONS("collisions"),
	BLOCKS("blocks");

	public static final DataType<ZoneRenderType> DATA_TYPE = DataType.of(values());

	private final String name;
	private final String nameKey;

	ZoneRenderType(String name) {
		this.name = name;
		this.nameKey = "options.vidlib.zone_render_type." + name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public int getId() {
		return ordinal();
	}

	public String getKey() {
		return nameKey;
	}
}
