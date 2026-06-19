package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import net.minecraft.server.permissions.Permissions;

public record PreviewCutscenePayload(Cutscene cutscene, KNumberVariables variables) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<PreviewCutscenePayload> TYPE = VidLibPacketType.internal("preview_cutscene", CompositeStreamCodec.of(
		Cutscene.DATA_TYPE.streamCodec(), PreviewCutscenePayload::cutscene,
		KNumberVariables.STREAM_CODEC, PreviewCutscenePayload::variables,
		PreviewCutscenePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			ctx.level().playCutscene(cutscene, variables);
		}
	}
}
