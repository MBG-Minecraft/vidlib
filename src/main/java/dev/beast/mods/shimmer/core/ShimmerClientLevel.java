package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public interface ShimmerClientLevel extends ShimmerLevel, ShimmerClientEntityContainer {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	@Nullable
	default ActiveZones shimmer$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.shimmer$sessionData().filteredZones;
	}
}
