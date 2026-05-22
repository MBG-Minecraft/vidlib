package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySessionData;

import java.util.function.Consumer;

public class RegisterReplaySessionDataEvent extends ReplayEvent {
	private final Consumer<ReplaySessionData> callback;

	public RegisterReplaySessionDataEvent(ReplayAPI api, Consumer<ReplaySessionData> callback) {
		super(api);
		this.callback = callback;
	}

	public void register(ReplaySessionData data) {
		this.callback.accept(data);
	}
}
