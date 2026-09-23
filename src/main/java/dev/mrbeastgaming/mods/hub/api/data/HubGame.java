package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;

public record HubGame(
	int uid,
	String id,
	String displayName,
	URI iconUrl
) {
	public static final Codec<HubGame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("uid").forGetter(HubGame::uid),
		Codec.STRING.fieldOf("id").forGetter(HubGame::id),
		Codec.STRING.fieldOf("display_name").forGetter(HubGame::displayName),
		HubAPI.URI_BASE_CODEC.fieldOf("icon_url").forGetter(HubGame::iconUrl)
	).apply(instance, HubGame::new));
}
