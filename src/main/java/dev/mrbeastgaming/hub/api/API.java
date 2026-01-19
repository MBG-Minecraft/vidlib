package dev.mrbeastgaming.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class API {
	public static final Codec<Integer> HEX_ID_CODEC = Codec.STRING.comapFlatMap(id -> {
		if (id.length() == 8) {
			try {
				return DataResult.success(Integer.parseUnsignedInt(id, 16));
			} catch (Exception ignored) {
			}
		}

		return DataResult.error(() -> "Invalid hex ID '" + id + "'");
	}, "%08x"::formatted);
}
