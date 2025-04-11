package dev.beast.mods.shimmer.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import dev.latvian.mods.kmath.color.Color;

public record ColorIcon(Color color) implements Icon {
	public static final SimpleRegistryType<ColorIcon> TYPE = SimpleRegistryType.dynamic(Shimmer.id("color"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.fieldOf("color").forGetter(ColorIcon::color)
	).apply(instance, ColorIcon::new)), Color.STREAM_CODEC.map(ColorIcon::new, ColorIcon::color));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
