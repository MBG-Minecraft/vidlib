package dev.beast.mods.shimmer.feature.worldsync.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WorldSyncWorld(
	String id,
	String displayName
) {
	public static final Codec<WorldSyncWorld> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(WorldSyncWorld::id),
		Codec.STRING.fieldOf("display_name").forGetter(WorldSyncWorld::id)
	).apply(instance, WorldSyncWorld::new));
}
