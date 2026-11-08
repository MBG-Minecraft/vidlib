package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;

import java.util.List;
import java.util.UUID;

public record SyncPlayerDataPayload(UUID player, List<DataMapValue<PlayerSessionData>> update) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SyncPlayerDataPayload> TYPE = SimplePayloadType.vidlib("sync_player_data", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, SyncPlayerDataPayload::player,
		DataKey.PLAYER.valueListStreamCodec, SyncPlayerDataPayload::update,
		SyncPlayerDataPayload::new
	));

	public SyncPlayerDataPayload(PlayerSessionData data, List<DataMapValue<PlayerSessionData>> update) {
		this(data.uuid, update);
	}

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		for (var u : update) {
			if (u.key() != null && !u.key().skipLogging) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void handle(Context ctx) {
		var session = ctx.globalSessionData();
		session.getPlayer(player).dataMap.update(session, update);
	}
}
