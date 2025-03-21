package dev.beast.mods.shimmer.feature.sound;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.network.codec.ByteBufCodecs;

public record TrackingSoundPayload(WorldPosition position, WorldNumberVariables variables, SoundData data, boolean looping) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<TrackingSoundPayload> TYPE = ShimmerPacketType.internal("tracking_sound", CompositeStreamCodec.of(
		WorldPosition.STREAM_CODEC, TrackingSoundPayload::position,
		WorldNumberVariables.STREAM_CODEC, TrackingSoundPayload::variables,
		SoundData.STREAM_CODEC, TrackingSoundPayload::data,
		ByteBufCodecs.BOOL, TrackingSoundPayload::looping,
		TrackingSoundPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().playTrackingSound(position, variables, data, looping);
	}
}
