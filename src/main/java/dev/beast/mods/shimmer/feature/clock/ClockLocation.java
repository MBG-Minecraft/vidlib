package dev.beast.mods.shimmer.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClockLocation(
	ClockFont font,
	BlockPos pos,
	Direction rotation,
	String format,
	Color color,
	boolean fullbright
) {
	public static final Codec<ClockLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClockFont.CODEC.fieldOf("font").forGetter(ClockLocation::font),
		BlockPos.CODEC.fieldOf("pos").forGetter(ClockLocation::pos),
		Direction.CODEC.fieldOf("rotation").forGetter(ClockLocation::rotation),
		Codec.STRING.optionalFieldOf("format", "%02d:%02d").forGetter(ClockLocation::format),
		Color.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(ClockLocation::color),
		Codec.BOOL.optionalFieldOf("fullbright", false).forGetter(ClockLocation::fullbright)
	).apply(instance, ClockLocation::new));

	public static final StreamCodec<ByteBuf, ClockLocation> STREAM_CODEC = CompositeStreamCodec.of(
		ClockFont.DIRECT_STREAM_CODEC,
		ClockLocation::font,
		BlockPos.STREAM_CODEC,
		ClockLocation::pos,
		Direction.STREAM_CODEC,
		ClockLocation::rotation,
		ByteBufCodecs.STRING_UTF8.optional("%02d:%02d"),
		ClockLocation::format,
		Color.STREAM_CODEC,
		ClockLocation::color,
		ByteBufCodecs.BOOL,
		ClockLocation::fullbright,
		ClockLocation::new
	);
}
