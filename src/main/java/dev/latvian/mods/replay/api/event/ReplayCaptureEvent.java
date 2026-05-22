package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;

public abstract class ReplayCaptureEvent extends ReplayEvent {
	private final ReplayCaptureSession session;

	public ReplayCaptureEvent(ReplayAPI api, ReplayCaptureSession session) {
		super(api);
		this.session = session;
	}

	public ReplayCaptureSession getSession() {
		return session;
	}
}
