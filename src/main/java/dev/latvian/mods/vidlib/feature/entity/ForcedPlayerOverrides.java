package dev.latvian.mods.vidlib.feature.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.JsonUtils;

import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public class ForcedPlayerOverrides {
	public static Map<UUID, Map<EntityOverride<?>, Object>> MAP = Map.of();
	public static final Codec<Map<UUID, Map<EntityOverride<?>, Object>>> CODEC = Codec.unboundedMap(KLibCodecs.UUID, EntityOverride.OVERRIDE_MAP_CODEC);

	@AutoInit({AutoInit.Type.ASSETS_LOADED, AutoInit.Type.DATA_LOADED, AutoInit.Type.GAME_LOADED})
	public static void reload() {
		var file = VidLibPaths.GAME.get().resolve("forced_player_overrides.json");

		if (Files.exists(file)) {
			try (var reader = Files.newBufferedReader(file)) {
				MAP = CODEC.decode(JsonOps.INSTANCE, JsonUtils.read(reader)).getOrThrow().getFirst();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
