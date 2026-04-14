package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.mrbeastgaming.mods.hub.api.HubFileType;

import java.nio.file.Files;

public interface FileTypeProvider {
	static HubFileType probe(FileInfo fileInfo) {
		try {
			var type = Files.probeContentType(fileInfo.path());
			return type == null || type.isEmpty() ? HubFileType.UNKNOWN : HubFileType.custom(type);
		} catch (Exception ignored) {
			return HubFileType.UNKNOWN;
		}
	}

	HubFileType getFileType(FileInfo fileInfo) throws Exception;
}
