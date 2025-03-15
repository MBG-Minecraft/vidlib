package dev.beast.mods.shimmer.feature.worldsync;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record WorldSyncProject(
	String id,
	String displayName,
	long size,
	BlockPos spawn,
	ResourceKey<Level> dimension,
	byte[] icon,
	long lastUpdated
) {
}
