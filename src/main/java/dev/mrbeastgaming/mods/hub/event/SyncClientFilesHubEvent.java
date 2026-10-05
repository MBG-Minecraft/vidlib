package dev.mrbeastgaming.mods.hub.event;

import dev.mrbeastgaming.mods.hub.file.HubFileUploads;

public class SyncClientFilesHubEvent extends SyncFilesHubEvent {
	private final boolean firstTime;

	public SyncClientFilesHubEvent(HubFileUploads uploads, boolean firstTime) {
		super(uploads);
		this.firstTime = firstTime;
	}

	public boolean isFirstTime() {
		return firstTime;
	}
}
