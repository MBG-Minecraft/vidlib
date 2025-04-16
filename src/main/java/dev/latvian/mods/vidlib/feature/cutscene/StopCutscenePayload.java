package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.StreamCodec;

public enum StopCutscenePayload implements SimplePacketPayload {
	INSTANCE;

	@AutoPacket
	public static final VidLibPacketType<StopCutscenePayload> TYPE = VidLibPacketType.internal("stop_cutscene", StreamCodec.unit(INSTANCE));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().stopCutscene();
	}
}
