package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;

public record PlayCutscenePayload(Cutscene cutscene, WorldNumberVariables variables) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<PlayCutscenePayload> TYPE = VidLibPacketType.internal("play_cutscene", CompositeStreamCodec.of(
		Cutscene.STREAM_CODEC, PlayCutscenePayload::cutscene,
		WorldNumberVariables.STREAM_CODEC, PlayCutscenePayload::variables,
		PlayCutscenePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().playCutscene(cutscene, variables);
	}
}
