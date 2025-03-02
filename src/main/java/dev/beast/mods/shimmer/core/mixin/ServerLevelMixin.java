package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerServerLevel;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ShimmerServerLevel {
	@Unique
	private ActiveZones shimmer$activeZones;

	@Override
	@Nullable
	public ActiveZones shimmer$getActiveZones() {
		return shimmer$activeZones;
	}

	@Override
	public void shimmer$setActiveZones(ActiveZones zones) {
		shimmer$activeZones = zones;
	}
}
