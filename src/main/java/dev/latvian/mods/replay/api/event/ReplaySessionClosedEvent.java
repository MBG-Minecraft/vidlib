package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;

public class ReplaySessionClosedEvent extends ReplaySessionEvent {
	public ReplaySessionClosedEvent(ReplayAPI api, ReplaySession session) {
		super(api, session);
	}
}
