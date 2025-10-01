package dev.latvian.mods.vidlib.feature.hud;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;

public record ToastDisplayPayload(String uniqueId, long displayTime, Component title, Component description) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ToastDisplayPayload> TYPE = VidLibPacketType.video("toast/display", CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, ToastDisplayPayload::uniqueId,
		ByteBufCodecs.VAR_LONG, ToastDisplayPayload::displayTime,
		ComponentSerialization.STREAM_CODEC, ToastDisplayPayload::title,
		ComponentSerialization.STREAM_CODEC, ToastDisplayPayload::description,
		ToastDisplayPayload::new
	));

	public ToastDisplayPayload(Component title, Component description) {
		this("", 5000L, title, description);
	}

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().toast(uniqueId, displayTime, title, description);
	}
}
