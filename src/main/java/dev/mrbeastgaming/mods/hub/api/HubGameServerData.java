package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;

import java.util.List;
import java.util.Optional;

public record HubGameServerData(
	String name,
	String ip,
	Optional<byte[]> icon
) {
	public static final Codec<HubGameServerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(HubGameServerData::name),
		Codec.STRING.fieldOf("ip").forGetter(HubGameServerData::ip),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("icon").forGetter(HubGameServerData::icon)
	).apply(instance, HubGameServerData::new));

	public static final Codec<List<HubGameServerData>> LIST_CODEC = CODEC.listOf();

	public static List<HubGameServerData> CURRENT = List.of();
}
