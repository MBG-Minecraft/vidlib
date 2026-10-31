package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

public record ForceEntityVelocityPayload(int entity, Vec3 velocity) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<ForceEntityVelocityPayload> TYPE = SimplePayloadType.vidlib("force_entity_velocity", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, ForceEntityVelocityPayload::entity,
		MCStreamCodecs.VEC3, ForceEntityVelocityPayload::velocity,
		ForceEntityVelocityPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
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
