package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.MiscUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

public class WorldSync {
	public static final UUID CHAT_UUID = UUID.nameUUIDFromBytes("worldsync".getBytes(StandardCharsets.UTF_8));

	public static final Lazy<Path> DIR = Shimmer.HOME_DIR.<Path>map(path -> MiscUtils.createDir(path.resolve("world-sync")));
}
