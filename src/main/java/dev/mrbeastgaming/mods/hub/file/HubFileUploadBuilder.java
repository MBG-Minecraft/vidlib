package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.FileInfoFilter;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;

import java.util.UUID;

public class HubFileUploadBuilder {
	FileInfoFilter filter = null;
	FileTypeProvider type = null;
	UniqueIdProvider uniqueIdProvider = null;
	Hex32 assignedTo = Hex32.NONE;
	UUID assignedToMinecraft = null;
	ProgressQueue progressQueue = null;

	public HubFileUploadBuilder filter(FileInfoFilter filter) {
		this.filter = filter;
		return this;
	}

	public HubFileUploadBuilder filterEndsWith(String suffix) {
		return filter(fileInfo -> fileInfo.name().endsWith(suffix));
	}

	public HubFileUploadBuilder type(FileTypeProvider fileType) {
		this.type = fileType;
		return this;
	}

	public HubFileUploadBuilder uniqueId(UniqueIdProvider uniqueIdProvider) {
		this.uniqueIdProvider = uniqueIdProvider;
		return this;
	}

	public HubFileUploadBuilder assignedTo(Hex32 assignedTo) {
		this.assignedTo = assignedTo;
		return this;
	}

	public HubFileUploadBuilder assignedToMinecraft(UUID id) {
		this.assignedToMinecraft = id;
		return this;
	}

	public HubFileUploadBuilder progressQueue(ProgressQueue progressQueue) {
		this.progressQueue = progressQueue;
		return this;
	}

	boolean testFilter(FileInfo fileInfo) {
		if (filter == null) {
			return true;
		}

		try {
			return filter.test(fileInfo);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
