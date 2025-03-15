package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.MiscUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;

public class WorldSync {
	public static final String ID = "worldsync";
	public static final String NAME = "World Sync";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static final Lazy<Path> DIR = Lazy.of(() -> MiscUtils.createDir(Path.of(System.getenv().getOrDefault("WORLD_SYNC_HOME", System.getProperty("user.home") + "/.world-sync"))));
	public static final Lazy<Path> REPOSITORIES_FILE = DIR.<Path>map(path -> path.resolve("repositories.json"));
	public static final Lazy<Path> SYNC_DIR = DIR.<Path>map(path -> path.resolve("worlds"));

	public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.followRedirects(HttpClient.Redirect.ALWAYS)
		.connectTimeout(Duration.ofMinutes(5L))
		.build();
}
