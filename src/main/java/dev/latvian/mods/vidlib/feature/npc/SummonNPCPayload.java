package dev.latvian.mods.vidlib.feature.npc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.world.phys.Vec3;

public record SummonNPCPayload(NPCParticleOptions options, Vec3 pos) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SummonNPCPayload> TYPE = VidLibPacketType.internal("summon_npc", CompositeStreamCodec.of(
		NPCParticleOptions.STREAM_CODEC, SummonNPCPayload::options,
		MCStreamCodecs.VEC3, SummonNPCPayload::pos,
		SummonNPCPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().addParticle(options, true, true, pos.x, pos.y, pos.z, 0D, 0D, 0D);
	}
}
