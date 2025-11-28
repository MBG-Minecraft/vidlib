package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.util.Lazy;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public class VidLibPaths {
	public static final Lazy<Path> GAME = Lazy.of(() -> FMLPaths.GAMEDIR.get().resolve("vidlib"));
	public static final Lazy<Path> LOCAL = Lazy.of(() -> FMLPaths.GAMEDIR.get().resolve("local/vidlib"));

	public static final Lazy<Path> USER = Lazy.of(() -> {
		var override = System.getenv("VIDLIB_USER_DATA_DIRECTORY");

		if (override == null || override.isEmpty()) {
			return Path.of(System.getProperty("user.home")).resolve(".latvian.dev");
		} else {
			return Path.of(override);
		}
	});

	public static Path mkdirs(Path path) {
		var parent = path.getParent();

		if (Files.notExists(parent)) {
			try {
				Files.createDirectories(parent);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to create VidLib paths", ex);
			}
		}

		return path;
	}
}
