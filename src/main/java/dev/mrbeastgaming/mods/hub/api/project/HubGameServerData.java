package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record HubGameServerData(
	String name,
	String location,
	Optional<byte[]> icon,
	Optional<URI> iconUrl
) {
	public static final Codec<HubGameServerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(HubGameServerData::name),
		Codec.STRING.fieldOf("location").forGetter(HubGameServerData::location),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("icon").forGetter(HubGameServerData::icon),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("icon_url").forGetter(HubGameServerData::iconUrl)
	).apply(instance, HubGameServerData::new));

	public static final Codec<List<HubGameServerData>> LIST_CODEC = CODEC.listOf();

	public static List<HubGameServerData> CURRENT = List.of();
}
