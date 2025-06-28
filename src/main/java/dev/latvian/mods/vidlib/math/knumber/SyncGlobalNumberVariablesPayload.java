package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record SyncGlobalNumberVariablesPayload(KNumberVariables variables) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncGlobalNumberVariablesPayload> TYPE = VidLibPacketType.internal("sync_global_number_variables", KNumberVariables.STREAM_CODEC.map(SyncGlobalNumberVariablesPayload::new, SyncGlobalNumberVariablesPayload::variables));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().getEnvironment().globalVariables().replace(variables);
	}
}
