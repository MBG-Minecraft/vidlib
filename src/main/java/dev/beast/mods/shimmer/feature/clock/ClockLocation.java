package dev.beast.mods.shimmer.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.math.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ClockLocation(
	ClockFont font,
	EntityFilter visible,
	ResourceKey<Level> dimension,
	BlockPos pos,
	float offset,
	float scale,
	Direction facing,
	String format,
	Color color,
	boolean fullbright
) {
	public static final Codec<ClockLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClockFont.KNOWN_CODEC.codec().fieldOf("font").forGetter(ClockLocation::font),
		EntityFilter.CODEC.optionalFieldOf("visible", EntityFilter.ANY.instance()).forGetter(ClockLocation::visible),
		ShimmerCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(ClockLocation::dimension),
		BlockPos.CODEC.fieldOf("pos").forGetter(ClockLocation::pos),
		Codec.FLOAT.optionalFieldOf("offset", 0F).forGetter(ClockLocation::offset),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(ClockLocation::scale),
		Direction.CODEC.fieldOf("facing").forGetter(ClockLocation::facing),
		Codec.STRING.optionalFieldOf("format", "%02d:%02d").forGetter(ClockLocation::format),
		Color.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(ClockLocation::color),
		Codec.BOOL.optionalFieldOf("fullbright", false).forGetter(ClockLocation::fullbright)
	).apply(instance, ClockLocation::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClockLocation> STREAM_CODEC = CompositeStreamCodec.of(
		ClockFont.DIRECT_STREAM_CODEC, ClockLocation::font,
		EntityFilter.STREAM_CODEC.optional(EntityFilter.ANY.instance()), ClockLocation::visible,
		ShimmerStreamCodecs.DIMENSION.optional(Level.OVERWORLD), ClockLocation::dimension,
		BlockPos.STREAM_CODEC, ClockLocation::pos,
		ByteBufCodecs.FLOAT, ClockLocation::offset,
		ByteBufCodecs.FLOAT, ClockLocation::scale,
		Direction.STREAM_CODEC, ClockLocation::facing,
		ByteBufCodecs.STRING_UTF8.optional("%02d:%02d"), ClockLocation::format,
		Color.STREAM_CODEC, ClockLocation::color,
		ByteBufCodecs.BOOL, ClockLocation::fullbright,
		ClockLocation::new
	);
}
