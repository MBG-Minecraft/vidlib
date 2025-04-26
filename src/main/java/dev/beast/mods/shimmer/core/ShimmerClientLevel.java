package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.data.InternalServerData;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.prop.ClientPropList;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ShimmerClientLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	default ClientLevel shimmer$level() {
		return (ClientLevel) this;
	}

	@Override
	default ClientPropList getProps() {
		throw new NoMixinException(this);
	}

	@Override
	@Nullable
	default ActiveZones shimmer$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.shimmer$sessionData().filteredZones;
	}

	default void environmentEffects(Minecraft mc, BlockPos pos) {
		var override = EntityOverride.ENVIRONMENT_EFFECTS.get(mc.player, List.of(), InternalServerData.ENVIRONMENT_EFFECTS);
		var level = shimmer$level();

		if (override != null && !override.isEmpty()) {
			for (var effect : override) {
				if (level.random.nextFloat() <= effect.chance()) {
					level.addParticle(
						effect.particle(),
						pos.getX() + level.random.nextDouble(),
						pos.getY() + level.random.nextDouble(),
						pos.getZ() + level.random.nextDouble(),
						0.0, 0.0, 0.0
					);
				}
			}
		}
	}

	@Override
	default boolean isReplayLevel() {
		var mc = Minecraft.getInstance();
		return mc.player != null && mc.player.isReplayCamera();
	}

	@Override
	default Iterable<Entity> allEntities() {
		return shimmer$level().entitiesForRendering();
	}
}
