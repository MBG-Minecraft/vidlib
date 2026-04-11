package dev.mrbeastgaming.mods.hub;

import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface ClientHubFileUploads {
	static ProgressQueue createQueue() {
		var queue = new ProgressQueue("Uploading Files...");
		queue.bottomText = "Please keep the game open!";
		queue.hideInGame = true;
		return queue;
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncDirectory(Path directory, Predicate<Path> filter, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return CompletableFuture.supplyAsync(() -> syncDirectory(directory, filter, contentType, uniqueIdProvider), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncDirectory(Path directory, Predicate<Path> filter, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return HubFileUploads.syncDirectory(directory, filter, contentType, uniqueIdProvider, Minecraft.getInstance().getUser().getProfileId(), createQueue());
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncFile(Path file, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return CompletableFuture.supplyAsync(() -> syncFile(file, contentType, uniqueIdProvider), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncFile(Path file, @Nullable FileTypeProvider contentType, @Nullable UniqueIdProvider uniqueIdProvider) {
		return HubFileUploads.syncFile(file, contentType, uniqueIdProvider, Minecraft.getInstance().getUser().getProfileId(), createQueue());
	}
}
