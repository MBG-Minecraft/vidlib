package dev.latvian.mods.vidlib.feature.environment;

import dev.latvian.mods.klib.math.KMath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record WorldBorderOverride(long time, Vec3 pos, double size) {
	public WorldBorderOverride lerp(double t, WorldBorderOverride end) {
		if (time >= end.time) {
			return end;
		} else if (t <= time) {
			return this;
		} else if (t >= end.time) {
			return end;
		}

		double d = KMath.map(t, time, end.time, 0D, 1D);
		return new WorldBorderOverride(0L, pos.lerp(end.pos, d), Mth.lerp(d, size, end.size));
	}
}
