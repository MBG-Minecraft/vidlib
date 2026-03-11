package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;

public abstract class ReplaySessionEvent extends ReplayEvent {
	private final ReplaySession session;

	public ReplaySessionEvent(ReplayAPI api, ReplaySession session) {
		super(api);
		this.session = session;
	}

	public ReplaySession getSession() {
		return session;
	}
}
