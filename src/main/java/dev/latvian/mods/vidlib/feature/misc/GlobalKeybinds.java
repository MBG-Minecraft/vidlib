package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonObject;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.JsonUtils;
import net.minecraft.client.Options;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalKeybinds {
	private static Path path;
	private static JsonObject json;

	public static Path getPath() {
		if (path == null) {
			path = Path.of(System.getProperty("user.home") + "/.latvian.dev/global-keybinds.json");
		}

		return path;
	}

	public static JsonObject getJson() {
		if (json == null) {
			json = new JsonObject();

			var path = getPath();

			if (Files.exists(path)) {
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

	@AutoInit(AutoInit.Type.CLIENT_OPTIONS_SAVED)
	public static void saveKeybinds(Options options) {
		var json = getJson();

		for (var key : options.keyMappings) {
			json.addProperty(key.getName(), key.saveString() + (key.getKeyModifier() != KeyModifier.NONE ? ":" + key.getKeyModifier() : ""));
		}

		var path = getPath();

		if (Files.notExists(path.getParent())) {
			try {
				Files.createDirectories(path.getParent());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try (var writer = Files.newBufferedWriter(path)) {
			JsonUtils.write(writer, json, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
