package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.replay.api.ReplayAPI;
import net.minecraft.world.entity.Entity;

public class ReplayCaptureEntitySnapshotEvent extends ReplayCaptureSnapshotEvent {
	private final S2CPacketConsumer packets;
	private final Entity entity;

	public ReplayCaptureEntitySnapshotEvent(ReplayAPI api, ReplayCaptureSession session, S2CPacketConsumer packets, Entity entity) {
		super(api, session);
		this.packets = packets;
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	public S2CPacketConsumer getPackets() {
		return packets;
	}
}
