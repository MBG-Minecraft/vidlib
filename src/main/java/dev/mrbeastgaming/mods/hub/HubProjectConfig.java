package dev.mrbeastgaming.mods.hub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.nio.file.Files;

public record HubProjectConfig(
	String token
) {
	public static final Codec<HubProjectConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("token").forGetter(HubProjectConfig::token)
	).apply(instance, HubProjectConfig::new));

	public static final Lazy<HubProjectConfig> INSTANCE = Lazy.of(() -> {
		var path = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-project-config.json");

		if (Files.exists(path)) {
			try {
				var json = JsonUtils.read(path);
				return CODEC.parse(HubAPI.jsonOps(), json).getOrThrow();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return null;
	});
}
