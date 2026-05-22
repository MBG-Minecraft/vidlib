package dev.latvian.mods.vidlib.feature.pin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImage;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImageKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class Pin {
	public static final Color DEFAULT_COLOR = Color.of(0xFFFFFFFF);
	public static final Color DEFAULT_BACKGROUND = Color.of(0x5A000000);

	public static final MapCodec<Pin> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("enabled", true).forGetter(p -> p.enabled),
		GalleryImageKey.CODEC.optionalFieldOf("icon").forGetter(p -> Optional.ofNullable(p.icon)),
		Color.CODEC.optionalFieldOf("color", DEFAULT_COLOR).forGetter(p -> p.color),
		Color.CODEC.optionalFieldOf("background", DEFAULT_BACKGROUND).forGetter(p -> p.background),
		PinShape.CODEC.optionalFieldOf("shape", PinShape.PIN).forGetter(p -> p.shape)
	).apply(instance, Pin::new));

	public static final Codec<Pin> CODEC = MAP_CODEC.codec();

	public boolean enabled;
	public GalleryImageKey<?> icon;
	public Color color;
	public Color background;
	public PinShape shape;
	public PinShape shapeOverride = null;

	public Pin() {
		this.enabled = true;
		this.icon = null;
		this.color = DEFAULT_COLOR;
		this.background = DEFAULT_BACKGROUND;
		this.shape = PinShape.PIN;
	}

	private Pin(
		boolean enabled,
		Optional<GalleryImageKey<?>> icon,
		Color color,
		Color background,
		PinShape shape
	) {
		this.enabled = enabled;
		this.icon = icon.orElse(null);
		this.color = color;
		this.background = background;
		this.shape = shape;
	}

	public boolean isSet() {
		return icon != null;
	}

	@Nullable
	public GalleryImage<?> getImage() {
		return icon == null ? null : icon.image();
	}

	public void setImage(@Nullable GalleryImage<?> image) {
		icon = image == null ? null : image.key();
	}
}