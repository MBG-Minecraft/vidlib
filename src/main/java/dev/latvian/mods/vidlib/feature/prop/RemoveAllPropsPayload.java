package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record RemoveAllPropsPayload(PropListType type) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemoveAllPropsPayload> TYPE = VidLibPacketType.internal("remove_all_props", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, RemoveAllPropsPayload::type,
		RemoveAllPropsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().getProps().propLists.get(type).removeAll();
	}
}
