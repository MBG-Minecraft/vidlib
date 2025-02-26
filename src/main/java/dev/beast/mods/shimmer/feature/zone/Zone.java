package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.util.EmptyCompoundTag;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record Zone(ZoneShape shape, CompoundTag data, Map<EntityOverride<?>, Object> playerOverrides) {
	public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ZoneShape.CODEC.forGetter(Zone::shape),
		CompoundTag.CODEC.optionalFieldOf("data", EmptyCompoundTag.INSTANCE).forGetter(Zone::data),
		EntityOverride.OVERRIDE_MAP_CODEC.optionalFieldOf("player_overrides", Map.of()).forGetter(Zone::playerOverrides)
	).apply(instance, Zone::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Zone> STREAM_CODEC = StreamCodec.composite(
		ZoneShape.STREAM_CODEC,
		Zone::shape,
		ShimmerStreamCodecs.optional(ShimmerStreamCodecs.COMPOUND_TAG, EmptyCompoundTag.INSTANCE),
		Zone::data,
		EntityOverride.OVERRIDE_MAP_STREAM_CODEC,
		Zone::playerOverrides,
		Zone::new
	);

	public Zone(ZoneShape shape, CompoundTag data) {
		this(shape, data, Map.of());
	}

	public Zone(ZoneShape shape) {
		this(shape, EmptyCompoundTag.INSTANCE, Map.of());
	}
}
