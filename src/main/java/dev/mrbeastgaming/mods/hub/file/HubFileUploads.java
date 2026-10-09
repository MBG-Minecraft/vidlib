package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.vidlib.VidLib;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public interface HubFileUploads {
	HubUploadContext getUploadContext();

	Path getGameDirectory();

	void add(HubFileInfo info, HubUploadBuilder builder);

	default void addDirectory(Path directory, @Nullable HubFileInfo.Filter filter, HubUploadBuilderCallback callback) {
		if (Files.notExists(directory)) {
			return;
		}

		var ctx = getUploadContext();
		var gameDirectory = getGameDirectory();

		try (var stream = Files.walk(directory)) {
			var fileStream = stream
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.map(path -> {
					try {
						return HubFileInfo.of(ctx, gameDirectory, directory, path);
					} catch (IOException ex) {
						VidLib.LOGGER.error("Failed to load HubFileInfo of " + path.toAbsolutePath(), ex);
						return null;
					}
				})
				.filter(Objects::nonNull);

			if (filter != null) {
				fileStream = fileStream.filter(info -> {
					try {
						return filter.test(info);
					} catch (Exception ex) {
						ex.printStackTrace();
						return false;
					}
				});
			}

			for (var info : fileStream.toList()) {
				var uploadBuilder = new HubUploadBuilder(info);
				callback.build(uploadBuilder);
				add(info, uploadBuilder);
			}
		} catch (IOException ex) {
			VidLib.LOGGER.error("Failed to load walk directory " + directory.toAbsolutePath(), ex);
		}
	}

	default void addDirectory(Path directory, HubUploadBuilderCallback callback) {
		addDirectory(directory, (HubFileInfo.Filter) null, callback);
	}

	default void addDirectory(Path directory, String suffix, HubUploadBuilderCallback callback) {
		addDirectory(directory, fileInfo -> fileInfo.path().endsWith(suffix), callback);
	}

	default void addFile(Path file, HubUploadBuilderCallback callback) {
		if (Files.notExists(file) || !Files.isRegularFile(file) || !Files.isReadable(file)) {
			return;
		}

		var ctx = getUploadContext();
		var gameDirectory = getGameDirectory();

		try {
			var info = HubFileInfo.of(ctx, gameDirectory, file.getParent(), file);

			if (info != null) {
				var uploadBuilder = new HubUploadBuilder(info);
				callback.build(uploadBuilder);
				add(info, uploadBuilder);
			}
		} catch (IOException ex) {
			VidLib.LOGGER.error("Failed to load HubFileInfo of " + file.toAbsolutePath(), ex);
		}
	}
}
