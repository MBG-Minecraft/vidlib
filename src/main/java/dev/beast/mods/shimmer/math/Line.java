package dev.beast.mods.shimmer.math;

import net.minecraft.world.phys.Vec3;

public record Line(Vec3 start, Vec3 end) {
	public double dx() {
		return end.x() - start.x();
	}

	public double dy() {
		return end.y() - start.y();
	}

	public double dz() {
		return end.z() - start.z();
	}
}
