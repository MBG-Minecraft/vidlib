package dev.latvian.mods.vidlib.core.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.core.UUIDUtil;
import net.neoforged.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Mixin(UUIDUtil.class)
public class UUIDUtilMixin {
	@Inject(method = "createOfflinePlayerUUID", at = @At("HEAD"), cancellable = true)
	private static void vl$createOfflinePlayerUUID(String name, CallbackInfoReturnable<UUID> cir) {
		if (VidLibConfig.fetchOfflinePlayerData && !FMLLoader.isProduction() && !name.startsWith("Player") && !name.startsWith("Dev")) {
			try {
				VidLib.LOGGER.info("Fetching offline UUID for " + name + "...");
				HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				connection.setRequestProperty("User-Agent", "VidLib/1.0");
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(3000);

				if (connection.getResponseCode() == 200) {
					try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
						var json = new Gson().fromJson(reader, JsonObject.class);
						var uuid = UndashedUuid.fromStringLenient(json.get("id").getAsString());
						VidLib.LOGGER.info("UUID for " + name + " found: " + uuid);
						cir.setReturnValue(uuid);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
