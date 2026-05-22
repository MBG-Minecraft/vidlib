package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public record LemniscateScreenShakeType(@Nullable SimpleRegistryType<?> typeOverride, float xScale, float yScale) implements ScreenShakeType {
	public static final SimpleRegistryType.Unit<LemniscateScreenShakeType> DEFAULT = SimpleRegistryType.unitWithType("default_lemniscate", type -> new LemniscateScreenShakeType(type, 0.6F, 1F));
	public static final SimpleRegistryType.Unit<LemniscateScreenShakeType> HORIZONTAL = SimpleRegistryType.unitWithType("horizontal", type -> new LemniscateScreenShakeType(type, 1F, 0F));
	public static final SimpleRegistryType.Unit<LemniscateScreenShakeType> VERTICAL = SimpleRegistryType.unitWithType("vertical", type -> new LemniscateScreenShakeType(type, 0F, 1F));

	public static final SimpleRegistryType<LemniscateScreenShakeType> TYPE = SimpleRegistryType.dynamic("lemniscate", RecordCodecBuilder.mapCodec(instance -> instance.group(
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
	public SimpleRegistryType<?> type() {
		return typeOverride == null ? TYPE : typeOverride;
	}

	@Override
	public Vector2dc get(float progress) {
		return new Vector2d(Math.cos(progress) * xScale, Math.sin(progress * 2D) * yScale);
	}
}
