package dev.beast.mods.shimmer.feature.sound;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrackingSoundPayload(int entity, SoundData data, boolean looping) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<TrackingSoundPayload> TYPE = ShimmerPacketType.internal("repeating_tracking_sound", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, TrackingSoundPayload::entity,
		SoundData.STREAM_CODEC, TrackingSoundPayload::data,
		ByteBufCodecs.BOOL, TrackingSoundPayload::looping,
		TrackingSoundPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		var e = ctx.player().level().getEntity(entity);

		if (e != null) {
			ctx.player().level().playTrackingSound(e, data, looping);
		}
	}
}
