package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CollectionStreamCodecs;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import it.unimi.dsi.fastutil.ints.IntList;

public record RemovePropsPayload(PropListType propListType, IntList ids, PropRemoveType removeType) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<RemovePropsPayload> TYPE = SimplePayloadType.vidlib("prop/remove", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, RemovePropsPayload::propListType,
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

		var props = VLPlatformHelper.CURRENT.getProps(ctx.level());
		var propList = props.propLists.get(propListType);

		for (var id : ids) {
			var prop = propList.get(id);

			if (prop != null) {
				prop.remove(removeType);
			}
		}
	}
}
