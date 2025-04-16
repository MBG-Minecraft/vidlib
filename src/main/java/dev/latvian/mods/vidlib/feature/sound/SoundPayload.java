package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;

public record SoundPayload(PositionedSoundData data, WorldNumberVariables variables) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SoundPayload> TYPE = VidLibPacketType.internal("sound", CompositeStreamCodec.of(
		PositionedSoundData.STREAM_CODEC, SoundPayload::data,
		WorldNumberVariables.STREAM_CODEC, SoundPayload::variables,
		SoundPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().playGlobalSound(data, variables);
	}
}
