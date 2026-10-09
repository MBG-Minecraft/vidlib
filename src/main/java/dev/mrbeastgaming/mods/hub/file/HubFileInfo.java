package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.FileChecksum;
import dev.latvian.mods.klib.io.checksum.SHA256;
import dev.latvian.mods.vidlib.VidLib;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

public record HubFileInfo(
	Path file,
	long size,
	Checksum checksum,
	Instant created,
	Instant lastModified,
	String fullPath,
	String path,
	String name
) {
	@FunctionalInterface
	public interface Filter {
		boolean test(HubFileInfo info) throws IOException;
	}

	@Nullable
	public static HubFileInfo of(HubUploadContext ctx, Path root, Path directory, Path file) throws IOException {
		var attributes = Files.readAttributes(file, BasicFileAttributes.class);
		var size = attributes.size();

		if (size <= 0L) {
			return null;
		}

		var created = attributes.lastModifiedTime().toInstant();
		var lastModified = attributes.lastModifiedTime().toInstant();

		String fullPath, path;

		try {
			fullPath = root.relativize(file).toString().replace('\\', '/');
			path = directory.relativize(file).toString().replace('\\', '/');
		} catch (IllegalArgumentException ex) {
			VidLib.LOGGER.error("Failed to resolve relative path:\nRoot: " + root + "\nDirectory: " + directory + "\nFile: " + file, ex);
			return null;
		}

		var progressItem = ctx.createCheckItem(path);

		if (progressItem != null) {
			progressItem.setSize(size);
			progressItem.display();
		}

		FileChecksum meta;

		try {
			meta = FileChecksum.loadAndSave(SHA256.TYPE, file, attributes, progressItem);
		} finally {
			if (progressItem != null) {
				progressItem.remove();
			}
		}

		if (meta.changed()) {
			VidLib.LOGGER.info("Updated SHA-256 of " + fullPath + ": " + meta.checksum());
		}

		int fnIndex = path.lastIndexOf('/');

		return new HubFileInfo(
			file,
			size,
			meta.checksum(),
			created,
			lastModified,
			fullPath,
			path,
			fnIndex == -1 ? path : path.substring(fnIndex + 1)
		);
	}
}
