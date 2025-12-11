package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ClientModListPayload(List<ClientModInfo> modList) implements SimplePacketPayload {
	@AutoPacket(stage = AutoPacket.Stage.CONFIG, to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<ClientModListPayload> TYPE = VidLibPacketType.internal("client_mod_list", KLibStreamCodecs.listOf(ClientModInfo.STREAM_CODEC).map(ClientModListPayload::new, ClientModListPayload::modList));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var mods = new LinkedHashMap<String, ClientModInfo>();

		for (var mod : modList) {
			mods.put(mod.id(), mod);
		}

		var map = Map.copyOf(mods);

		if (!CommonGameEngine.INSTANCE.handleClientModList(ctx, map)) {
			return;
		}

		if (ctx.sessionData() instanceof ServerSessionData sessionData) {
			sessionData.clientMods = map;
		}

		ctx.finishTask(ModListRequestPayload.CONFIG_TASK.type());
	}
}
