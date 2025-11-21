package dev.latvian.mods.vidlib.feature.waypoint;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;

public record RemoveWaypointsPayload(List<String> ids) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemoveWaypointsPayload> TYPE = VidLibPacketType.internal("waypoint/remove", CompositeStreamCodec.of(
		KLibStreamCodecs.listOf(ByteBufCodecs.STRING_UTF8), RemoveWaypointsPayload::ids,
		RemoveWaypointsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().removeWaypoints(ids);
	}
}
