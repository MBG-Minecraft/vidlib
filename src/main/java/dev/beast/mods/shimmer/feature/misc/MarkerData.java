package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public record MarkerData(String event, String name, UUID uuid) {
	public static final StreamCodec<ByteBuf, MarkerData> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, MarkerData::event,
		ByteBufCodecs.STRING_UTF8, MarkerData::name,
		ShimmerStreamCodecs.UUID, MarkerData::uuid,
		MarkerData::new
	);

	public MarkerData(String event, String name, Entity entity) {
		this(event, name, entity.getUUID());
	}

	public MarkerData(String event, Entity entity) {
		this(event, entity.getName().getString(), entity.getUUID());
	}

	public MarkerData(String event) {
		this(event, "", Util.NIL_UUID);
	}
}
