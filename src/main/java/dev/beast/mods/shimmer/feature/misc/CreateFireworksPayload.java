package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.FireworkExplosion;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record CreateFireworksPayload(double x, double y, double z, double vx, double vy, double vz, List<FireworkExplosion> explosions) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<CreateFireworksPayload> TYPE = ShimmerPacketType.internal("create_fireworks", CompositeStreamCodec.of(
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::x,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::y,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::z,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::vx,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::vy,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::vz,
		FireworkExplosion.STREAM_CODEC.list(), CreateFireworksPayload::explosions,
		CreateFireworksPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().level().createFireworks(x, y, z, vx, vy, vz, explosions);
	}
}
