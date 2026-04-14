package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ClientHubFileUploads {
	static ProgressQueue createQueue() {
		var queue = new ProgressQueue("Uploading Files...");
		queue.bottomText = "Please keep the game open!";
		queue.hideInGame = true;
		return queue;
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncDirectory(Path directory, HubFileUploadBuilder upload) {
		return CompletableFuture.supplyAsync(() -> syncDirectory(directory, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncDirectory(Path directory, HubFileUploadBuilder upload) {
		return HubFileUploads.syncDirectory(directory, upload.minecraftId(Minecraft.getInstance().getUser().getProfileId()).progressQueue(createQueue()));
	}

	static CompletableFuture<List<HubFileUploads.SyncedFile>> asyncFile(Path file, HubFileUploadBuilder upload) {
		return CompletableFuture.supplyAsync(() -> syncFile(file, upload), HubAPI.SEQUENTIAL_EXECUTOR.get());
	}

	static List<HubFileUploads.SyncedFile> syncFile(Path file, HubFileUploadBuilder upload) {
		return HubFileUploads.syncFile(file, upload.minecraftId(Minecraft.getInstance().getUser().getProfileId()).progressQueue(createQueue()));
	}
}
