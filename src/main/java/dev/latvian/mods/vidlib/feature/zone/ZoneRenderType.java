package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import net.minecraft.util.StringRepresentable;

@AutoInit
public enum ZoneRenderType implements StringRepresentable {
	NORMAL("normal"),
	COLLISIONS("collisions"),
	BLOCKS("blocks");

	public static final DataType<ZoneRenderType> DATA_TYPE = DataType.of(values());
	public static final RegisteredDataType<ZoneRenderType> KNOWN_CODEC = RegisteredDataType.register(VidLib.id("zone_render_type"), DATA_TYPE);

	private final String name;

	ZoneRenderType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
