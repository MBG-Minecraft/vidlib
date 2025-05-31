package dev.latvian.mods.vidlib.feature.highlight;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record TerrainHighlightPayload(TerrainHighlight highlight) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<TerrainHighlightPayload> TYPE = VidLibPacketType.internal("terrain_highlight", TerrainHighlight.STREAM_CODEC.map(TerrainHighlightPayload::new, TerrainHighlightPayload::highlight));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().addTerrainHighlight(highlight);
	}
}
