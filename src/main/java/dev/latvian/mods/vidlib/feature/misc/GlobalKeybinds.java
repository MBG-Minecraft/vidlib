package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonObject;
import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.util.JsonUtils;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalKeybinds {
	public static final Lazy<Path> PATH = Lazy.of(() -> {
		var override = System.getenv("MC_GLOBAL_KEYBINDS_PATH");

		if (override == null || override.isEmpty()) {
			var userHome = Util.getPlatform() == Util.OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
			return Path.of(userHome).resolve("latvian.dev").resolve("minecraft-global-keybinds.json");
		} else {
			return Path.of(override);
		}
	});

	private static JsonObject json;

	public static JsonObject getJson() {
		if (json == null) {
			json = new JsonObject();
			var path = PATH.get();

			if (path != null && Files.exists(path)) {
				try (var reader = Files.newBufferedReader(path)) {
					json = JsonUtils.read(reader).getAsJsonObject();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		return json;
	}

	@Nullable
	public static String get(String key) {
		var json = getJson();
		return json.has(key) ? json.get(key).getAsString() : null;
	}

	public static void saveKeybinds(Options options) {
		var json = getJson();

		for (var key : options.keyMappings) {
			json.addProperty(key.getName(), key.saveString() + (key.getKeyModifier() != KeyModifier.NONE ? ":" + key.getKeyModifier() : ""));
		}

		var path = PATH.get();

		if (path != null) {
			try (var writer = Files.newBufferedWriter(CommonPaths.mkdirs(path))) {
				JsonUtils.write(writer, json, true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
