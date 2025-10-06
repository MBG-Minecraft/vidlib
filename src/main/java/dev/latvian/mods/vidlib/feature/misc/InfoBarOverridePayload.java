package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;

public record InfoBarOverridePayload(int bar, Component text) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<InfoBarOverridePayload> TYPE = VidLibPacketType.internal("info_bar/override", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, InfoBarOverridePayload::bar,
		ComponentSerialization.TRUSTED_STREAM_CODEC, InfoBarOverridePayload::text,
		InfoBarOverridePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().setInfoBarText(bar, text);
	}
}
