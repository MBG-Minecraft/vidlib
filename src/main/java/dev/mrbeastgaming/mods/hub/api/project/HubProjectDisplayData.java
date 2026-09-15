package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.HubGameData;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;

public record HubProjectDisplayData(
	Hex32 id,
	String name,
	String description,
	String productionCode,
	HubGameData game,
	HubProjectFlags flags
) {
	public static final MapCodec<HubProjectDisplayData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubProjectDisplayData::id),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubProjectDisplayData::name),
		Codec.STRING.optionalFieldOf("description", "").forGetter(HubProjectDisplayData::description),
		Codec.STRING.optionalFieldOf("production_code", "").forGetter(HubProjectDisplayData::productionCode),
		HubGameData.CODEC.fieldOf("game").forGetter(HubProjectDisplayData::game),
		HubProjectFlags.MAP_CODEC.forGetter(HubProjectDisplayData::flags)
	).apply(instance, HubProjectDisplayData::new));

	public static final Codec<HubProjectDisplayData> CODEC = MAP_CODEC.codec();

	public static final Codec<Int2ObjectMap<HubProjectDisplayData>> INT_MAP_CODEC = CODEC.listOf().xmap(list -> {
		var map = new Int2ObjectLinkedOpenHashMap<HubProjectDisplayData>();

		for (var project : list) {
			map.put(project.id().raw(), project);
		}

		return map;
	}, map -> new ArrayList<>(map.values()));

	@Override
	public String toString() {
		return name + "#" + id;
	}
}
