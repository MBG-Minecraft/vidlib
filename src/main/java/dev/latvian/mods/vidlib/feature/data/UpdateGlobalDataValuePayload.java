package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;

import java.util.List;

public record UpdateGlobalDataValuePayload(List<DataMapValue<GlobalSessionData>> update) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<UpdateGlobalDataValuePayload> TYPE = SimplePayloadType.vidlib("update_server_data_value", DataKey.GLOBAL.valueListStreamCodec.map(UpdateGlobalDataValuePayload::new, UpdateGlobalDataValuePayload::update));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			var dataMap = ctx.globalSessionData().dataMap;

			for (var u : update) {
				if (u.key() != null) {
					dataMap.set(u.key(), Cast.to(u.value()));
				}
			}
		}
	}
}
