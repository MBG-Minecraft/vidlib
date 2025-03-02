package dev.beast.mods.shimmer.math.worldposition;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class TargetWorldPosition implements WorldPosition {
	public static final SimpleRegistryType.Unit<TargetWorldPosition> TYPE = SimpleRegistryType.unit(Shimmer.id("target"), new TargetWorldPosition());

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		return Objects.requireNonNull(ctx.targetPos);
	}
}
