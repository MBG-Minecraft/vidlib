package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public record LemniscateScreenShakeType(@Nullable CustomRegistryType<?> typeOverride, float xScale, float yScale) implements ScreenShakeType {
	public static final CustomRegistryType.Unit<LemniscateScreenShakeType> DEFAULT = CustomRegistryType.unitWithType("default_lemniscate", type -> new LemniscateScreenShakeType(type, 0.6F, 1F));
	public static final CustomRegistryType.Unit<LemniscateScreenShakeType> HORIZONTAL = CustomRegistryType.unitWithType("horizontal", type -> new LemniscateScreenShakeType(type, 1F, 0F));
	public static final CustomRegistryType.Unit<LemniscateScreenShakeType> VERTICAL = CustomRegistryType.unitWithType("vertical", type -> new LemniscateScreenShakeType(type, 0F, 1F));

	public static final CustomRegistryType<LemniscateScreenShakeType> TYPE = CustomRegistryType.dynamic("lemniscate", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("x_scale", 1F).forGetter(LemniscateScreenShakeType::xScale),
		Codec.FLOAT.optionalFieldOf("y_scale", 1F).forGetter(LemniscateScreenShakeType::yScale)
	).apply(instance, LemniscateScreenShakeType::new)), CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, LemniscateScreenShakeType::xScale,
		ByteBufCodecs.FLOAT, LemniscateScreenShakeType::yScale,
		LemniscateScreenShakeType::new
	));

	public LemniscateScreenShakeType(float xScale, float yScale) {
		this(null, xScale, yScale);
	}

	@Override
	public CustomRegistryType<?> type() {
		return typeOverride == null ? TYPE : typeOverride;
	}

	@Override
	public Vector2dc get(float progress) {
		return new Vector2d(Math.cos(progress) * xScale, Math.sin(progress * 2D) * yScale);
	}
}
