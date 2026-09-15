package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;

public record HubUserDisplayData(
	Hex32 id,
	String name,
	String avatarUrl
) {
	public static final MapCodec<HubUserDisplayData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubUserDisplayData::id),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubUserDisplayData::name),
		Codec.STRING.optionalFieldOf("avatar_url", "").forGetter(HubUserDisplayData::avatarUrl)
	).apply(instance, HubUserDisplayData::new));

	public static final Codec<HubUserDisplayData> CODEC = MAP_CODEC.codec();

	@Override
	public String toString() {
		return name + "#" + id;
	}
}
