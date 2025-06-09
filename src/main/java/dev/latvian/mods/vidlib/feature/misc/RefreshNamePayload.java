package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.UUID;

public record RefreshNamePayload(UUID player, Component nickname) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RefreshNamePayload> TYPE = VidLibPacketType.internal("refresh_name", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, RefreshNamePayload::player,
		ComponentSerialization.TRUSTED_STREAM_CODEC, RefreshNamePayload::nickname,
		RefreshNamePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var p = ctx.level().getPlayerByUUID(player);

		if (p != null) {
			p.setNickname(nickname);
			p.refreshDisplayName();
			p.vl$sessionData().refreshListedPlayers();
		}
	}
}
