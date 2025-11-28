package dev.latvian.mods.vidlib.feature.pin;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.color.Color;

import java.util.UUID;

public final class Pin {
	public final UUID uuid;
	public boolean enabled;
	public UUID texture;
	public Color color;

	public Pin(UUID uuid) {
		this.uuid = uuid;
		this.enabled = true;
		this.texture = null;
		this.color = Color.of(0x4DFF4D);
	}

	public Pin(JsonObject json) {
		this.uuid = UUID.fromString(json.get("uuid").getAsString());
		this.enabled = json.get("enabled").getAsBoolean();
		this.texture = json.has("texture") ? UUID.fromString(json.get("texture").getAsString()) : null;
		this.color = Color.CODEC_RGB.parse(JsonOps.INSTANCE, json.get("color")).resultOrPartial().orElse(Color.WHITE);
	}

	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("uuid", uuid.toString());
		json.addProperty("enabled", enabled);

		if (texture != null) {
			json.addProperty("texture", texture.toString());
		}

		json.addProperty("color", color.toRGBString());
		return json;
	}
}