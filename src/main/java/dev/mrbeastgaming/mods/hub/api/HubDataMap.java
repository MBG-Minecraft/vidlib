package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class HubDataMap {
	public static final HubDataMap EMPTY = new HubDataMap();

	public static final Codec<HubDataMap> CODEC = MapCodec.unitCodec(EMPTY);
}
