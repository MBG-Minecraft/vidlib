package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ServerHubFileUploads {
	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncDirectory(Path directory, Consumer<HubDirectoryUploadBuilder> upload) {
		return CompletableFuture.supplyAsync(() -> syncDirectory(directory, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncDirectory(Path directory, Consumer<HubDirectoryUploadBuilder> upload) {
		return HubFileUploads.syncFiles(HubFileUploads.prepareDirectory(directory, upload), null);
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncFile(Path file, BiConsumer<FileInfo, HubFileUploadBuilder> upload) {
		return CompletableFuture.supplyAsync(() -> syncFile(file, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncFile(Path file, BiConsumer<FileInfo, HubFileUploadBuilder> upload) {
		return HubFileUploads.syncFiles(HubFileUploads.prepareFile(file, upload), null);
	}
}
