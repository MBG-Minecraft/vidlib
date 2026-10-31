package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.replay.api.ReplayAPI;

public class ReplayCaptureGameSnapshotEvent extends ReplayCaptureSnapshotEvent {
	private final S2CPacketConsumer packets;

	public ReplayCaptureGameSnapshotEvent(ReplayAPI api, ReplayCaptureSession session, S2CPacketConsumer packets) {
		super(api, session);
		this.packets = packets;
	}

	public S2CPacketConsumer getPackets() {
		return packets;
	}
}
