package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.latvian.mods.klib.io.checksum.SHA256;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.api.data.HubWorld;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record HubWorldDirectory(
	Path directory,
	String id,
	String name,
	String path,
	long size,
	Instant created,
	Instant lastModified,
	Checksum icon,
	Optional<Path> iconPath
) {
	public static HubWorldDirectory of(Path root, String name, Path directory) throws IOException {
		var fileId = directory.resolve("uid.dat");

		if (Files.exists(fileId)) {
			try (var file = Files.newInputStream(fileId)) {
				return of(root, name, ByteInput.of(file).readUUID().toString(), directory);
			} catch (Exception ex) {
				VidLib.LOGGER.warn("Failed to read {}, generating new random UUID", fileId, ex);
			}
		}

		var id = UUID.randomUUID();
		var bytes = ByteOutput.ofByteBuilder(16);
		bytes.writeUUID(id);
		Files.write(fileId, bytes.toByteArray());
		return of(root, name, id.toString(), directory);
	}

	public static HubWorldDirectory of(Path root, String name, String id, Path directory) throws IOException {
		directory = directory.toRealPath().toAbsolutePath();

		if (!directory.startsWith(root)) {
			throw new IOException("Directory " + directory + " does not start with root path " + root);
		}

		var path = root.relativize(directory).toString().replace('\\', '/');
		long size = 0L;
		var lastModified = Files.getLastModifiedTime(directory).toInstant();
		var created = Files.getFileAttributeView(directory, BasicFileAttributeView.class).readAttributes().creationTime().toInstant();

		try (var stream = Files.walk(directory)) {
			for (var file : stream.filter(Files::isRegularFile).toList()) {
				size += Files.size(file);

				var lmt = Files.getLastModifiedTime(file).toInstant();

				if (lmt.isAfter(lastModified)) {
					lastModified = lmt;
				}

				var ct = Files.readAttributes(file, BasicFileAttributes.class).creationTime().toInstant();

				if (ct.isBefore(created)) {
					created = ct;
				}
			}
		}

		Checksum icon = NoChecksum.INSTANCE;
		var iconPathOpt = Optional.<Path>empty();

		var iconPath = directory.resolve("icon.png");

		if (Files.exists(iconPath)) {
			var img = ImageIO.read(iconPath.toFile());

			if (img.getWidth() != 64 || img.getHeight() != 64) {
				img = IOUtils.resize(img, 64, 64);
				ImageIO.write(img, "png", iconPath.toFile());
			}

			icon = SHA256.TYPE.digest(iconPath, null);
			iconPathOpt = Optional.of(iconPath);
		}

		return new HubWorldDirectory(
			directory,
			id,
			name,
			path,
			size,
			created,
			lastModified,
			icon,
			iconPathOpt
		);
	}

	public HubWorld toData() {
		return new HubWorld(
			id,
			name,
			path,
			size,
			created,
			lastModified,
			icon
		);
	}
}