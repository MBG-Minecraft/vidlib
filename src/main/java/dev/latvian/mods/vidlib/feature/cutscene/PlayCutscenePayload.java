package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record PlayCutscenePayload(Ref<Cutscene> cutscene, KNumberVariables variables) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<PlayCutscenePayload> TYPE = SimplePayloadType.vidlib("cutscene/play", CompositeStreamCodec.of(
		Cutscene.DATA_TYPE.streamCodec(), PlayCutscenePayload::cutscene,
		KNumberVariables.STREAM_CODEC, PlayCutscenePayload::variables,
		PlayCutscenePayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().playCutscene(cutscene, variables);
	}
}
