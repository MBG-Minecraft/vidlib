package dev.beast.mods.shimmer.math;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class CurvedPath {
	public final Vec3[] path;

	public CurvedPath(BlockPos... b) {
		var list = new ArrayList<Vec3>();
		var center = new Vec3[b.length];

		for (int i = 0; i < b.length; i++) {
			center[i] = Vec3.atCenterOf(b[i]);
		}

		for (int i = 0; i < b.length; i++) {
			var p1 = center[i];
			var p2 = center[(i + 1) % b.length];

			var p3 = center[(i + 2) % b.length];
			double inc = 0.4D / p1.distanceTo(p2);

			for (double t = 0D; t < 0.5D; t += inc) {
				double x = KMath.curve(t, p1.x, p2.x, p3.x);
				double z = KMath.curve(t, p1.z, p2.z, p3.z);
				list.add(new Vec3(x, Mth.lerp(t, p1.y, p2.y), z));
			}
		}

		path = list.toArray(new Vec3[0]);
	}

	public Vec3 get(int i) {
		return path[(i < 0 ? (i + path.length) : i) % path.length];
	}
}
