package dev.latvian.mods.vidlib.feature.chunks;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;

public record PlayReloadChunksPayload() implements SimplePayload {

	public static final PlayReloadChunksPayload INSTANCE = new PlayReloadChunksPayload();

	@AutoPacket
	public static final SimplePayloadType<PlayReloadChunksPayload> TYPE = SimplePayloadType.vidlib("reload_chunks", StreamCodec.unit(INSTANCE));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		Minecraft.getInstance().levelExtractor.allChanged();
	}
}
