package dev.latvian.mods.vidlib.feature.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.util.JsonUtils;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public class ForcedPlayerOverrides {
	public static Map<UUID, Map<EntityOverride<?>, Object>> MAP = Map.of();
	public static final Codec<Map<UUID, Map<EntityOverride<?>, Object>>> CODEC = Codec.unboundedMap(VLCodecs.UUID, EntityOverride.OVERRIDE_MAP_CODEC);

	@AutoInit({AutoInit.Type.ASSETS_LOADED, AutoInit.Type.DATA_LOADED, AutoInit.Type.GAME_LOADED})
	public static void reload() {
		var file = FMLPaths.GAMEDIR.get().resolve("forced_player_overrides.json");

		if (Files.exists(file)) {
			try (var reader = Files.newBufferedReader(file)) {
				MAP = CODEC.decode(JsonOps.INSTANCE, JsonUtils.read(reader)).getOrThrow().getFirst();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
