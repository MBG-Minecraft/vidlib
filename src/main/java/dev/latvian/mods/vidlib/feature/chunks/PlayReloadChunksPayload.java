package dev.latvian.mods.vidlib.feature.chunks;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;

public record PlayReloadChunksPayload() implements SimplePacketPayload {

	public static final PlayReloadChunksPayload INSTANCE = new PlayReloadChunksPayload();

	@AutoPacket
	public static final VidLibPacketType<PlayReloadChunksPayload> TYPE = VidLibPacketType.internal("reload_chunks", StreamCodec.unit(INSTANCE));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		Minecraft.getInstance().levelRenderer.allChanged();
	}
}
