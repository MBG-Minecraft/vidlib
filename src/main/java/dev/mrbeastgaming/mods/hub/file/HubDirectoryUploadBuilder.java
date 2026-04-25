package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.FileInfoFilter;

public class HubDirectoryUploadBuilder extends HubUploadBuilderBase {
	FileInfoFilter filter = null;

	public void setFilter(FileInfoFilter filter) {
		this.filter = filter;
	}

	public void setFilterEndsWith(String suffix) {
		setFilter(fileInfo -> fileInfo.name().endsWith(suffix));
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
