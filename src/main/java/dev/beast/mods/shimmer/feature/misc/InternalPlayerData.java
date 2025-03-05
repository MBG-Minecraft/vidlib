package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.session.PlayerDataType;

public class InternalPlayerData {
	public static final PlayerDataType<InternalGlobalPlayerData> GLOBAL = PlayerDataType.builder(Shimmer.id("global"), InternalGlobalPlayerData::new)
		.save(InternalGlobalPlayerData.CODEC)
		.sync(InternalGlobalPlayerData.STREAM_CODEC)
		.syncToAllClients()
		.build();

	public static final PlayerDataType<InternalLocalPlayerData> LOCAL = PlayerDataType.builder(Shimmer.id("local"), InternalLocalPlayerData::new)
		.save(InternalLocalPlayerData.CODEC)
		.sync(InternalLocalPlayerData.STREAM_CODEC)
		.build();

	public static void bootstrap() {
	}
}
