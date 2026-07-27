package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ScreenCorner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ScreenClock(
	Ref<EntityFilter> visible,
	ScreenCorner location,
	String format,
	Color color,
	Color flashingColor
) {
	public static final Codec<ScreenClock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		EntityFilter.CODEC.optionalFieldOf("visible", EntityFilter.ANY).forGetter(ScreenClock::visible),
		ScreenCorner.DATA_TYPE.codec().optionalFieldOf("location", ScreenCorner.BOTTOM_LEFT).forGetter(ScreenClock::location),
		Codec.STRING.optionalFieldOf("format", "%02d:%02d").forGetter(ScreenClock::format),
		Color.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(ScreenClock::color),
		Color.SOLID_CODEC.optionalFieldOf("flashing_color", Color.of(1F, 1F, 0.3F, 0.3F)).forGetter(ScreenClock::flashingColor)
	).apply(instance, ScreenClock::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ScreenClock> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.ANY), ScreenClock::visible,
		ScreenCorner.DATA_TYPE.streamCodec(), ScreenClock::location,
		KLibStreamCodecs.optional(ByteBufCodecs.STRING_UTF8, "%02d:%02d"), ScreenClock::format,
		Color.STREAM_CODEC, ScreenClock::color,
		Color.STREAM_CODEC, ScreenClock::flashingColor,
		ScreenClock::new
	);
}
