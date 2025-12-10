package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;

import java.util.List;

public record ClientModListPayload(List<ClientModInfo> modList) implements SimplePacketPayload {
	@AutoPacket(stage = AutoPacket.Stage.CONFIG, to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<ClientModListPayload> TYPE = VidLibPacketType.internal("client_mod_list", KLibStreamCodecs.listOf(ClientModInfo.STREAM_CODEC).map(ClientModListPayload::new, ClientModListPayload::modList));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		CommonGameEngine.INSTANCE.handleClientModList(ctx, modList);
		ctx.finishTask(ModListRequestPayload.CONFIG_TASK.type());
	}
}
