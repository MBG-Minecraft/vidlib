package dev.beast.mods.shimmer.feature.sound;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SoundPayload(Vec3 pos, SoundData data, long gameTime) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SoundPayload> TYPE = ShimmerPacketType.internal("sound", CompositeStreamCodec.of(
		ShimmerStreamCodecs.VEC_3, SoundPayload::pos,
		SoundData.STREAM_CODEC, SoundPayload::data,
		ByteBufCodecs.VAR_LONG, SoundPayload::gameTime,
		SoundPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().level().playSound(pos, data);
	}
}
