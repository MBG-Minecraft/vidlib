package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.util.ScreenCorner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ScreenClock(
	EntityFilter visible,
	ScreenCorner location,
	String format,
	Color color
) {
	public static final Codec<ScreenClock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		EntityFilter.CODEC.optionalFieldOf("visible", EntityFilter.ANY.instance()).forGetter(ScreenClock::visible),
		ScreenCorner.REGISTERED_DATA_TYPE.type().codec().optionalFieldOf("location", ScreenCorner.BOTTOM_LEFT).forGetter(ScreenClock::location),
		Codec.STRING.optionalFieldOf("format", "%02d:%02d").forGetter(ScreenClock::format),
		Color.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(ScreenClock::color)
	).apply(instance, ScreenClock::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ScreenClock> STREAM_CODEC = CompositeStreamCodec.of(
		EntityFilter.STREAM_CODEC.optional(EntityFilter.ANY.instance()), ScreenClock::visible,
		ScreenCorner.REGISTERED_DATA_TYPE.type().streamCodec(), ScreenClock::location,
		ByteBufCodecs.STRING_UTF8.optional("%02d:%02d"), ScreenClock::format,
		Color.STREAM_CODEC, ScreenClock::color,
		ScreenClock::new
	);
}
