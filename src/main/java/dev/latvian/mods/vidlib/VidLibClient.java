package dev.latvian.mods.vidlib;

import dev.mrbeastgaming.mods.hub.api.HubClientSessionData;

public class VidLibClient {
	public static void init() {
		HubClientSessionData.loadAsync();
	}
}
