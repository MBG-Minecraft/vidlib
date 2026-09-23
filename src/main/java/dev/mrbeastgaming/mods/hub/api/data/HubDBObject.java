package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.klib.util.Hex32;

import java.util.List;

public interface HubDBObject {
	static MapCodec<Hex32> idCodec(String key) {
		return Hex32.LENIENT_CODEC.optionalFieldOf(key, Hex32.NONE);
	}

	static MapCodec<List<Hex32>> idListCodec(String key) {
		return Hex32.LENIENT_CODEC.listOf().optionalFieldOf(key, List.of());
	}

	MapCodec<Hex32> ID_FIELD = idCodec("id");

	Hex32 id();
}
