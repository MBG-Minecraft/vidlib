package dev.beast.mods.shimmer.math;

import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public record DistanceComparator<T>(Vec3 origin, PositionGetter<T> position) implements Comparator<T> {
	@Override
	public int compare(T a, T b) {
		var aDist = origin.distanceToSqr(position.get(a));
		var bDist = origin.distanceToSqr(position.get(b));
		return Double.compare(bDist, aDist);
	}
}
