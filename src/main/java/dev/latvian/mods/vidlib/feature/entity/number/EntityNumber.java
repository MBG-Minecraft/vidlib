package dev.latvian.mods.vidlib.feature.entity.number;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.ToDoubleFunction;

public interface EntityNumber extends SimpleRegistryEntry, ToDoubleFunction<Entity> {
	SimpleRegistry<EntityNumber> REGISTRY = SimpleRegistry.create(VidLib.id("entity_number"), c -> PlatformHelper.CURRENT.collectEntityNumbers(c));

	static SimpleRegistryType.Unit<EntityNumber> basic(String name, ToDoubleFunction<Entity> function) {
		return SimpleRegistryType.unitWithType(name, t -> new BasicEntityNumber(t, function));
	}

	SimpleRegistryType.Unit<EntityNumber> X = basic("x", Entity::getX);
	SimpleRegistryType.Unit<EntityNumber> Y = basic("y", Entity::getY);
	SimpleRegistryType.Unit<EntityNumber> Z = basic("z", Entity::getZ);
	SimpleRegistryType.Unit<EntityNumber> PITCH = basic("pitch", Entity::getXRot);
	SimpleRegistryType.Unit<EntityNumber> YAW = basic("yaw", Entity::getYRot);
	SimpleRegistryType.Unit<EntityNumber> WIDTH = basic("width", Entity::getBbWidth);
	SimpleRegistryType.Unit<EntityNumber> HEIGHT = basic("height", Entity::getBbHeight);
	SimpleRegistryType.Unit<EntityNumber> HEAD_YAW = basic("head_yaw", Entity::getYHeadRot);
	SimpleRegistryType.Unit<EntityNumber> BODY_YAW = basic("body_yaw", e -> e instanceof LivingEntity l ? l.yBodyRot : e.getYRot());
	SimpleRegistryType.Unit<EntityNumber> VISUAL_YAW = basic("visual_yaw", Entity::getVisualRotationYInDegrees);
	SimpleRegistryType.Unit<EntityNumber> BLOCK_X = basic("block_x", Entity::getBlockX);
	SimpleRegistryType.Unit<EntityNumber> BLOCK_Y = basic("block_y", Entity::getBlockY);
	SimpleRegistryType.Unit<EntityNumber> BLOCK_Z = basic("block_z", Entity::getBlockZ);
	SimpleRegistryType.Unit<EntityNumber> AIR_SUPPLY = basic("air_supply", Entity::getAirSupply);
	SimpleRegistryType.Unit<EntityNumber> MAX_AIR_SUPPLY = basic("max_air_supply", Entity::getMaxAirSupply);
	SimpleRegistryType.Unit<EntityNumber> HEALTH = basic("health", e -> e.vl$getHealth(1F));
	SimpleRegistryType.Unit<EntityNumber> MAX_HEALTH = basic("max_health", e -> e.vl$getMaxHealth(1F));
	SimpleRegistryType.Unit<EntityNumber> RELATIVE_HEALTH = basic("relative_health", e -> e.getRelativeHealth(1F));

	static FixedEntityNumber of(double value) {
		return value == 0D ? FixedEntityNumber.ZERO : value == 1D ? FixedEntityNumber.ONE : new FixedEntityNumber(value);
	}

	Codec<EntityNumber> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, EntityNumber> STREAM_CODEC = REGISTRY.streamCodec();

	static void builtinTypes(SimpleRegistryCollector<EntityNumber> registry) {
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

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}
}
