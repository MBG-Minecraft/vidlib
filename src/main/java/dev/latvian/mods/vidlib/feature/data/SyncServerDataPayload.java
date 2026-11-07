package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

import java.util.List;

public record SyncServerDataPayload(List<DataMapValue> update) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SyncServerDataPayload> TYPE = SimplePayloadType.vidlib("sync_server_data", DataKey.SERVER.valueListStreamCodec.map(SyncServerDataPayload::new, SyncServerDataPayload::update));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		for (var u : update) {
			if (u.key() != null && !u.key().skipLogging()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void handle(Context ctx) {
		ctx.playerSessionData().worldData.updateServerData(ctx.player(), update);
	}
}
