package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import it.unimi.dsi.fastutil.ints.IntList;

public record RemovePropsPayload(PropListType type, IntList ids) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemovePropsPayload> TYPE = VidLibPacketType.internal("remove_props", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, RemovePropsPayload::type,
		VLStreamCodecs.VAR_INT_LIST, RemovePropsPayload::ids,
		RemovePropsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = ctx.level().getProps();

		for (var id : ids) {
			var prop = props.propLists.get(type).get(id);

			if (prop != null) {
				prop.remove();
			}
		}
	}
}
