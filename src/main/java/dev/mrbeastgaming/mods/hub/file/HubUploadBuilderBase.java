package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.MD5;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.HubFileType;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

public abstract class HubUploadBuilderBase {
	FileNameProvider fileNameProvider = null;
	FileTypeProvider type = null;
	UniqueIdProvider uniqueIdProvider = null;
	FileCreationDateProvider creationDateProvider = null;
	Hex32 assignedTo = Hex32.NONE;
	UUID assignedToMinecraft = null;
	ProgressQueue progressQueue = null;

	public void setFileNameProvider(FileNameProvider provider) {
		this.fileNameProvider = provider;
	}

	public void setType(FileTypeProvider provider) {
		this.type = provider;
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

	public void setAssignedTo(Hex32 assignedTo) {
		this.assignedTo = assignedTo;
	}

	public void setAssignedToMinecraft(UUID id) {
		this.assignedToMinecraft = id;
	}

	public void setProgressQueue(ProgressQueue queue) {
		this.progressQueue = queue;
	}

	HubFileType getFileType(FileInfo fileInfo) throws Exception {
		return type == null ? FileTypeProvider.probe(fileInfo) : type.getFileType(fileInfo);
	}

	@Nullable
	MD5 getUniqueId(FileInfo fileInfo, HubProjectConfig projectConfig) throws Exception {
		if (uniqueIdProvider == null && assignedToMinecraft != null) {
			try (var bytes = new ByteArrayOutputStream();
				 var data = new DataOutputStream(bytes)
			) {
				data.writeLong(assignedToMinecraft.getMostSignificantBits());
				data.writeLong(assignedToMinecraft.getLeastSignificantBits());
				IOUtils.writeUTF(data, fileInfo.name());
				data.writeInt(projectConfig.projectId().raw());
				return MD5.fromBytes(MessageDigest.getInstance("MD5").digest(bytes.toByteArray()));
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to create a unique ID of " + fileInfo.path(), ex);
				return null;
			}
		}

		return uniqueIdProvider == null ? MD5.NIL : uniqueIdProvider.getUniqueId(fileInfo, projectConfig);
	}

	@Nullable
	Instant getFileCreated(FileInfo fileInfo) throws Exception {
		return creationDateProvider == null ? null : creationDateProvider.getFileCreated(fileInfo);
	}
}
