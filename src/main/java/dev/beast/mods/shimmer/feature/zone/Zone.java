package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.Empty;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record Zone(
	ZoneShape shape,
	Color color,
	EntityFilter entityFilter,
	CompoundTag data,
	Map<EntityOverride<?>, Object> playerOverrides
) {
	public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ZoneShape.CODEC.forGetter(Zone::shape),
		Color.CODEC_RGB.optionalFieldOf("color", Color.CYAN).forGetter(Zone::color),
		EntityFilter.CODEC.optionalFieldOf("entity_filter", EntityFilter.PLAYER).forGetter(Zone::entityFilter),
		net.minecraft.nbt.CompoundTag.CODEC.optionalFieldOf("data", Empty.COMPOUND_TAG).forGetter(Zone::data),
		EntityOverride.OVERRIDE_MAP_CODEC.optionalFieldOf("player_overrides", Map.of()).forGetter(Zone::playerOverrides)
	).apply(instance, Zone::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Zone> STREAM_CODEC = StreamCodec.composite(
		ZoneShape.STREAM_CODEC,
		Zone::shape,
		Color.STREAM_CODEC,
		Zone::color,
		EntityFilter.STREAM_CODEC,
		Zone::entityFilter,
		ShimmerStreamCodecs.optional(ShimmerStreamCodecs.COMPOUND_TAG, Empty.COMPOUND_TAG),
		Zone::data,
		EntityOverride.OVERRIDE_MAP_STREAM_CODEC,
		Zone::playerOverrides,
		Zone::new
	);
}
