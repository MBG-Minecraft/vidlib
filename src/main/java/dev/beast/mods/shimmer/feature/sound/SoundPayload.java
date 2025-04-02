package dev.beast.mods.shimmer.feature.sound;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record SoundPayload(Optional<Vec3> pos, SoundData data) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SoundPayload> TYPE = ShimmerPacketType.internal("sound", CompositeStreamCodec.of(
		ShimmerStreamCodecs.VEC_3.optional(), SoundPayload::pos,
		SoundData.STREAM_CODEC, SoundPayload::data,
		SoundPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().playSound(pos, data);
	}
}
