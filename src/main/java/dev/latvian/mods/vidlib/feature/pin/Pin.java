package dev.latvian.mods.vidlib.feature.pin;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.gallery.Gallery;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImage;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Pin {
	public static final Color DEFAULT_COLOR = Color.of(0xFFFFFF);
	public static final Color DEFAULT_BACKGROUND = Color.of(0x5A000000);

	public static final Codec<Pin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.UUID.fieldOf("uuid").forGetter(p -> p.uuid),
		Codec.BOOL.optionalFieldOf("enabled", true).forGetter(p -> p.enabled),
		Codec.STRING.optionalFieldOf("gallery", "").forGetter(p -> p.gallery),
		KLibCodecs.UUID.optionalFieldOf("texture", Util.NIL_UUID).forGetter(p -> p.texture),
		Color.SOLID_CODEC.optionalFieldOf("color", DEFAULT_COLOR).forGetter(p -> p.color),
		Color.CODEC.optionalFieldOf("background", DEFAULT_BACKGROUND).forGetter(p -> p.background),
		PinShape.CODEC.optionalFieldOf("shape", PinShape.PIN).forGetter(p -> p.shape)
	).apply(instance, Pin::new));

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
		this.gallery = "";
		this.texture = Util.NIL_UUID;
		this.color = DEFAULT_COLOR;
		this.background = DEFAULT_BACKGROUND;
		this.shape = PinShape.PIN;
	}

	public Pin(
		UUID uuid,
		boolean enabled,
		String gallery,
		UUID texture,
		Color color,
		Color background,
		PinShape shape
	) {
		this.uuid = uuid;
		this.enabled = enabled;
		this.gallery = gallery;
		this.texture = texture;
		this.color = color;
		this.background = background;
		this.shape = shape;
	}

	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("uuid", uuid.toString());
		json.addProperty("enabled", enabled);
		json.addProperty("gallery", gallery);
		json.addProperty("texture", texture.toString());
		json.addProperty("color", color.toRGBString());
		json.addProperty("background", background.toString());
		json.addProperty("shape", shape.id);
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