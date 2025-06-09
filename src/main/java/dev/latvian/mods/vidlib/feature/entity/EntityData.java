package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.util.Empty;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public record EntityData(int entityId, int id, CompoundTag data) {
	public static final StreamCodec<ByteBuf, EntityData> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, EntityData::entityId,
		ByteBufCodecs.VAR_INT, EntityData::id,
		MCStreamCodecs.COMPOUND_TAG, EntityData::data,
		EntityData::new
	);

	public static EntityData of(Entity entity, int id, CompoundTag data) {
		return new EntityData(entity.getId(), id, data);
	}

	public static EntityData of(Entity entity, int id) {
		return of(entity, id, Empty.COMPOUND_TAG);
	}

	public static EntityData of(Entity entity, int id, Consumer<CompoundTag> data) {
		var tag = new CompoundTag();
		data.accept(tag);
		return of(entity, id, tag);
	}
}
