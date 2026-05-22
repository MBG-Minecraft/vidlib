package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public record SimpleColorIcon(Color color) implements ColorIcon {
	public static final SimpleRegistryType<SimpleColorIcon> TYPE = SimpleRegistryType.dynamic("color", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.fieldOf("color").forGetter(SimpleColorIcon::color)
	).apply(instance, SimpleColorIcon::new)), Color.STREAM_CODEC.map(SimpleColorIcon::new, SimpleColorIcon::color));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
