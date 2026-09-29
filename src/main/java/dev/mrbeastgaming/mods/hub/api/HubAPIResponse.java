package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.net.HttpResponseData;

public class HubAPIResponse extends HttpResponseData {
	public HubAPIResponse(HttpResponseData parent) {
		super(parent);
	}

	@Override
	public <T> T json(Codec<T> codec) {
		return codec.parse(HubAPI.jsonOps(), json()).getOrThrow();
	}
}
