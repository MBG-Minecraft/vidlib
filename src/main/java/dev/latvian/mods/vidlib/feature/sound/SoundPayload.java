package dev.latvian.mods.vidlib.feature.sound;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record SoundPayload(PositionedSoundData data, KNumberVariables variables) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SoundPayload> TYPE = SimplePayloadType.vidlib("sound", CompositeStreamCodec.of(
		PositionedSoundData.STREAM_CODEC, SoundPayload::data,
		KNumberVariables.STREAM_CODEC, SoundPayload::variables,
		SoundPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().playGlobalSound(data, variables);
	}
}
