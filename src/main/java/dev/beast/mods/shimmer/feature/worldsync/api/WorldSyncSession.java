package dev.beast.mods.shimmer.feature.worldsync.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public record WorldSyncSession(
	String id,
	long totalSize,
	ResourceKey<Level> dimension,
	BlockPos spawn,
	List<WorldSyncSessionFile> files
) {
	public static final Codec<WorldSyncSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(WorldSyncSession::id),
		Codec.LONG.optionalFieldOf("total_size", 0L).forGetter(WorldSyncSession::totalSize),
		ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension", Level.OVERWORLD).forGetter(WorldSyncSession::dimension),
		BlockPos.CODEC.optionalFieldOf("spawn", new BlockPos(0, 100, 0)).forGetter(WorldSyncSession::spawn),
		WorldSyncSessionFile.CODEC.listOf().optionalFieldOf("files", List.of()).forGetter(WorldSyncSession::files)
	).apply(instance, WorldSyncSession::new));
}
