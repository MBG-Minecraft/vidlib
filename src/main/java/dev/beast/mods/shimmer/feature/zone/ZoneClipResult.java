package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.math.Line;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ZoneClipResult(ZoneInstance instance, ZoneShape shape, double distanceSq, @Nullable Vec3 pos, Object result) {
	public static ZoneClipResult of(ZoneInstance instance, ZoneShape shape, Line ray, HitResult hit) {
		return new ZoneClipResult(instance, shape, hit.getLocation().distanceToSqr(ray.start()), hit.getLocation(), hit);
	}

	public static ZoneClipResult of(ZoneInstance instance, ZoneShape shape, double distanceSq) {
		return new ZoneClipResult(instance, shape, distanceSq, null, null);
	}
}
