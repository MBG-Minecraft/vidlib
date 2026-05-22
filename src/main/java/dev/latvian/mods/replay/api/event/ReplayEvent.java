package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import net.neoforged.bus.api.Event;

public abstract class ReplayEvent extends Event {
	private final ReplayAPI api;

	public ReplayEvent(ReplayAPI api) {
		this.api = api;
	}

	public ReplayAPI getApi() {
		return api;
	}
}
