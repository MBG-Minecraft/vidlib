package dev.latvian.mods.vidlib.feature.clock;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.registry.ID;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record SyncClocksPayload(Map<ResourceLocation, ClockValue> map) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncClocksPayload> TYPE = VidLibPacketType.internal("sync_clocks", ID.STREAM_CODEC.unboundedMap(ClockValue.STREAM_CODEC).map(SyncClocksPayload::new, SyncClocksPayload::map));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().updateClocks(map);
	}
}
