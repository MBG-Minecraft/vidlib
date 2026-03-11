package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;

public class ReplayCaptureGameSnapshotEvent extends ReplayCaptureSnapshotEvent {
	private final VLS2CPacketConsumer packets;

	public ReplayCaptureGameSnapshotEvent(ReplayAPI api, ReplayCaptureSession session, VLS2CPacketConsumer packets) {
		super(api, session);
		this.packets = packets;
	}

	public VLS2CPacketConsumer getPackets() {
		return packets;
	}
}
