package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;
import java.util.UUID;

public record UpdatePlayerDataValuePayload(UUID uuid, List<DataMapValue> update) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<UpdatePlayerDataValuePayload> TYPE = VidLibPacketType.internal("update_player_data_value", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, UpdatePlayerDataValuePayload::uuid,
		DataKey.PLAYER.valueListStreamCodec, UpdatePlayerDataValuePayload::update,
		UpdatePlayerDataValuePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		for (var u : update) {
			if (u.key() != null && (u.key().allowClientUpdates() && ctx.player().getUUID().equals(uuid) || ctx.player().hasPermissions(2))) {
				var session = ctx.level().getServer().vl$getOrLoadServerSession(uuid);
				session.dataMap.set(u.key(), Cast.to(u.value()));
			}
		}
	}
}
