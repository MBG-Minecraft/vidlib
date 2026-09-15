package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.latvian.mods.klib.io.checksum.SHA256;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
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
	Optional<byte[]> iconBytes
) {
	public static HubWorldDirectory of(String name, Path directory) throws IOException, NoSuchAlgorithmException {
		var root = PlatformHelper.CURRENT.getGameDirectory().toRealPath().toAbsolutePath();
		directory = directory.toRealPath().toAbsolutePath();

		if (!directory.startsWith(root)) {
			throw new IOException("Directory " + directory + " does not start with root path " + root);
		}

		String id;
		var idPath = directory.resolve("beast-hub-world-id.txt");

		if (Files.notExists(idPath)) {
			id = UUID.randomUUID().toString();
			Files.writeString(idPath, id);
		} else {
			id = Files.readString(idPath).trim();
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
		var iconBytes = Optional.<byte[]>empty();

		var iconPath = directory.resolve("icon.png");

		if (Files.exists(iconPath)) {
			try (var in = Files.newInputStream(iconPath)) {
				var img = IOUtils.resize(ImageIO.read(in), 64, 64);
				var imageBytes = new ByteArrayOutputStream();
				ImageIO.write(img, "png", imageBytes);
				var bytes = imageBytes.toByteArray();
				icon = SHA256.TYPE.digest(bytes);
				iconBytes = Optional.of(bytes);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
			iconBytes
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