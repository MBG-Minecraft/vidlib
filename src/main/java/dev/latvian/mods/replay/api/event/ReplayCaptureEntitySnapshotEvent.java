package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import net.minecraft.world.entity.Entity;

public class ReplayCaptureEntitySnapshotEvent extends ReplayCaptureSnapshotEvent {
	private final VLS2CPacketConsumer packets;
	private final Entity entity;

	public ReplayCaptureEntitySnapshotEvent(ReplayAPI api, ReplayCaptureSession session, VLS2CPacketConsumer packets, Entity entity) {
		super(api, session);
		this.packets = packets;
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	public VLS2CPacketConsumer getPackets() {
		return packets;
	}
}
