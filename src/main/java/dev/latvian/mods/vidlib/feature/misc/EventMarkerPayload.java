package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Optional;

public record EventMarkerPayload(String event, Optional<Tag> tag) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<EventMarkerPayload> TYPE = VidLibPacketType.internal("event_marker", CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, EventMarkerPayload::event,
		ByteBufCodecs.optional(ByteBufCodecs.TRUSTED_TAG), EventMarkerPayload::tag,
		EventMarkerPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().marker(event, tag.orElse(null));
	}
}
