package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.util.Empty;
import dev.latvian.mods.kmath.color.Color;
import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Zone(
	ZoneShape shape,
	Color color,
	EntityFilter entityFilter,
	CompoundTag data,
	Map<EntityOverride<?>, Object> playerOverrides,
	EntityFilter solid,
	Set<String> tags,
	boolean forceLoaded
) {
	public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ZoneShape.CODEC.fieldOf("shape").forGetter(Zone::shape),
		Color.CODEC.optionalFieldOf("color", Color.CYAN).forGetter(Zone::color),
		EntityFilter.CODEC.optionalFieldOf("entity_filter", EntityFilter.PLAYER.instance()).forGetter(Zone::entityFilter),
		CompoundTag.CODEC.optionalFieldOf("data", Empty.COMPOUND_TAG).forGetter(Zone::data),
		EntityOverride.OVERRIDE_MAP_CODEC.optionalFieldOf("player_overrides", Map.of()).forGetter(Zone::playerOverrides),
		EntityFilter.CODEC.optionalFieldOf("solid", EntityFilter.NONE.instance()).forGetter(Zone::solid),
		ShimmerCodecs.set(Codec.STRING).optionalFieldOf("tags", Set.of()).forGetter(Zone::tags),
		Codec.BOOL.optionalFieldOf("force_loaded", false).forGetter(Zone::forceLoaded)
	).apply(instance, Zone::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Zone> STREAM_CODEC = CompositeStreamCodec.of(
		ZoneShape.STREAM_CODEC, Zone::shape,
		Color.STREAM_CODEC, Zone::color,
		EntityFilter.STREAM_CODEC, Zone::entityFilter,
		ShimmerStreamCodecs.COMPOUND_TAG.optional(Empty.COMPOUND_TAG), Zone::data,
		EntityOverride.OVERRIDE_MAP_STREAM_CODEC, Zone::playerOverrides,
		EntityFilter.STREAM_CODEC, Zone::solid,
		ByteBufCodecs.STRING_UTF8.linkedSet(), Zone::tags,
		ByteBufCodecs.BOOL, Zone::forceLoaded,
		Zone::new
	);

	public static final TicketType<ChunkPos> TICKET_TYPE = TicketType.create("shimmer:zone", Comparator.comparingLong(ChunkPos::toLong));

	public void writeUUID(FriendlyByteBuf buf) {
		shape.writeUUID(buf);
		buf.writeInt(color.argb());
		entityFilter.writeUUID(buf);
		ShimmerStreamCodecs.COMPOUND_TAG.encode(buf, data);
		solid.writeUUID(buf);
	}

	public UUID computeUUID() {
		var buf = new FriendlyByteBuf(Unpooled.buffer());
		writeUUID(buf);

		try {
			return UUID.nameUUIDFromBytes(buf.array());
		} catch (Exception e) {
			return Util.NIL_UUID;
		}
	}
}
