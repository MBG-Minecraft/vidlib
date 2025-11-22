package dev.latvian.mods.vidlib.feature.waypoint;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;

public record AddWaypointsPayload(List<Waypoint> waypoints) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<AddWaypointsPayload> TYPE = VidLibPacketType.internal("waypoint/add", CompositeStreamCodec.of(
		KLibStreamCodecs.listOf(Waypoint.STREAM_CODEC), AddWaypointsPayload::waypoints,
		AddWaypointsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().addWaypoints(waypoints);
	}
}
