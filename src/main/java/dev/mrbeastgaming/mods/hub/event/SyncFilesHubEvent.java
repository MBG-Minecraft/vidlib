package dev.mrbeastgaming.mods.hub.event;

import dev.latvian.mods.klib.io.FileInfo;
import dev.mrbeastgaming.mods.hub.file.HubDirectoryUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploadBuilder;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class SyncFilesHubEvent extends HubEvent {
	private final List<HubFileUploads.Entry> entries;

	public SyncFilesHubEvent(List<HubFileUploads.Entry> entries) {
		this.entries = entries;
	}

	public void addDirectory(Path directory, Consumer<HubDirectoryUploadBuilder> upload) {
		this.entries.addAll(HubFileUploads.prepareDirectory(directory, upload));
	}

	public void addFile(Path file, BiConsumer<FileInfo, HubFileUploadBuilder> upload) {
		this.entries.addAll(HubFileUploads.prepareFile(file, upload));
	}
}
