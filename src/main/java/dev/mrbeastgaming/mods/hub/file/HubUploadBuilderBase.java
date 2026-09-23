package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.mrbeastgaming.mods.hub.api.data.HubFileType;
import dev.mrbeastgaming.mods.hub.api.data.HubPossibleUser;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public abstract class HubUploadBuilderBase {
	FileNameProvider fileNameProvider = null;
	FileTypeProvider type = null;
	UniqueIdProvider uniqueIdProvider = null;
	FileCreationDateProvider creationDateProvider = null;
	HubPossibleUser assignedTo = HubPossibleUser.NONE;
	String customName = "";

	public void setFileNameProvider(FileNameProvider provider) {
		this.fileNameProvider = provider;
	}

	public void setType(FileTypeProvider provider) {
		this.type = provider;

		if (provider instanceof HubFileType t) {
			this.customName = t.name();
		}
	}

	public void setUniqueId(UniqueIdProvider provider) {
		this.uniqueIdProvider = provider;
	}

	public void setNoUniqueId() {
		this.uniqueIdProvider = UniqueIdProvider.NIL;
	}

	public void setCreationDate(FileCreationDateProvider provider) {
		this.creationDateProvider = provider;
	}

	public void setAssignedTo(HubPossibleUser assignedTo) {
		this.assignedTo = assignedTo;
	}

	public void setAssignedToMinecraft(UUID id) {
		setAssignedTo(HubPossibleUser.of(id));
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	HubFileType getFileType(FileInfo fileInfo) throws Exception {
		return type == null ? FileTypeProvider.probe(fileInfo) : type.getFileType(fileInfo);
	}

	@Nullable
	Checksum getUniqueId(FileInfo fileInfo, UploadContext ctx) throws Exception {
		return uniqueIdProvider == null ? NoChecksum.INSTANCE : uniqueIdProvider.getUniqueId(fileInfo, ctx);
	}

	@Nullable
	Instant getFileCreated(FileInfo fileInfo) throws Exception {
		return creationDateProvider == null ? null : creationDateProvider.getFileCreated(fileInfo);
	}
}
