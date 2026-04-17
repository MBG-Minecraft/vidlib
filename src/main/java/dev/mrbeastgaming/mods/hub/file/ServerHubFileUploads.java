package dev.mrbeastgaming.mods.hub.file;

import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ServerHubFileUploads {
	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncDirectory(Path directory, Consumer<HubFileUploadBuilder> upload) {
		return CompletableFuture.supplyAsync(() -> syncDirectory(directory, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncDirectory(Path directory, Consumer<HubFileUploadBuilder> upload) {
		return HubFileUploads.syncDirectory(directory, upload);
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncFile(Path file, Consumer<HubFileUploadBuilder> upload) {
		return CompletableFuture.supplyAsync(() -> syncFile(file, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncFile(Path file, Consumer<HubFileUploadBuilder> upload) {
		return HubFileUploads.syncFile(file, upload);
	}
}
