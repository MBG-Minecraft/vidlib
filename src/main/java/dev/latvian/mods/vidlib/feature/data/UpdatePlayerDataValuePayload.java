package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;

public record UpdatePlayerDataValuePayload(List<DataMapValue> update) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<UpdatePlayerDataValuePayload> TYPE = VidLibPacketType.internal("update_player_data_value", DataKey.PLAYER.valueListStreamCodec.map(UpdatePlayerDataValuePayload::new, UpdatePlayerDataValuePayload::update));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		for (var u : update) {
			if (u.key() != null && u.key().allowClientUpdates()) {
				ctx.player().set(u.key(), Cast.to(u.value()));
			}
		}
	}
}
