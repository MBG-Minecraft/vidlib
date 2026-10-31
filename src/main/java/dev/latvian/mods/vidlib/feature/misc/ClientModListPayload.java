package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.platform.PlatformModInfo;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ClientModListPayload(List<PlatformModInfo> modList) implements SimplePayload {
	@AutoPacket(stage = AutoPacket.Stage.COMMON, to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<ClientModListPayload> TYPE = SimplePayloadType.vidlib("client_mod_list", KLibStreamCodecs.listOf(PlatformModInfo.STREAM_CODEC).map(ClientModListPayload::new, ClientModListPayload::modList));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var mods = new LinkedHashMap<String, PlatformModInfo>();

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

		if (ctx.isConfig()) {
			ctx.finishTask(ModListRequestPayload.CONFIG_TASK.type());
		}
	}
}
