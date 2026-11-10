package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.StreamCodec;

public enum StopCutscenePayload implements SimplePayload {
	INSTANCE;

	@AutoPacket
	public static final SimplePayloadType<StopCutscenePayload> TYPE = SimplePayloadType.vidlib("cutscene/stop", StreamCodec.unit(INSTANCE));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().stopCutscene();
	}
}
