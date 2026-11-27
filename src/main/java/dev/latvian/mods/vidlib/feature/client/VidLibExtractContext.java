package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.FrustumIntersection;

public record VidLibExtractContext(
	Minecraft mc,
	Visuals visuals,
	float worldPartialTick,
	float screenPartialTick,
	Frustum frustum,
	Vec3 cameraPos,
	long gameTime,
	KNumberContext numberContext
) implements FrustumCheck {
	@Override
	public boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		var result = frustum.cubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
		return result == FrustumIntersection.INTERSECT || result == FrustumIntersection.INSIDE;
	}

	@Override
	public boolean isVisible(AABB aabb) {
		return frustum.isVisible(aabb);
	}
}
