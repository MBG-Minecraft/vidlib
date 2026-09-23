package dev.mrbeastgaming.mods.hub.api.data;

import com.google.gson.JsonObject;

public record HubUsedPort(String name, int min, int max, int protocols) {
	public static final int TCP = 1;
	public static final int UDP = 2;

	public static HubUsedPort tcp(String name, int min, int max) {
		return new HubUsedPort(name, min, max, TCP);
	}

	public static HubUsedPort tcp(String name, int port) {
		return tcp(name, port, port);
	}

	public static HubUsedPort udp(String name, int min, int max) {
		return new HubUsedPort(name, min, max, UDP);
	}

	public static HubUsedPort udp(String name, int port) {
		return udp(name, port, port);
	}

	public boolean tcp() {
		return (protocols & TCP) != 0;
	}

	public boolean udp() {
		return (protocols & UDP) != 0;
	}

	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("name", name);
		json.addProperty("min", Math.min(min, max));
		json.addProperty("max", Math.max(min, max));

		if (tcp()) {
			json.addProperty("tcp", true);
		}

		if (udp()) {
			json.addProperty("udp", true);
		}

		return json;
	}
}
