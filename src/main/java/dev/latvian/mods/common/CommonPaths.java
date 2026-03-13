package dev.latvian.mods.common;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.Util;

import java.nio.file.Files;
import java.nio.file.Path;

public interface CommonPaths {
	Lazy<Path> LOCAL = Lazy.of(() -> PlatformHelper.CURRENT.getLocalDirectory());

	Lazy<Path> USER = Lazy.of(() -> {
		var override = System.getenv("LATVIAN_DEV_DATA_DIRECTORY");

		if (override == null || override.isEmpty()) {
			var userHome = Util.getPlatform() == Util.OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
			return Path.of(userHome).resolve("latvian.dev");
		} else {
			return Path.of(override);
		}
	});

	static Path mkdirs(Path path) {
		var parent = path.getParent();

		if (Files.notExists(parent)) {
			try {
				Files.createDirectories(parent);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to create paths", ex);
			}
		}

		return path;
	}
}
