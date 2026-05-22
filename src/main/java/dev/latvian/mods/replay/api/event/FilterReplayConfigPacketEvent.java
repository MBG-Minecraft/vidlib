package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplayConfigPacket;
import dev.latvian.mods.replay.api.ReplaySession;
import net.neoforged.bus.api.ICancellableEvent;

public class FilterReplayConfigPacketEvent extends ReplaySessionEvent implements ICancellableEvent {
	private final ReplayConfigPacket packet;

	public FilterReplayConfigPacketEvent(ReplayAPI api, ReplaySession session, ReplayConfigPacket packet) {
		super(api, session);
		this.packet = packet;
	}

	public ReplayConfigPacket getPacket() {
		return packet;
	}
}
