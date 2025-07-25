package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.FireworkExplosion;

import java.util.List;

public record CreateFireworksPayload(double x, double y, double z, double vx, double vy, double vz, List<FireworkExplosion> explosions) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<CreateFireworksPayload> TYPE = VidLibPacketType.internal("create_fireworks", CompositeStreamCodec.of(
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::x,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::y,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::z,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::vx,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::vy,
		ByteBufCodecs.DOUBLE, CreateFireworksPayload::vz,
		KLibStreamCodecs.listOf(FireworkExplosion.STREAM_CODEC), CreateFireworksPayload::explosions,
		CreateFireworksPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().createFireworks(x, y, z, vx, vy, vz, explosions);
	}
}
