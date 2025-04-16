package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import net.minecraft.util.StringRepresentable;

@AutoInit
public enum ZoneRenderType implements StringRepresentable {
	NORMAL("normal"),
	COLLISIONS("collisions"),
	BLOCKS("blocks");

	public static final KnownCodec<ZoneRenderType> KNOWN_CODEC = KnownCodec.registerEnum(VidLib.id("zone_render_type"), values());

	private final String name;

	ZoneRenderType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
