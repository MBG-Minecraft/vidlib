package dev.beast.mods.shimmer.feature.npc;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.world.phys.Vec3;

public record SummonNPCPayload(NPCParticleOptions options, Vec3 pos) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SummonNPCPayload> TYPE = ShimmerPacketType.internal("summon_npc", CompositeStreamCodec.of(
		NPCParticleOptions.STREAM_CODEC, SummonNPCPayload::options,
		Vec3.STREAM_CODEC, SummonNPCPayload::pos,
		SummonNPCPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().addParticle(options, true, true, pos.x, pos.y, pos.z, 0D, 0D, 0D);
	}
}
