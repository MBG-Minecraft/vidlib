package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import it.unimi.dsi.fastutil.ints.IntList;

public record RemovePropsPayload(IntList ids) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemovePropsPayload> TYPE = VidLibPacketType.internal("remove_props", VLStreamCodecs.VAR_INT_LIST.map(RemovePropsPayload::new, RemovePropsPayload::ids));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = ctx.level().getProps();

		for (var id : ids) {
			var prop = props.active.get(id.intValue());

			if (prop != null) {
				prop.remove();
			}
		}
	}
}
