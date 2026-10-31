package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

import java.util.List;

public record UpdateServerDataValuePayload(List<DataMapValue> update) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<UpdateServerDataValuePayload> TYPE = SimplePayloadType.vidlib("update_server_data_value", DataKey.SERVER.valueListStreamCodec.map(UpdateServerDataValuePayload::new, UpdateServerDataValuePayload::update));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			for (var u : update) {
				if (u.key() != null) {
					ctx.level().getServer().set(u.key(), Cast.to(u.value()));
				}
			}
		}
	}
}
