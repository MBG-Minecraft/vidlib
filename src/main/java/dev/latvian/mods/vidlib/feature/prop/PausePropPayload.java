package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record PausePropPayload(PropListType type, int id, boolean paused) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<PausePropPayload> TYPE = VidLibPacketType.internal("prop/pause", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, PausePropPayload::type,
		ByteBufCodecs.VAR_INT, PausePropPayload::id,
		ByteBufCodecs.BOOL, PausePropPayload::paused,
		PausePropPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var prop = ctx.level().getProps().propLists.get(type).get(id);

		if (prop != null) {
			prop.paused = paused;
		}
	}
}
