package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.UUID;

@Deprecated
public record RefreshNamePayload(UUID player, Component nickname) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<RefreshNamePayload> TYPE = SimplePayloadType.vidlib("refresh_name", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, RefreshNamePayload::player,
		ComponentSerialization.TRUSTED_STREAM_CODEC, RefreshNamePayload::nickname,
		RefreshNamePayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var p = ctx.level().getPlayerByUUID(player);

		if (p != null) {
			p.setNickname(nickname);
			p.refreshDisplayName();
		}
	}
}
