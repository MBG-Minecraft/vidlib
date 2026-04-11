package dev.mrbeastgaming.mods.hub;

import dev.mrbeastgaming.mods.hub.api.HubFileType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface FileTypeProvider {
	static HubFileType probe(Path path) throws IOException {
		try {
			var type = Files.probeContentType(path);
			return type == null || type.isEmpty() ? HubFileType.UNKNOWN : HubFileType.custom(type);
		} catch (Exception ignored) {
			return HubFileType.UNKNOWN;
		}
	}

	HubFileType getFileType(Path path) throws Exception;
}
