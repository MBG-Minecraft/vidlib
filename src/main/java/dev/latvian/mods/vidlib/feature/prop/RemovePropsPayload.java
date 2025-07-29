package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CollectionStreamCodecs;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.misc.VLFlashbackIntegration;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import it.unimi.dsi.fastutil.ints.IntList;

public record RemovePropsPayload(PropListType type, IntList ids, PropRemoveType removeType) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemovePropsPayload> TYPE = VidLibPacketType.internal("prop/remove", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, RemovePropsPayload::type,
		CollectionStreamCodecs.VAR_INT_LIST, RemovePropsPayload::ids,
		PropRemoveType.STREAM_CODEC, RemovePropsPayload::removeType,
		RemovePropsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (VLFlashbackIntegration.ENABLED && VLFlashbackIntegration.RECORDED_PROPS != null) {
			return;
		}

		var props = ctx.level().getProps().propLists.get(type);

		for (var id : ids) {
			var prop = props.get(id);

			if (prop != null) {
				prop.remove(removeType);
			}
		}
	}
}
