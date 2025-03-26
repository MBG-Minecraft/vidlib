package dev.beast.mods.shimmer.feature.entity;

import dev.beast.mods.shimmer.core.ShimmerClientPacketListener;
import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

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
) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<ExactEntitySpawnPayload> TYPE = ShimmerPacketType.internal("exact_entity_spawn", CompositeStreamCodec.of(
		ShimmerStreamCodecs.VIDEO_ID.map(BuiltInRegistries.ENTITY_TYPE::getValue, t -> t.builtInRegistryHolder().getKey().location()), ExactEntitySpawnPayload::type,
		ShimmerStreamCodecs.UUID, ExactEntitySpawnPayload::uuid,
		ByteBufCodecs.VAR_INT, ExactEntitySpawnPayload::id,
		ShimmerStreamCodecs.VEC_3, ExactEntitySpawnPayload::pos,
		ShimmerStreamCodecs.VEC_3, ExactEntitySpawnPayload::velocity,
		ByteBufCodecs.FLOAT, ExactEntitySpawnPayload::xRot,
		ByteBufCodecs.FLOAT, ExactEntitySpawnPayload::yRot,
		ByteBufCodecs.FLOAT, ExactEntitySpawnPayload::yHeadRot,
		ByteBufCodecs.VAR_INT, ExactEntitySpawnPayload::data,
		ExactEntitySpawnPayload::new
	));

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
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	public void update(Entity entity) {
		entity.syncPacketPositionCodec(pos.x, pos.y, pos.z);
		entity.moveTo(pos.x, pos.y, pos.z, yRot, xRot);
		entity.setId(id);
		entity.setUUID(uuid);
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		if (ctx.parent().listener() instanceof ShimmerClientPacketListener listener) {
			listener.shimmer$addEntity(ctx, this);
		}
	}
}
