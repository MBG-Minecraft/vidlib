package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record SyncGlobalNumberVariablesPayload(KNumberVariables variables) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SyncGlobalNumberVariablesPayload> TYPE = SimplePayloadType.vidlib("sync_global_number_variables", KNumberVariables.STREAM_CODEC.map(SyncGlobalNumberVariablesPayload::new, SyncGlobalNumberVariablesPayload::variables));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().getEnvironment().globalVariables().replace(variables);
	}
}
