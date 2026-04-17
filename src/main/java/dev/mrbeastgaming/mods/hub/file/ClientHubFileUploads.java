package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.vidlib.VidLibClient;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubUserCapabilities;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ClientHubFileUploads {
	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncDirectory(Path directory, Consumer<HubFileUploadBuilder> upload) {
		return CompletableFuture.supplyAsync(() -> syncDirectory(directory, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncDirectory(Path directory, Consumer<HubFileUploadBuilder> upload) {
		if (!HubUserCapabilities.CURRENT.autoUploadFiles()) {
			return List.of();
		}

		return HubFileUploads.syncDirectory(directory, VidLibClient.wrapHubFileUploadBuilder(upload));
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncFile(Path file, Consumer<HubFileUploadBuilder> upload) {
		return CompletableFuture.supplyAsync(() -> syncFile(file, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncFile(Path file, Consumer<HubFileUploadBuilder> upload) {
		if (!HubUserCapabilities.CURRENT.autoUploadFiles()) {
			return List.of();
		}

		return HubFileUploads.syncFile(file, VidLibClient.wrapHubFileUploadBuilder(upload));
	}
}
