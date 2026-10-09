package dev.mrbeastgaming.mods.hub.event;

import dev.latvian.mods.klib.io.IOConsumer;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;

public abstract class SyncFilesHubEvent extends HubEvent {
	private final HubFileUploads uploads;

	public SyncFilesHubEvent(HubFileUploads uploads) {
		this.uploads = uploads;
	}

	public void add(IOConsumer<HubFileUploads> uploads) {
		try {
			uploads.accept(this.uploads);
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to add upload files", ex);
		}
	}
}
