package dev.mrbeastgaming.mods.hub;

import dev.mrbeastgaming.mods.hub.api.HubAPI;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface ServerHubFileUploads {
	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncDirectory(Path directory, Predicate<Path> filter, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return CompletableFuture.supplyAsync(() -> syncDirectory(directory, filter, contentType, uniqueIdProvider), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncDirectory(Path directory, Predicate<Path> filter, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return HubFileUploads.syncDirectory(directory, filter, contentType, uniqueIdProvider, null, null);
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncFile(Path file, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return CompletableFuture.supplyAsync(() -> syncFile(file, contentType, uniqueIdProvider), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncFile(Path file, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return HubFileUploads.syncFile(file, contentType, uniqueIdProvider, null, null);
	}
}
