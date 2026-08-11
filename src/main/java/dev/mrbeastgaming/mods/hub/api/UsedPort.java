package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonObject;

public record UsedPort(String name, int min, int max, int protocols) {
	public static final int TCP = 1;
	public static final int UDP = 2;

	public static UsedPort tcp(String name, int min, int max) {
		return new UsedPort(name, min, max, TCP);
	}

	public static UsedPort tcp(String name, int port) {
		return tcp(name, port, port);
	}

	public static UsedPort udp(String name, int min, int max) {
		return new UsedPort(name, min, max, UDP);
	}

	public static UsedPort udp(String name, int port) {
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
