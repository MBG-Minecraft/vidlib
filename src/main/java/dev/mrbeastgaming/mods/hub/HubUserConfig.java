package dev.mrbeastgaming.mods.hub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.nio.file.Files;

public record HubUserConfig(
	String token
) {
	public static final Codec<HubUserConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("token", "").forGetter(HubUserConfig::token)
	).apply(instance, HubUserConfig::new));

	private static HubUserConfig instance = null;

	public static synchronized HubUserConfig load() {
		if (instance == null) {
			var file = HubPaths.USER_CONFIG.get();

			if (Files.exists(file)) {
				try {
					var json = JsonUtils.read(file);
					instance = CODEC.parse(HubAPI.jsonOps(), json).getOrThrow();
					return instance;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			instance = new HubUserConfig("");
		}

		return instance;
	}

	public static synchronized void save(HubUserConfig config) {
		instance = config;
		var file = HubPaths.USER_CONFIG.get();

		try {
			JsonUtils.write(CommonPaths.mkdirs(file), CODEC.encodeStart(HubAPI.jsonOps(), config).getOrThrow(), true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public HubUserConfig withToken(String token) {
		return new HubUserConfig(token);
	}
}
