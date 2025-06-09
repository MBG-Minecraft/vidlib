package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public record LemniscateScreenShakeType(float xScale, float yScale) implements ScreenShakeType {
	public static final SimpleRegistryType.Unit<LemniscateScreenShakeType> DEFAULT = SimpleRegistryType.unit("default_lemniscate", new LemniscateScreenShakeType(0.6F, 1F));
	public static final SimpleRegistryType.Unit<LemniscateScreenShakeType> HORIZONTAL = SimpleRegistryType.unit("horizontal", new LemniscateScreenShakeType(1F, 0F));
	public static final SimpleRegistryType.Unit<LemniscateScreenShakeType> VERTICAL = SimpleRegistryType.unit("vertical", new LemniscateScreenShakeType(0F, 1F));

	public static final SimpleRegistryType<LemniscateScreenShakeType> TYPE = SimpleRegistryType.dynamic("lemniscate", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("x_scale", 1F).forGetter(LemniscateScreenShakeType::xScale),
		Codec.FLOAT.optionalFieldOf("y_scale", 1F).forGetter(LemniscateScreenShakeType::yScale)
	).apply(instance, LemniscateScreenShakeType::new)), CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, LemniscateScreenShakeType::xScale,
		ByteBufCodecs.FLOAT, LemniscateScreenShakeType::yScale,
		LemniscateScreenShakeType::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vector2dc get(float progress) {
		return new Vector2d(Math.cos(progress) * xScale, Math.sin(progress * 2D) * yScale);
	}
}
