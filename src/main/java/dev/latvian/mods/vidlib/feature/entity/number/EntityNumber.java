package dev.latvian.mods.vidlib.feature.entity.number;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToDoubleFunction;

public interface EntityNumber extends ToDoubleFunction<Entity> {
	CustomRegistry<RegistryFriendlyByteBuf, EntityNumber> REGISTRY = CustomRegistry.<RegistryFriendlyByteBuf, EntityNumber>builder()
		.keys(ID.vidlib("entity_number"), VidLib.ID)
		.type(EntityNumber::type)
		.build();

	static CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> basic(String name, ToDoubleFunction<Entity> function) {
		return REGISTRY.unitWithType(ID.vidlib(name), t -> new BasicEntityNumber(t, function));
	}

	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> X = basic("x", Entity::getX);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> Y = basic("y", Entity::getY);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> Z = basic("z", Entity::getZ);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> PITCH = basic("pitch", Entity::getXRot);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> YAW = basic("yaw", Entity::getYRot);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> WIDTH = basic("width", Entity::getBbWidth);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> HEIGHT = basic("height", Entity::getBbHeight);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> HEAD_YAW = basic("head_yaw", Entity::getYHeadRot);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> BODY_YAW = basic("body_yaw", e -> e instanceof LivingEntity l ? l.yBodyRot : e.getYRot());
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> VISUAL_YAW = basic("visual_yaw", Entity::getVisualRotationYInDegrees);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> BLOCK_X = basic("block_x", Entity::getBlockX);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> BLOCK_Y = basic("block_y", Entity::getBlockY);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> BLOCK_Z = basic("block_z", Entity::getBlockZ);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> AIR_SUPPLY = basic("air_supply", Entity::getAirSupply);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> MAX_AIR_SUPPLY = basic("max_air_supply", Entity::getMaxAirSupply);
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> HEALTH = basic("health", e -> e.vl$getHealth(1F));
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> MAX_HEALTH = basic("max_health", e -> e.vl$getMaxHealth(1F));
	CustomRegistryType.Unit<RegistryFriendlyByteBuf, EntityNumber> RELATIVE_HEALTH = basic("relative_health", e -> e.getRelativeHealth(1F));

	static FixedEntityNumber of(double value) {
		return value == 0D ? FixedEntityNumber.ZERO : value == 1D ? FixedEntityNumber.ONE : new FixedEntityNumber(value);
	}

	Codec<Ref<EntityNumber>> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Ref<EntityNumber>> STREAM_CODEC = REGISTRY.streamCodec();

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, EntityNumber> registry) {
		registry.register(FixedEntityNumber.TYPE);
		registry.register(X);
		registry.register(Y);
		registry.register(Z);
		registry.register(PITCH);
		registry.register(YAW);
		registry.register(WIDTH);
		registry.register(HEIGHT);
		registry.register(HEAD_YAW);
		registry.register(BODY_YAW);
		registry.register(VISUAL_YAW);
		registry.register(BLOCK_X);
		registry.register(BLOCK_Y);
		registry.register(BLOCK_Z);
		registry.register(AIR_SUPPLY);
		registry.register(MAX_AIR_SUPPLY);
		registry.register(HEALTH);
		registry.register(MAX_HEALTH);
		registry.register(RELATIVE_HEALTH);
	}

	@Nullable
	default CustomRegistryType<RegistryFriendlyByteBuf, EntityNumber> type() {
		return null;
	}
}
