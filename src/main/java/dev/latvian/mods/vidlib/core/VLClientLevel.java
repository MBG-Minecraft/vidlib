package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface VLClientLevel extends VLLevel {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	default ClientLevel vl$level() {
		return (ClientLevel) this;
	}

	@Override
	default ClientProps getProps() {
		throw new NoMixinException(this);
	}

	@Override
	@Nullable
	default ActiveZones vl$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.vl$sessionData().filteredZones;
	}

	default void environmentEffects(Minecraft mc, BlockPos pos) {
		var override = EntityOverride.ENVIRONMENT_EFFECTS.get(mc.player);
		var level = this.vl$level();

		if (override != null && !override.isEmpty()) {
			for (var effect : override) {
				var chance = effect.chance().getOr(level.getGlobalContext(), 0D);

				if (level.random.roll((float) chance)) {
					level.addParticle(
						effect.particle(),
						pos.getX() + level.random.nextFloat(),
						pos.getY() + level.random.nextFloat(),
						pos.getZ() + level.random.nextFloat(),
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
		return vl$level().entitiesForRendering();
	}

	@Override
	default float vl$getDelta() {
		return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}
}
