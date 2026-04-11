package dev.mrbeastgaming.mods.hub;

import dev.latvian.mods.klib.util.Lazy;
import net.minecraft.Util;

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
}
