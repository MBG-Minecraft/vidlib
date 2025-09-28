package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonObject;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.util.JsonUtils;
import net.minecraft.client.Options;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalKeybinds {
	private static final Path PATH = VidLibPaths.USER.resolve("global-keybinds.json");
	private static JsonObject json;

	public static JsonObject getJson() {
		if (json == null) {
			json = new JsonObject();

			if (Files.exists(PATH)) {
				try (var reader = Files.newBufferedReader(PATH)) {
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

		try (var writer = Files.newBufferedWriter(PATH)) {
			JsonUtils.write(writer, json, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
