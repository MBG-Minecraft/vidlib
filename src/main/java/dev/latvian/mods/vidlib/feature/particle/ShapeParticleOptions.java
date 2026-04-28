package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShapeParticleOptions(int lifespan, Shape shape, Gradient color, Gradient outlineColor, RenderLightLayer lightLayer) implements ParticleOptions {
	public static final MapCodec<ShapeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 40).forGetter(ShapeParticleOptions::lifespan),
		Shape.CODEC.optionalFieldOf("shape", CubeShape.UNIT).forGetter(ShapeParticleOptions::shape),
		Gradient.CODEC.optionalFieldOf("color", Color.CYAN).forGetter(ShapeParticleOptions::color),
		Gradient.CODEC.optionalFieldOf("outline_color", Color.WHITE).forGetter(ShapeParticleOptions::outlineColor),
		RenderLightLayer.DATA_TYPE.codec().optionalFieldOf("light_layer", RenderLightLayer.NORMAL).forGetter(ShapeParticleOptions::lightLayer)
	).apply(instance, ShapeParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ShapeParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, ShapeParticleOptions::lifespan,
		Shape.STREAM_CODEC, ShapeParticleOptions::shape,
		Gradient.STREAM_CODEC, ShapeParticleOptions::color,
		Gradient.STREAM_CODEC, ShapeParticleOptions::outlineColor,
		RenderLightLayer.DATA_TYPE.streamCodec(), ShapeParticleOptions::lightLayer,
		ShapeParticleOptions::new
	);

	public ShapeParticleOptions(int lifespan, Gradient color, Gradient outlineColor) {
		this(lifespan, CubeShape.UNIT, color, outlineColor, RenderLightLayer.NORMAL);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.SHAPE.get();
	}
}
