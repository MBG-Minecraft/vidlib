package dev.mrbeastgaming.mods.hub.event;

import dev.mrbeastgaming.mods.hub.file.HubFileUploads;
import net.minecraft.server.MinecraftServer;

public class SyncServerFilesHubEvent extends SyncFilesHubEvent {
	private final MinecraftServer server;

	public SyncServerFilesHubEvent(HubFileUploads uploads, MinecraftServer server) {
		super(uploads);
		this.server = server;
	}

	public MinecraftServer getServer() {
		return server;
	}
}
