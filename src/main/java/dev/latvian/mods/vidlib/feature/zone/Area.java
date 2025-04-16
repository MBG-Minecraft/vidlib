package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.AAIBB;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Area(ResourceKey<Level> dimension, AAIBB shape) {
	public static final Codec<Area> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		VLCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Area::dimension),
		AAIBB.CODEC.fieldOf("areas").forGetter(Area::shape)
	).apply(instance, Area::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Area> STREAM_CODEC = CompositeStreamCodec.of(
		VLStreamCodecs.DIMENSION.optional(Level.OVERWORLD), Area::dimension,
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
