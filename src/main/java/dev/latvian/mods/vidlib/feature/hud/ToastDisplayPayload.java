package dev.latvian.mods.vidlib.feature.hud;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.concurrent.TimeUnit;

public record ToastDisplayPayload(String id, long displayTime, Component title, Component description) implements SimplePacketPayload {
	private static final Cache<String, SystemToast.SystemToastId> SYSTEM_TOAST_IDS = CacheBuilder.newBuilder()
		.expireAfterWrite(5, TimeUnit.MINUTES)
		.build();

	@AutoPacket
	public static final VidLibPacketType<ToastDisplayPayload> TYPE = VidLibPacketType.video("toast/display", CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, ToastDisplayPayload::id,
		ByteBufCodecs.VAR_LONG, ToastDisplayPayload::displayTime,
		ComponentSerialization.STREAM_CODEC, ToastDisplayPayload::title,
		ComponentSerialization.STREAM_CODEC, ToastDisplayPayload::description,
		ToastDisplayPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		SystemToast.SystemToastId id;
		try {
			id = SYSTEM_TOAST_IDS.get(this.id, () -> new SystemToast.SystemToastId(displayTime));
		} catch (Exception e) {
			id = new SystemToast.SystemToastId(displayTime);
		}
		SystemToast.addOrUpdate(
			Minecraft.getInstance().getToastManager(),
			id,
			title,
			description
		);
	}
}
