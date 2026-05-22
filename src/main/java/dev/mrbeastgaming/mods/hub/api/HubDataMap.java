package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;

public class HubDataMap {
	public static final HubDataMap EMPTY = new HubDataMap();

	public static final Codec<HubDataMap> CODEC = Codec.unit(EMPTY);
}
