package dev.mrbeastgaming.mods.hub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.mrbeastgaming.mods.hub.api.token.ProjectToken;

import java.nio.file.Files;

public record HubProjectConfig(
	Hex32 projectId,
	ProjectToken token
) {
	public static final Codec<HubProjectConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("project_id").forGetter(HubProjectConfig::projectId),
		ProjectToken.CODEC.fieldOf("token").forGetter(HubProjectConfig::token)
	).apply(instance, HubProjectConfig::new));

	public static final Lazy<HubProjectConfig> INSTANCE = Lazy.of(() -> {
		var path = PlatformHelper.CURRENT.getGameDirectory().resolve("beast-hub-project-config.json");

		if (Files.exists(path)) {
			try {
				var json = JsonUtils.read(path);
				return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return null;
	});
}
