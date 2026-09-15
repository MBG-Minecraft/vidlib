package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;

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

	public static final Codec<Int2ObjectMap<HubUserDisplayData>> INT_MAP_CODEC = CODEC.listOf().xmap(list -> {
		var map = new Int2ObjectLinkedOpenHashMap<HubUserDisplayData>();

		for (var user : list) {
			map.put(user.id().raw(), user);
		}

		return map;
	}, map -> new ArrayList<>(map.values()));

	@Override
	public String toString() {
		return name + "#" + id;
	}
}
