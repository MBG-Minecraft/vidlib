package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.server.level.ServerPlayer;

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
		CommonGameEngine.INSTANCE.handleClientModList((ServerPlayer) ctx.player(), modList);
	}
}
