package dev.beast.mods.shimmer.feature.worldsync.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record WorldSyncSessionFile(
	String checksum,
	long size,
	boolean compress,
	List<String> names
) {
	public static final Codec<WorldSyncSessionFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("checksum").forGetter(WorldSyncSessionFile::checksum),
		Codec.LONG.optionalFieldOf("size", 0L).forGetter(WorldSyncSessionFile::size),
		Codec.BOOL.optionalFieldOf("compress", false).forGetter(WorldSyncSessionFile::compress),
		Codec.STRING.listOf().optionalFieldOf("names", List.of()).forGetter(WorldSyncSessionFile::names)
	).apply(instance, WorldSyncSessionFile::new));
}
