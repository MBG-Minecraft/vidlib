package dev.latvian.mods.vidlib;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public class VidLibPaths {
	public static final Path GAME = FMLPaths.GAMEDIR.get().resolve("vidlib");
	public static final Path LOCAL = FMLPaths.GAMEDIR.get().resolve("local/vidlib");
	public static final Path USER = Path.of(System.getProperty("user.home") + "/.latvian.dev");

	static {
		try {
			if (Files.notExists(GAME)) {
				Files.createDirectories(GAME);
			}

			if (Files.notExists(LOCAL)) {
				Files.createDirectories(LOCAL);
			}

			if (Files.notExists(USER)) {
				Files.createDirectories(USER);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create VidLib paths", ex);
		}
	}
}
