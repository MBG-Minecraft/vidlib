package dev.latvian.mods.vidlib.feature.clock;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.Map;

public record SyncClocksPayload(Map<Ref<Clock>, ClockValue> map) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncClocksPayload> TYPE = VidLibPacketType.internal("sync_clocks", KLibStreamCodecs.unboundedMap(Clock.STREAM_CODEC, ClockValue.STREAM_CODEC).map(SyncClocksPayload::new, SyncClocksPayload::map));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().updateClocks(map);
	}
}
