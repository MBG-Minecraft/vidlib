package dev.latvian.mods.vidlib.util.client;

import net.minecraft.world.phys.AABB;

public interface FrustumCheck {
	boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

	default boolean isVisible(AABB aabb) {
		return aabb.isInfinite() || isVisible(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}
}
