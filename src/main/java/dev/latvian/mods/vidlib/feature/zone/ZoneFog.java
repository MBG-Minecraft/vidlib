package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ZoneFog(Color color, double distance) {
	public static final ZoneFog EMPTY = new ZoneFog(Color.TRANSPARENT, 0D);

	public static final Codec<ZoneFog> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Color.CODEC.optionalFieldOf("color", Color.TRANSPARENT).forGetter(ZoneFog::color),
		Codec.DOUBLE.optionalFieldOf("distance", 0D).forGetter(ZoneFog::distance)
	).apply(instance, ZoneFog::new));

	public static final StreamCodec<ByteBuf, ZoneFog> STREAM_CODEC = CompositeStreamCodec.of(
		Color.STREAM_CODEC, ZoneFog::color,
		ByteBufCodecs.DOUBLE, ZoneFog::distance,
		ZoneFog::new
	);
}
