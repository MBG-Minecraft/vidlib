package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
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

	private static final Lazy<JsonObject> JSON = Lazy.of(() -> {
		var json = new JsonObject();
		var path = PATH.get();

		if (path != null && Files.exists(path)) {
			try (var reader = Files.newBufferedReader(path)) {
				json = JsonUtils.read(reader).getAsJsonObject();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return json;
	});

	@Nullable
	public static String get(String key) {
		var json = JSON.get();

		if (json.has(key)) {
			var k = json.get(key).getAsString();
			return k.isEmpty() ? "key.keyboard.unknown" : k;
		}

		return null;
	}

	public static void saveKeybinds(Options options) {
		var json = JSON.get();

		for (var key : options.keyMappings) {
			var k = key.saveString();

			if (key.getKeyModifier() != KeyModifier.NONE) {
				k += ":" + key.getKeyModifier();
			}

			if (k.equals("key.keyboard.unknown")) {
				k = "";
			}

			json.addProperty(key.getName(), k);
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

	public static InputConstants.Key modifyDefaultKeys(String name, InputConstants.Key original) {
		return switch (name) {
			case "key.saveToolbarActivator",
				 "key.loadToolbarActivator",
				 "key.socialInteractions",
				 "key.atlasviewer.open_viewer",
				 "key.curios.open.desc",
				 "key.bridgingmod.toggle_bridging",
				 "key.voice_chat_group",
				 "key.advancements" -> InputConstants.UNKNOWN;
			case "key.jei.bookmark" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_EQUALS);
			case "key.push_to_talk" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_C);
			case "key.ok_zoomer.zoom" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z);
			default -> original;
		};
	}
}
