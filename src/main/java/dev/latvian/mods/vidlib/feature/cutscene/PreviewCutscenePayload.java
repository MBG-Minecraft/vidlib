package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;

public record PreviewCutscenePayload(Cutscene cutscene, WorldNumberVariables variables) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<PreviewCutscenePayload> TYPE = VidLibPacketType.internal("preview_cutscene", CompositeStreamCodec.of(
		Cutscene.DATA_TYPE.streamCodec(), PreviewCutscenePayload::cutscene,
		WorldNumberVariables.STREAM_CODEC, PreviewCutscenePayload::variables,
		PreviewCutscenePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.player().hasPermissions(2)) {
			ctx.level().playCutscene(cutscene, variables);
		}
	}
}
