package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.environment.KLibEnvironmentAttributes;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface VidLibEnvironmentAttributes {
	static <T> Supplier<AttributeType<T>> registerType(String path, Supplier<Codec<T>> codec) {
		return KLibEnvironmentAttributes.registerType(ID.vidlib(path), () -> AttributeType.ofNotInterpolated(codec.get()));
	}

	static <T> Supplier<EnvironmentAttribute<T>> register(String path, Supplier<AttributeType<T>> type, UnaryOperator<EnvironmentAttribute.Builder<T>> builder) {
		return KLibEnvironmentAttributes.register(ID.vidlib(path), () -> builder.apply(EnvironmentAttribute.builder(type.get())).build());
	}

	Supplier<AttributeType<SkyboxData>> SKYBOX_TYPE = registerType("skybox", () -> SkyboxData.CODEC);

	Supplier<EnvironmentAttribute<Boolean>> SUN = register("visual/sun", () -> AttributeTypes.BOOLEAN, builder -> builder
		.defaultValue(true)
		.syncable()
	);

	Supplier<EnvironmentAttribute<Boolean>> MOON = register("visual/moon", () -> AttributeTypes.BOOLEAN, builder -> builder
		.defaultValue(true)
		.syncable()
	);

	Supplier<EnvironmentAttribute<Float>> CELESTIAL_TILT = register("visual/celestial_tilt", () -> AttributeTypes.ANGLE_DEGREES, builder -> builder
		.defaultValue(0F)
		.spatiallyInterpolated()
		.syncable()
	);

	Supplier<EnvironmentAttribute<SkyboxData>> SKYBOX = register("visual/skybox", SKYBOX_TYPE, builder -> builder
		.defaultValue(SkyboxData.NONE)
		.syncable()
	);

	static void init() {
	}
}
