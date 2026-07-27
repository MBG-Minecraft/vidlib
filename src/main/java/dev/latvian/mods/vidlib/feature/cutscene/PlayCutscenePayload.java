package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record PlayCutscenePayload(Ref<Cutscene> cutscene, KNumberVariables variables) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<PlayCutscenePayload> TYPE = VidLibPacketType.internal("cutscene/play", CompositeStreamCodec.of(
		Cutscene.DATA_TYPE.streamCodec(), PlayCutscenePayload::cutscene,
		KNumberVariables.STREAM_CODEC, PlayCutscenePayload::variables,
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
