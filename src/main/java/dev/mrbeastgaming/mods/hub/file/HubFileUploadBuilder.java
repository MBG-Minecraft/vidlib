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
	FileNameProvider fileNameProvider = null;

	public void setFilter(FileInfoFilter filter) {
		this.filter = filter;
	}

	public void setFilterEndsWith(String suffix) {
		setFilter(fileInfo -> fileInfo.name().endsWith(suffix));
	}

	public void setType(FileTypeProvider provider) {
		this.type = provider;
	}

	public void setUniqueId(UniqueIdProvider provider) {
		this.uniqueIdProvider = provider;
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

	public void setFileNameProvider(FileNameProvider provider) {
		this.fileNameProvider = provider;
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
