package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.net.URI;

public record HubGameData(
	int uid,
	String id,
	String displayName,
	URI iconUrl
) {
	public static final Codec<HubGameData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("uid").forGetter(HubGameData::uid),
		Codec.STRING.fieldOf("id").forGetter(HubGameData::id),
		Codec.STRING.fieldOf("display_name").forGetter(HubGameData::displayName),
		HubAPI.URI_BASE_CODEC.fieldOf("icon_url").forGetter(HubGameData::iconUrl)
	).apply(instance, HubGameData::new));
}
