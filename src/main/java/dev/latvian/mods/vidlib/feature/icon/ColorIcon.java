package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public record ColorIcon(Color color) implements Icon {
	public static final SimpleRegistryType<ColorIcon> TYPE = SimpleRegistryType.dynamic("color", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.fieldOf("color").forGetter(ColorIcon::color)
	).apply(instance, ColorIcon::new)), Color.STREAM_CODEC.map(ColorIcon::new, ColorIcon::color));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
