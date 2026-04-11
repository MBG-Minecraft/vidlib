package dev.mrbeastgaming.mods.hub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.mrbeastgaming.mods.hub.api.token.UserToken;

import java.nio.file.Files;
import java.util.Optional;

public record HubUserConfig(
	Optional<UserToken> token
) {
	public static final Codec<HubUserConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		UserToken.CODEC.optionalFieldOf("token").forGetter(HubUserConfig::token)
	).apply(instance, HubUserConfig::new));

	private static HubUserConfig instance = null;

	public static synchronized HubUserConfig load() {
		if (instance == null) {
			var file = HubPaths.USER_CONFIG.get();

			if (Files.exists(file)) {
				try {
					var json = JsonUtils.read(file);
					instance = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
					return instance;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			instance = new HubUserConfig(Optional.empty());
		}

		return instance;
	}

	public static synchronized void save(HubUserConfig config) {
		instance = config;
		var file = HubPaths.USER_CONFIG.get();

		try {
			JsonUtils.write(file, CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow(), true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public HubUserConfig withToken(UserToken token) {
		return new HubUserConfig(Optional.of(token));
	}
}
