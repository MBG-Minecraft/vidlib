package dev.mrbeastgaming.mods.hub.event;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.vidlib.VidLibClient;
import dev.mrbeastgaming.mods.hub.file.HubDirectoryUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SyncClientFilesHubEvent extends SyncFilesHubEvent {
	private final boolean firstTime;

	public SyncClientFilesHubEvent(List<HubFileUploads.Entry> entries, boolean firstTime) {
		super(entries);
		this.firstTime = firstTime;
	}

	public boolean isFirstTime() {
		return firstTime;
	}

	@Override
	public void addDirectory(Path directory, Consumer<HubDirectoryUploadBuilder> upload) {
		super.addDirectory(directory, VidLibClient.wrapHubDirectoryUploadBuilder(upload));
	}

	@Override
	public void addFile(Path file, BiConsumer<FileInfo, HubFileUploadBuilder> upload) {
		super.addFile(file, VidLibClient.wrapHubFileUploadBuilder(upload));
	}
}
