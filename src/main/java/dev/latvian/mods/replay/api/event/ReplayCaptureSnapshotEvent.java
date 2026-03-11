package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;

public class ReplayCaptureSnapshotEvent extends ReplayCaptureEvent {
	public ReplayCaptureSnapshotEvent(ReplayAPI api, ReplayCaptureSession session) {
		super(api, session);
	}
}
