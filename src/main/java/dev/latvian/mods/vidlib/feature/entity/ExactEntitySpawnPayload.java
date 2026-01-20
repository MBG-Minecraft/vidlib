package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.core.VLClientPlayPacketListener;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ExactEntitySpawnPayload(
	EntityType<?> type,
	UUID uuid,
	int id,
	Vec3 pos,
	Vec3 velocity,
	float xRot,
	float yRot,
	float yHeadRot,
	int data
) implements SimplePacketPayload {
	public static final StreamCodec<ByteBuf, ExactEntitySpawnPayload> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC.map(BuiltInRegistries.ENTITY_TYPE::getValue, t -> t.builtInRegistryHolder().getKey().location()), ExactEntitySpawnPayload::type,
		KLibStreamCodecs.UUID, ExactEntitySpawnPayload::uuid,
		ByteBufCodecs.VAR_INT, ExactEntitySpawnPayload::id,
		MCStreamCodecs.VEC3, ExactEntitySpawnPayload::pos,
		MCStreamCodecs.VEC3S, ExactEntitySpawnPayload::velocity,
		ByteBufCodecs.FLOAT, ExactEntitySpawnPayload::xRot,
		ByteBufCodecs.FLOAT, ExactEntitySpawnPayload::yRot,
		ByteBufCodecs.FLOAT, ExactEntitySpawnPayload::yHeadRot,
		ByteBufCodecs.VAR_INT, ExactEntitySpawnPayload::data,
		ExactEntitySpawnPayload::new
	);

	@AutoPacket
	public static final VidLibPacketType<ExactEntitySpawnPayload> TYPE = VidLibPacketType.internal("exact_entity_spawn", STREAM_CODEC);

	public ExactEntitySpawnPayload(Entity entity, ServerEntity serverEntity, int data) {
		this(
			entity.getType(),
			entity.getUUID(),
			entity.getId(),
			serverEntity.getPositionBase(),
			serverEntity.getLastSentMovement(),
			serverEntity.getLastSentXRot(),
			serverEntity.getLastSentYRot(),
			serverEntity.getLastSentYHeadRot(),
			data
		);
	}

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	public void update(Entity entity) {
		entity.syncPacketPositionCodec(pos.x, pos.y, pos.z);
		entity.snapTo(pos, yRot, xRot);
		entity.setId(id);
		entity.setUUID(uuid);
	}

	@Nullable
	public Entity spawnEntity(Context ctx) {
		if (ctx.listener() instanceof VLClientPlayPacketListener listener) {
			return listener.vl$addEntity(ctx, this);
		}

		return null;
	}

	@Override
	public void handle(Context ctx) {
		spawnEntity(ctx);
	}
}
