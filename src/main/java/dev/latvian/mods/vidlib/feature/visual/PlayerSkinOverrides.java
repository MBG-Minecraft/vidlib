package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public record PlayerSkinOverrides(Map<UUID, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> map) {
	private static PlayerSkinOverrides INSTANCE;

	@Nullable
	public static MinecraftProfileTexture get(UUID uuid, MinecraftProfileTexture.Type type) {
		if (INSTANCE == null) {
			var map = new HashMap<UUID, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>();

			var path = VidLibPaths.GAME.resolve("skin-overrides.json");

			if (Files.exists(path)) {
				try (var reader = Files.newBufferedReader(path)) {
					var json = JsonUtils.read(reader);

					for (var entry : json.getAsJsonObject().entrySet()) {
						var id = UndashedUuid.fromStringLenient(entry.getKey());
						var textures = new EnumMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>(MinecraftProfileTexture.Type.class);
						var json1 = entry.getValue().getAsJsonObject();

						for (var t : MinecraftProfileTexture.Type.values()) {
							var k = t.name().toLowerCase(Locale.ROOT);

							if (json1.has(k)) {
								textures.put(t, new MinecraftProfileTexture(json1.get(k).getAsString(), Map.of()));
							}
						}

						map.put(id, textures);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			INSTANCE = new PlayerSkinOverrides(Map.copyOf(map));
		}

		var m = INSTANCE.map.get(uuid);
		return m == null ? null : m.get(type);
	}
}
