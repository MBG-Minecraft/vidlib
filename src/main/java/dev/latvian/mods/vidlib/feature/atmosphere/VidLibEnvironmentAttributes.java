package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.environment.KLibEnvironmentAttributes;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface VidLibEnvironmentAttributes {
	static <T> Supplier<AttributeType<T>> registerType(String path, Supplier<AttributeType<T>> value) {
		return KLibEnvironmentAttributes.registerType(ID.vidlib(path), () -> value.get());
	}

	static <T> Supplier<AttributeType<T>> registerUninterpolatedType(String path, Supplier<Codec<T>> codec) {
		return KLibEnvironmentAttributes.registerType(ID.vidlib(path), () -> AttributeType.ofNotInterpolated(codec.get()));
	}

	static <T> Supplier<EnvironmentAttribute<T>> register(String path, Supplier<AttributeType<T>> type, UnaryOperator<EnvironmentAttribute.Builder<T>> builder) {
		return KLibEnvironmentAttributes.register(ID.vidlib(path), () -> builder.apply(EnvironmentAttribute.builder(type.get())).build());
	}

	Supplier<AttributeType<SkyboxData>> SKYBOX_TYPE = registerUninterpolatedType("skybox", () -> SkyboxData.CODEC);

	Supplier<AttributeType<List<Ref<ScreenEffect>>>> SCREEN_EFFECTS_TYPE = registerType("screen_effects", () -> AttributeType.ofInterpolated(
		ScreenEffect.CODEC.listOf(),
		AttributeModifier.listLibrary(),
		LerpFunction.ofListCrossFade((effect, alpha) -> effect.value().withAlpha(alpha).ref())
	));

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

	Supplier<EnvironmentAttribute<List<Ref<ScreenEffect>>>> SCREEN_EFFECTS = register("visual/screen_effects", SCREEN_EFFECTS_TYPE, builder -> builder
		.defaultValue(List.of())
		.syncable()
	);

	static void init() {
	}
}
