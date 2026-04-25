package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.util.MD5;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.HubFileType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class HubFileUploadBuilder extends HubUploadBuilderBase {
	HubFileType type = null;
	MD5 uniqueId = null;
	String fileName = null;
	Instant creationDate = null;

	public void setType(HubFileType provider) {
		this.type = provider;
	}

	public void setUniqueId(MD5 id) {
		this.uniqueId = id;
	}

	public void setNoUniqueId() {
		this.uniqueId = MD5.NIL;
	}

	public void setFileName(String name) {
		this.fileName = name;
	}

	public void setCreationDate(Instant instant) {
		this.creationDate = instant;
	}

	@Override
	HubFileType getFileType(FileInfo fileInfo) {
		return type == null ? HubFileType.UNKNOWN : type;
	}

	@Override
	@Nullable
	MD5 getUniqueId(FileInfo fileInfo, HubProjectConfig projectConfig) throws Exception {
		if (uniqueId == null && assignedToMinecraft != null) {
			return UniqueIdProvider.ofUUIDAndFileName(assignedToMinecraft).getUniqueId(fileInfo, projectConfig);
		}

		return uniqueId;
	}

	@Override
	@Nullable
	Instant getFileCreated(FileInfo fileInfo) {
		return creationDate;
	}
}
