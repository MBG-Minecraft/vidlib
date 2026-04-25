package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.MD5;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.HubFileType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public abstract class HubUploadBuilderBase {
	Hex32 assignedTo = Hex32.NONE;
	UUID assignedToMinecraft = null;
	ProgressQueue progressQueue = null;

	public void setAssignedTo(Hex32 assignedTo) {
		this.assignedTo = assignedTo;
	}

	public void setAssignedToMinecraft(UUID id) {
		this.assignedToMinecraft = id;
	}

	public void setProgressQueue(ProgressQueue queue) {
		this.progressQueue = queue;
	}

	abstract HubFileType getFileType(FileInfo fileInfo) throws Exception;

	@Nullable
	abstract MD5 getUniqueId(FileInfo fileInfo, HubProjectConfig projectConfig) throws Exception;

	@Nullable
	abstract Instant getFileCreated(FileInfo fileInfo) throws Exception;
}
