package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.klib.util.Hex32;

public interface HubDBObject {
	MapCodec<Hex32> ID_FIELD = Hex32.LENIENT_CODEC.optionalFieldOf("id", Hex32.NONE);

	Hex32 id();
}
