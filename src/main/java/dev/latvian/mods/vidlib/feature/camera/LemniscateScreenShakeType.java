package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.codec.JOMLCodecs;
import dev.latvian.mods.klib.codec.JOMLStreamCodecs;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.UnitType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public record LemniscateScreenShakeType(@Nullable UnitType<ByteBuf, ScreenShakeType> typeOverride, Vector2fc scale) implements ScreenShakeType {
	public static final UnitType<ByteBuf, ScreenShakeType> DEFAULT = UnitType.create("default_lemniscate", type -> new LemniscateScreenShakeType(type, new Vector2f(0.6F, 1F)));
	public static final UnitType<ByteBuf, ScreenShakeType> HORIZONTAL = UnitType.create("horizontal", type -> new LemniscateScreenShakeType(type, new Vector2f(1F, 0F)));
	public static final UnitType<ByteBuf, ScreenShakeType> VERTICAL = UnitType.create("vertical", type -> new LemniscateScreenShakeType(type, new Vector2f(0F, 1F)));

	public static final DynamicType<ByteBuf, ScreenShakeType> TYPE = DynamicType.create(
		"lemniscate",
		"scale",
		JOMLCodecs.VEC2SC,
		JOMLStreamCodecs.VEC2SC,
		LemniscateScreenShakeType::new,
		LemniscateScreenShakeType::scale
	);

	public LemniscateScreenShakeType(Vector2fc scale) {
		this(null, scale);
	}

	@Override
	public CustomRegistryType<ByteBuf, ScreenShakeType> type() {
		return typeOverride == null ? TYPE : typeOverride;
	}

	@Override
	public Vector2dc get(float progress) {
		return new Vector2d(Math.cos(progress) * scale.x(), Math.sin(progress * 2D) * scale.y());
	}
}
