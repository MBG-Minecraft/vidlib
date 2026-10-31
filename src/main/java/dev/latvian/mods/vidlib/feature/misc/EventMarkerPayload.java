package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Optional;

@Deprecated
public record EventMarkerPayload(String event, Optional<Tag> tag) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<EventMarkerPayload> TYPE = SimplePayloadType.vidlib("event_marker", CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, EventMarkerPayload::event,
		ByteBufCodecs.optional(ByteBufCodecs.TRUSTED_TAG), EventMarkerPayload::tag,
		EventMarkerPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
	}
}
