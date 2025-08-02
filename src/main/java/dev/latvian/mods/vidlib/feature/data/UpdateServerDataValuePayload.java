package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;

public record UpdateServerDataValuePayload(List<DataMapValue> update) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<UpdateServerDataValuePayload> TYPE = VidLibPacketType.internal("update_server_data_value", DataKey.SERVER.valueListStreamCodec.map(UpdateServerDataValuePayload::new, UpdateServerDataValuePayload::update));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			for (var u : update) {
				if (u.key() != null && u.key().allowClientUpdates()) {
					ctx.level().getServer().set(u.key(), Cast.to(u.value()));
				}
			}
		}
	}
}
