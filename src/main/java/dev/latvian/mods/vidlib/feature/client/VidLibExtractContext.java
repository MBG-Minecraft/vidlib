package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.math.FrustumCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import org.joml.FrustumIntersection;

public record VidLibExtractContext(
	Minecraft mc,
	VLWorldRenderState vl,
	float delta,
	Frustum frustum,
	Vec3 cameraPos,
	long gameTime
) implements FrustumCheck {
	public VidLibExtractContext(Minecraft mc, VLWorldRenderState vl, ExtractLevelRenderStateEvent event) {
		this(
			mc,
			vl,
			event.getDeltaTracker().getGameTimeDeltaPartialTick(false),
			event.getRenderState().cameraRenderState.cullFrustum,
			event.getRenderState().cameraRenderState.pos,
			mc.level.getGameTime()
		);
	}

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
