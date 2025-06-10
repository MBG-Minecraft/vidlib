package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.math.AAIBB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public record Area(ResourceKey<Level> dimension, AAIBB shape) {
	public static final Codec<Area> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Area::dimension),
		AAIBB.CODEC.fieldOf("area").forGetter(Area::shape)
	).apply(instance, Area::new));

	public static final Codec<Area> CODEC = Codec.either(DIRECT_CODEC, AAIBB.CODEC).xmap(e -> e.map(Function.identity(), shape -> new Area(Level.OVERWORLD, shape)), area -> area.dimension == Level.OVERWORLD ? Either.right(area.shape) : Either.left(area));

	public static final StreamCodec<RegistryFriendlyByteBuf, Area> STREAM_CODEC = CompositeStreamCodec.of(
		MCStreamCodecs.DIMENSION.optional(Level.OVERWORLD), Area::dimension,
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
