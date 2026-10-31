package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.klib.net.S2CConfigPacketConsumer;
import dev.latvian.mods.replay.api.ReplayAPI;

public class ReplayCaptureConfigSnapshotEvent extends ReplayCaptureSnapshotEvent {
	private final S2CConfigPacketConsumer packets;

	public ReplayCaptureConfigSnapshotEvent(ReplayAPI api, ReplayCaptureSession session, S2CConfigPacketConsumer packets) {
		super(api, session);
		this.packets = packets;
	}

	public S2CConfigPacketConsumer getPackets() {
		return packets;
	}
}
