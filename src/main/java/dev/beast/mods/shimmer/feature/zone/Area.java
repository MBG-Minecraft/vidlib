package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.latvian.mods.kmath.AAIBB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Area(ResourceKey<Level> dimension, AAIBB shape) {
	public static final Codec<Area> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ShimmerCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Area::dimension),
		AAIBB.CODEC.fieldOf("areas").forGetter(Area::shape)
	).apply(instance, Area::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Area> STREAM_CODEC = CompositeStreamCodec.of(
		ShimmerStreamCodecs.DIMENSION.optional(Level.OVERWORLD), Area::dimension,
		AAIBB.STREAM_CODEC, Area::shape,
		Area::new
	);

	public Area(ResourceKey<Level> dimension, BlockPos start, BlockPos end) {
		this(dimension, new AAIBB(start, end));
	}

	public Area(ResourceKey<Level> dimension, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this(dimension, new AAIBB(minX, minY, minZ, maxX, maxY, maxZ));
	}
}
