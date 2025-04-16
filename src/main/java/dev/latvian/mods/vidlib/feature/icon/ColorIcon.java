package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public record ColorIcon(Color color) implements Icon {
	public static final SimpleRegistryType<ColorIcon> TYPE = SimpleRegistryType.dynamic(VidLib.id("color"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.fieldOf("color").forGetter(ColorIcon::color)
	).apply(instance, ColorIcon::new)), Color.STREAM_CODEC.map(ColorIcon::new, ColorIcon::color));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
