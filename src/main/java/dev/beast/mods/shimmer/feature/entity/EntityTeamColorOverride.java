package dev.beast.mods.shimmer.feature.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EntityTeamColorOverride {
	@Nullable
	Integer getEntityTeamColor(Entity entity);
}
