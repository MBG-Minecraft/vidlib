package dev.beast.mods.shimmer.feature.worldsync;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.util.JsonUtils;
import dev.beast.mods.shimmer.util.Lazy;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record WorldSyncRepo(String id, String displayName, String address, String token, List<WorldSyncProject> projects) {
	public static final Lazy<Map<String, WorldSyncRepo>> MAP = Lazy.of(() -> {
		var map = new LinkedHashMap<String, WorldSyncRepo>();

		var path = WorldSync.REPOSITORIES_FILE.get();

		if (Files.exists(path)) {
			try (var reader = Files.newBufferedReader(path)) {
				var json = JsonUtils.read(reader);
				var list = WorldSyncRepo.CODEC.listOf().parse(JsonOps.INSTANCE, json).getOrThrow();

				for (var repo : list) {
					map.put(repo.id(), repo);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return map;
	});

	public static void save() {
		var path = WorldSync.REPOSITORIES_FILE.get();

		try (var writer = Files.newBufferedWriter(path)) {
			var json = WorldSyncRepo.CODEC.listOf().encodeStart(JsonOps.INSTANCE, List.copyOf(MAP.get().values())).getOrThrow();
			JsonUtils.write(writer, json, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static final Codec<WorldSyncRepo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(WorldSyncRepo::id),
		Codec.STRING.fieldOf("display_name").forGetter(WorldSyncRepo::displayName),
		Codec.STRING.fieldOf("address").forGetter(WorldSyncRepo::address),
		Codec.STRING.fieldOf("token").forGetter(WorldSyncRepo::token)
	).apply(instance, WorldSyncRepo::new));

	public WorldSyncRepo(String id, String displayName, String address, String token) {
		this(id, displayName, address, token, new ArrayList<>());
	}

	public boolean replaces(WorldSyncRepo repo) {
		return address.equals(repo.address);
	}
}
