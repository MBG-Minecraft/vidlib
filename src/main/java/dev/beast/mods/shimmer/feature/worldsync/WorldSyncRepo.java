package dev.beast.mods.shimmer.feature.worldsync;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.Lazy;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public record WorldSyncRepo(String address, int port, String password, List<WorldSyncProject> projects) {
	public static final Lazy<List<WorldSyncRepo>> LIST = Lazy.of(() -> {
		var list = new ArrayList<WorldSyncRepo>();

		try {
			var path = Shimmer.HOME_DIR.get().resolve("world-sync");
			Files.createDirectories(path);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return list;
	});

	public static final Codec<WorldSyncRepo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("address").forGetter(WorldSyncRepo::address),
		Codec.INT.fieldOf("port").forGetter(WorldSyncRepo::port),
		Codec.STRING.fieldOf("password").forGetter(WorldSyncRepo::password)
	).apply(instance, WorldSyncRepo::new));

	private WorldSyncRepo(String ip, int port, String password) {
		this(ip, port, password, new ArrayList<>());
	}
}
