package dev.beast.mods.shimmer.math.worldposition;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SourceWorldPosition implements WorldPosition {
	public static final SimpleRegistryType.Unit<SourceWorldPosition> TYPE = SimpleRegistryType.unit(Shimmer.id("source"), new SourceWorldPosition());

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		return ctx.sourcePos;
	}
}
