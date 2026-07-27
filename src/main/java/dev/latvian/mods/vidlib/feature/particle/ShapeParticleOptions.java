package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShapeParticleOptions(int lifespan, Ref<Shape> shape, Ref<Gradient> color, Ref<Gradient> outlineColor, RenderLightLayer lightLayer) implements ParticleOptions {
	public static final MapCodec<ShapeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 40).forGetter(ShapeParticleOptions::lifespan),
		Shape.CODEC.optionalFieldOf("shape", CubeShape.UNIT_CUBE).forGetter(ShapeParticleOptions::shape),
		Gradient.CODEC.optionalFieldOf("color", Color.CYAN.toGradient().ref()).forGetter(ShapeParticleOptions::color),
		Gradient.CODEC.optionalFieldOf("outline_color", Gradient.WHITE).forGetter(ShapeParticleOptions::outlineColor),
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

	public ShapeParticleOptions(int lifespan, Ref<Gradient> color, Ref<Gradient> outlineColor) {
		this(lifespan, CubeShape.UNIT_CUBE, color, outlineColor, RenderLightLayer.NORMAL);
	}

	public ShapeParticleOptions(int lifespan, Color color, Color outlineColor) {
		this(lifespan, color.toGradient().ref(), outlineColor.toGradient().ref());
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.SHAPE.get();
	}
}
