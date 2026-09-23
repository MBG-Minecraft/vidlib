package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record HubGameServer(
	String name,
	String location,
	Optional<byte[]> icon,
	Optional<URI> iconUrl
) {
	public static final Codec<HubGameServer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(HubGameServer::name),
		Codec.STRING.fieldOf("location").forGetter(HubGameServer::location),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("icon").forGetter(HubGameServer::icon),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("icon_url").forGetter(HubGameServer::iconUrl)
	).apply(instance, HubGameServer::new));

	public static final Codec<List<HubGameServer>> LIST_CODEC = CODEC.listOf();
}
