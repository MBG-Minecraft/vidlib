package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.core.VLS2CConfigPacketConsumer;

public class ReplayCaptureConfigSnapshotEvent extends ReplayCaptureSnapshotEvent {
	private final VLS2CConfigPacketConsumer packets;

	public ReplayCaptureConfigSnapshotEvent(ReplayAPI api, ReplayCaptureSession session, VLS2CConfigPacketConsumer packets) {
		super(api, session);
		this.packets = packets;
	}

	public VLS2CConfigPacketConsumer getPackets() {
		return packets;
	}
}
