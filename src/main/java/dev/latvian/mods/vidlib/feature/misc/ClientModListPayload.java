package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;

public record ClientModListPayload(List<ClientModInfo> modList) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<ClientModListPayload> TYPE = VidLibPacketType.internal("client_mod_list", CompositeStreamCodec.of(
		KLibStreamCodecs.listOf(ClientModInfo.STREAM_CODEC), ClientModListPayload::modList,
		ClientModListPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (VidLibConfig.logClientModList && ctx.level().getServer().isDedicatedServer()) {
			VidLib.LOGGER.info("Player " + ctx.player().getScoreboardName() + " logged in with mods:");

			for (var info : modList) {
				VidLib.LOGGER.info(" - " + info.name() + " (" + info.name() + " / " + info.fileName() + "), " + info.version());
			}
		}
	}
}
