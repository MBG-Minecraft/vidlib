package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public class ColorIcon implements Icon {
	private final Color color;

	public static final SimpleRegistryType<ColorIcon> TYPE = SimpleRegistryType.dynamic("color", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.fieldOf("color").forGetter(ColorIcon::color)
	).apply(instance, ColorIcon::new)), Color.STREAM_CODEC.map(ColorIcon::new, ColorIcon::color));

	public ColorIcon(Color color) {
		this.color = color;
	}

	public Color color() {
		return color;
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
