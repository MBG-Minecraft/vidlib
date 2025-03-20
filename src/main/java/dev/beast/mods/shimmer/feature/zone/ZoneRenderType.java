package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.util.StringRepresentable;

@AutoInit
public enum ZoneRenderType implements StringRepresentable {
	NORMAL("normal"),
	COLLISIONS("collisions"),
	BLOCKS("blocks");

	public static final KnownCodec<ZoneRenderType> KNOWN_CODEC = KnownCodec.registerEnum(Shimmer.id("zone_render_type"), values());

	private final String name;

	ZoneRenderType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
