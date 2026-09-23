package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.checksum.SHA256;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.mrbeastgaming.mods.hub.api.data.HubUploadRequestFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public record HubUploadRequestFileWithPath(HubUploadRequestFile file, Path path) {
	public static HubUploadRequestFileWithPath load(String id, Path path, @Nullable ProgressItem progressItem) throws IOException {
		var attributes = Files.readAttributes(path, BasicFileAttributes.class);
		long size = attributes.size();

		return new HubUploadRequestFileWithPath(new HubUploadRequestFile(
			id,
			SHA256.TYPE.digest(new FileInfo(path, "", size), progressItem),
			size,
			attributes.creationTime().toInstant(),
			attributes.lastModifiedTime().toInstant()
		), path);
	}

	public static List<HubUploadRequestFileWithPath> loadDirectory(String prefix, Path root, @Nullable ProgressItem progressItem) throws IOException {
		var list = new ArrayList<HubUploadRequestFileWithPath>();

		try (var stream = Files.walk(root)) {
			for (var path : stream.filter(Files::isRegularFile).toList()) {
				if (Files.size(path) > 0L) {
					list.add(load(prefix + root.relativize(path).toString().replace('\\', '/'), path, progressItem));
				}
			}
		}

		return list;
	}
}
