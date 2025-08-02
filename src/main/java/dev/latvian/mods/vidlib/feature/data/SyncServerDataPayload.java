package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;

public record SyncServerDataPayload(List<DataMapValue> update) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncServerDataPayload> TYPE = VidLibPacketType.internal("sync_server_data", DataKey.SERVER.valueListStreamCodec.map(SyncServerDataPayload::new, SyncServerDataPayload::update));

	@Override
	public VidLibPacketType<?> getType() {
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
		ctx.player().vl$sessionData().updateServerData(ctx.remoteGameTime(), ctx.player(), update);
	}
}
