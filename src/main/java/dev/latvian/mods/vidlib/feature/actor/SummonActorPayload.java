package dev.latvian.mods.vidlib.feature.actor;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.world.phys.Vec3;

public record SummonActorPayload(ActorParticleOptions options, Vec3 pos) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SummonActorPayload> TYPE = SimplePayloadType.vidlib("summon_actor", CompositeStreamCodec.of(
		ActorParticleOptions.STREAM_CODEC, SummonActorPayload::options,
		MCStreamCodecs.VEC3, SummonActorPayload::pos,
		SummonActorPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().addParticle(options, true, true, pos.x, pos.y, pos.z, 0D, 0D, 0D);
	}
}
