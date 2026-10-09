package dev.mrbeastgaming.mods.hub;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.CommonPaths;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.util.JsonUtils;

import java.nio.file.Files;

public class HubConfig {
	public static String userToken = "";
	public static String projectToken = "";
	public static String identity = "";

	public static void load() {
		userToken = "";
		projectToken = "";
		identity = "";

		var userConfigPath = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-user-config.json");

		if (Files.notExists(userConfigPath)) {
			userConfigPath = HubPaths.DATA_DIRECTORY.get().resolve("beast-hub-user-config.json");
		}

		if (Files.exists(userConfigPath)) {
			try {
				userToken = JsonUtils.read(userConfigPath).getAsJsonObject().get("token").getAsString();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		var projectConfigPath = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-project-config.json");

		if (Files.exists(projectConfigPath)) {
			try {
				projectToken = JsonUtils.read(projectConfigPath).getAsJsonObject().get("token").getAsString();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		var identityConfigPath = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-identity-config.json");

		if (Files.exists(identityConfigPath)) {
			try {
				identity = JsonUtils.read(identityConfigPath).getAsJsonObject().get("identity").getAsString();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void saveUser() {
		var userConfigPath = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-user-config.json");

		if (Files.notExists(userConfigPath)) {
			userConfigPath = HubPaths.DATA_DIRECTORY.get().resolve("beast-hub-user-config.json");
		}

		try {
			var json = new JsonObject();
			json.addProperty("token", userToken);
			JsonUtils.write(CommonPaths.mkdirs(userConfigPath), json, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
