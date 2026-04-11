package dev.mrbeastgaming.mods.hub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.mrbeastgaming.mods.hub.api.token.UserToken;

import java.nio.file.Files;
import java.util.Optional;

public record HubUserConfig(
	Optional<UserToken> token
) {
	public static final Codec<HubUserConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		UserToken.CODEC.optionalFieldOf("token").forGetter(HubUserConfig::token)
	).apply(instance, HubUserConfig::new));

	public static final Lazy<HubUserConfig> INSTANCE = Lazy.of(() -> {
		var file = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-user-config.json");

		if (Files.exists(file)) {
			try (var reader = Files.newBufferedReader(file)) {
				var json = JsonUtils.read(reader);
				return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return null;
	});
}
