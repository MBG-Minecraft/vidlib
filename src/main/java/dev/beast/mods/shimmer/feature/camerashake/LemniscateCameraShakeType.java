package dev.beast.mods.shimmer.feature.camerashake;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Vec2d;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;

public record LemniscateCameraShakeType(float xScale, float yScale) implements CameraShakeType {
	public static final SimpleRegistryType.Unit<LemniscateCameraShakeType> DEFAULT = SimpleRegistryType.unit(Shimmer.id("default_lemniscate"), new LemniscateCameraShakeType(1F, 1F));
	public static final SimpleRegistryType.Unit<LemniscateCameraShakeType> HORIZONTAL = SimpleRegistryType.unit(Shimmer.id("horizontal"), new LemniscateCameraShakeType(1F, 0F));
	public static final SimpleRegistryType.Unit<LemniscateCameraShakeType> VERTICAL = SimpleRegistryType.unit(Shimmer.id("vertical"), new LemniscateCameraShakeType(0F, 1F));

	public static final SimpleRegistryType<LemniscateCameraShakeType> TYPE = SimpleRegistryType.dynamic(Shimmer.id("lemniscate"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("x_scale", 1F).forGetter(LemniscateCameraShakeType::xScale),
		Codec.FLOAT.optionalFieldOf("y_scale", 1F).forGetter(LemniscateCameraShakeType::yScale)
	).apply(instance, LemniscateCameraShakeType::new)), CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, LemniscateCameraShakeType::xScale,
		ByteBufCodecs.FLOAT, LemniscateCameraShakeType::yScale,
		LemniscateCameraShakeType::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec2d get(float progress) {
		return new Vec2d(Math.cos(progress) * xScale, Math.sin(progress * 2D) * yScale);
	}
}
