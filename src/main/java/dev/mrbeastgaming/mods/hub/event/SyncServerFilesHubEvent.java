package dev.mrbeastgaming.mods.hub.event;

import dev.mrbeastgaming.mods.hub.file.HubFileUploads;

import java.util.List;

public abstract class SyncServerFilesHubEvent extends SyncFilesHubEvent {
	public SyncServerFilesHubEvent(List<HubFileUploads.Entry> entries) {
		super(entries);
	}
}
