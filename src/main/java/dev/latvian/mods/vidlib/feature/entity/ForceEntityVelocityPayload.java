package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

public record ForceEntityVelocityPayload(int entity, Vec3 velocity) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ForceEntityVelocityPayload> TYPE = VidLibPacketType.internal("force_entity_velocity", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, ForceEntityVelocityPayload::entity,
		MCStreamCodecs.VEC3, ForceEntityVelocityPayload::velocity,
		ForceEntityVelocityPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var e = ctx.level().getEntity(entity);

		if (e != null) {
			e.forceSetVelocity(velocity);
		}
	}
}
