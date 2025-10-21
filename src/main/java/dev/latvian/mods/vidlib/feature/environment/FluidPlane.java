package dev.latvian.mods.vidlib.feature.environment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.zone.ZoneFluid;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FluidPlane(ZoneFluid fluid, float y) {
	public static final Codec<FluidPlane> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ZoneFluid.CODEC.fieldOf("fluid").forGetter(FluidPlane::fluid),
		Codec.FLOAT.fieldOf("y").forGetter(FluidPlane::y)
	).apply(instance, FluidPlane::new));

	public static final StreamCodec<ByteBuf, FluidPlane> STREAM_CODEC = CompositeStreamCodec.of(
		ZoneFluid.STREAM_CODEC, FluidPlane::fluid,
		ByteBufCodecs.FLOAT, FluidPlane::y,
		FluidPlane::new
	);
}
