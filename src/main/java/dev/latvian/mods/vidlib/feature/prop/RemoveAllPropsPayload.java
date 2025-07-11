package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record RemoveAllPropsPayload(PropListType type, PropRemoveType removeType) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemoveAllPropsPayload> TYPE = VidLibPacketType.internal("prop/remove_all", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, RemoveAllPropsPayload::type,
		PropRemoveType.STREAM_CODEC, RemoveAllPropsPayload::removeType,
		RemoveAllPropsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = ctx.level().getProps().propLists.get(type);

		for (var prop : props) {
			prop.removed = removeType;
		}
	}
}
