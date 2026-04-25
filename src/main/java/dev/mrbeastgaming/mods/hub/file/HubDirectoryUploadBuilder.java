package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.FileInfoFilter;
import dev.latvian.mods.klib.util.MD5;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.HubFileType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class HubDirectoryUploadBuilder extends HubUploadBuilderBase {
	FileInfoFilter filter = null;
	FileNameProvider fileNameProvider = null;
	private FileTypeProvider type = null;
	private UniqueIdProvider uniqueIdProvider = null;
	private FileCreationDateProvider creationDateProvider = null;

	public void setFilter(FileInfoFilter filter) {
		this.filter = filter;
	}

	public void setFilterEndsWith(String suffix) {
		setFilter(fileInfo -> fileInfo.name().endsWith(suffix));
	}

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

	@Override
	HubFileType getFileType(FileInfo fileInfo) throws Exception {
		return type == null ? FileTypeProvider.probe(fileInfo) : type.getFileType(fileInfo);
	}

	@Override
	@Nullable
	MD5 getUniqueId(FileInfo fileInfo, HubProjectConfig projectConfig) throws Exception {
		if (uniqueIdProvider == null && assignedToMinecraft != null) {
			return UniqueIdProvider.ofUUIDAndFileName(assignedToMinecraft).getUniqueId(fileInfo, projectConfig);
		}

		return uniqueIdProvider == null ? MD5.NIL : uniqueIdProvider.getUniqueId(fileInfo, projectConfig);
	}

	@Override
	@Nullable
	Instant getFileCreated(FileInfo fileInfo) throws Exception {
		return creationDateProvider == null ? null : creationDateProvider.getFileCreated(fileInfo);
	}
}
