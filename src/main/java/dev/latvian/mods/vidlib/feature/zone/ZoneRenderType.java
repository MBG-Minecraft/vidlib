package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

@AutoInit
public enum ZoneRenderType implements OptionEnum, StringRepresentable {
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

	@Override
	public int getId() {
		return ordinal();
	}

	@Override
	public String getKey() {
		return nameKey;
	}
}
