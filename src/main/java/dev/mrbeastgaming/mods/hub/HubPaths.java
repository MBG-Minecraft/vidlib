package dev.mrbeastgaming.mods.hub;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.Util;

import java.nio.file.Files;
import java.nio.file.Path;

public interface HubPaths {
	Lazy<Path> DATA_DIRECTORY = Lazy.of(() -> {
		var override = System.getenv("MBG_HUB_DATA_DIRECTORY");

		if (override == null || override.isEmpty()) {
			var userHome = Util.getPlatform() == Util.OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
			return Path.of(userHome).resolve("hub.mrbeastmc.com");
		} else {
			return Path.of(override);
		}
	});

	Lazy<Path> USER_CONFIG = Lazy.of(() -> {
		var path = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-user-config.json");

		if (Files.exists(path)) {
			return path;
		}

		return DATA_DIRECTORY.get().resolve("beast-hub-user-config.json");
	});
}
