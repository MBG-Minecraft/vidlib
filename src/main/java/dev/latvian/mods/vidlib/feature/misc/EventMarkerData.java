package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public record EventMarkerData(String event, String name, UUID uuid) {
	public static final StreamCodec<ByteBuf, EventMarkerData> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, EventMarkerData::event,
		ByteBufCodecs.STRING_UTF8, EventMarkerData::name,
		KLibStreamCodecs.UUID, EventMarkerData::uuid,
		EventMarkerData::new
	);

	public EventMarkerData(String event, String name, Entity entity) {
		this(event, name, entity.getUUID());
	}

	public EventMarkerData(String event, Entity entity) {
		this(event, entity.getName().getString(), entity.getUUID());
	}

	public EventMarkerData(String event) {
		this(event, "", Util.NIL_UUID);
	}
}
