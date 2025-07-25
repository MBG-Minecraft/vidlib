package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record ZoneFog(Color color, double distance, Optional<Direction> direction, int steps) {
	public static final ZoneFog NONE = new ZoneFog(Color.TRANSPARENT, 0D, Optional.empty(), 0);

	public static final Codec<ZoneFog> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Color.CODEC.optionalFieldOf("color", Color.BLACK).forGetter(ZoneFog::color),
		Codec.DOUBLE.optionalFieldOf("distance", 3D).forGetter(ZoneFog::distance),
		Direction.CODEC.optionalFieldOf("direction").forGetter(ZoneFog::direction),
		Codec.INT.optionalFieldOf("steps", 7).forGetter(ZoneFog::steps)
	).apply(instance, ZoneFog::new));

	public static final StreamCodec<ByteBuf, ZoneFog> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		Color.STREAM_CODEC, ZoneFog::color,
		KLibStreamCodecs.optional(ByteBufCodecs.DOUBLE, 3D), ZoneFog::distance,
		ByteBufCodecs.optional(Direction.STREAM_CODEC), ZoneFog::direction,
		ByteBufCodecs.VAR_INT, ZoneFog::steps,
		ZoneFog::new
	);

	public static final StreamCodec<ByteBuf, ZoneFog> STREAM_CODEC = KLibStreamCodecs.optional(DIRECT_STREAM_CODEC, NONE);

	public boolean isNone() {
		return steps <= 0;
	}
}
