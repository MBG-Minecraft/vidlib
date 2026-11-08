package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CollectionStreamCodecs;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import it.unimi.dsi.fastutil.ints.IntList;

public record RemovePropsPayload(IntList ids, PropRemoveType removeType) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<RemovePropsPayload> TYPE = SimplePayloadType.vidlib("prop/remove", CompositeStreamCodec.of(
		CollectionStreamCodecs.VAR_INT_LIST, RemovePropsPayload::ids,
		PropRemoveType.STREAM_CODEC, RemovePropsPayload::removeType,
		RemovePropsPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ReplayProp.LIST != null) {
			return;
		}

		var props = ctx.levelSessionData().props;

		for (var id : ids) {
			var prop = props.levelProps.get(id);

			if (prop != null) {
				prop.remove(removeType);
			}
		}
	}
}
