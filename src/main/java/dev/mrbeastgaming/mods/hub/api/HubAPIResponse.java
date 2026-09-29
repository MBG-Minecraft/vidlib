package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.net.HttpResponseData;
import dev.latvian.mods.vidlib.VidLib;

public class HubAPIResponse extends HttpResponseData {
	public HubAPIResponse(HttpResponseData parent) {
		super(parent);
	}

	@Override
	public <T> T json(Codec<T> codec) {
		var json = json();

		try {
			return codec.parse(HubAPI.jsonOps(), json).getOrThrow();
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to parse json `" + json + "`");
			throw ex;
		}
	}
}
