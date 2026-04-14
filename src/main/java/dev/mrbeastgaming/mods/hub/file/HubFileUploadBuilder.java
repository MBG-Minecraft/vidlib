package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;

import java.util.UUID;

public class HubFileUploadBuilder {
	HubFileUploadFilter filter = null;
	FileTypeProvider type = null;
	UniqueIdProvider uniqueIdProvider = null;
	UUID minecraftId = null;
	ProgressQueue progressQueue = null;

	public HubFileUploadBuilder filter(HubFileUploadFilter filter) {
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

	public HubFileUploadBuilder minecraftId(UUID minecraftId) {
		this.minecraftId = minecraftId;
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
			return filter.upload(fileInfo);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
