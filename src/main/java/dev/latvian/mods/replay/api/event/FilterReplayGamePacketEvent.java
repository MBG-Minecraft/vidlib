package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplayGamePacket;
import dev.latvian.mods.replay.api.ReplaySession;
import net.neoforged.bus.api.ICancellableEvent;

public class FilterReplayGamePacketEvent extends ReplaySessionEvent implements ICancellableEvent {
	private final ReplayGamePacket packet;

	public FilterReplayGamePacketEvent(ReplayAPI api, ReplaySession session, ReplayGamePacket packet) {
		super(api, session);
		this.packet = packet;
	}

	public ReplayGamePacket getPacket() {
		return packet;
	}
}
