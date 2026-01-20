package dev.latvian.mods.vidlib.feature.pin;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.gallery.Gallery;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImage;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Pin {
	public static final Color DEFAULT_COLOR = Color.of(0xFFFFFF);
	public static final Color DEFAULT_BACKGROUND = Color.of(0x5A000000);

	public final UUID uuid;
	public boolean enabled;
	public String gallery;
	public UUID texture;
	public Color color;
	public Color background;
	public PinShape shape;

	public Pin(UUID uuid) {
		this.uuid = uuid;
		this.enabled = true;
		this.gallery = "pins";
		this.texture = Util.NIL_UUID;
		this.color = DEFAULT_COLOR;
		this.background = DEFAULT_BACKGROUND;
		this.shape = PinShape.S1;
	}

	public Pin(JsonObject json) {
		this.uuid = UUID.fromString(json.get("uuid").getAsString());
		this.enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
		this.gallery = json.has("gallery") ? json.get("gallery").getAsString() : "";
		this.texture = json.has("texture") ? UUID.fromString(json.get("texture").getAsString()) : Util.NIL_UUID;
		this.color = json.has("color") ? Color.CODEC_RGB.parse(JsonOps.INSTANCE, json.get("color")).resultOrPartial().orElse(DEFAULT_COLOR) : DEFAULT_COLOR;
		this.background = json.has("background") ? Color.CODEC.parse(JsonOps.INSTANCE, json.get("background")).resultOrPartial().orElse(DEFAULT_BACKGROUND) : DEFAULT_BACKGROUND;
		this.shape = json.has("shape") ? PinShape.VALUES[json.get("shape").getAsInt()] : PinShape.S1;
	}

	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("uuid", uuid.toString());
		json.addProperty("enabled", enabled);
		json.addProperty("gallery", gallery);
		json.addProperty("texture", texture.toString());
		json.addProperty("color", color.toRGBString());
		json.addProperty("background", background.toString());
		json.addProperty("shape", shape.ordinal());
		return json;
	}

	public boolean isSet() {
		return !gallery.isEmpty() && (texture.getMostSignificantBits() != 0L || texture.getLeastSignificantBits() != 0L);
	}

	@Nullable
	public GalleryImage<UUID> getImage() {
		if (isSet()) {
			var g = (Gallery<UUID>) Gallery.ALL.get().get(gallery);

			if (g != null) {
				return g.get(texture);
			}
		}

		return null;
	}

	public void setImage(@Nullable GalleryImage<UUID> image) {
		if (image == null) {
			gallery = "";
			texture = Util.NIL_UUID;
		} else {
			gallery = image.gallery().id;
			texture = image.id();
		}
	}
}