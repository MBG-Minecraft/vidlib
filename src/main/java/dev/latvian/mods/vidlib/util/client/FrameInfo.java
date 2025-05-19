package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public record FrameInfo(
	Minecraft mc,
	LocalClientSessionData session,
	@Nullable TerrainRenderLayer layer,
	PoseStack poseStack,
	Matrix4fc projectionMatrix,
	Matrix4fc modelViewMatrix,
	Matrix4fc worldMatrix,
	int renderTick,
	DeltaTracker deltaTracker,
	float worldDelta,
	float screenDelta,
	Camera camera,
	double cameraX,
	double cameraY,
	double cameraZ,
	Frustum frustum,
	boolean replay,
	Vector3f normal
) {
	public static FrameInfo CURRENT;

	@Nullable
	private static TerrainRenderLayer renderLayer(RenderLevelStageEvent.Stage stage) {
		for (var layer : TerrainRenderLayer.ALL) {
			if (layer.neoForgeStage == stage) {
				return layer;
			}
		}

		return null;
	}

	public FrameInfo(Minecraft mc, LocalClientSessionData session, RenderLevelStageEvent event) {
		this(
			mc,
			session,
			renderLayer(event.getStage()),
			event.getPoseStack(),
			event.getProjectionMatrix(),
			event.getModelViewMatrix(),
			new Matrix4f(event.getProjectionMatrix()).mul(event.getModelViewMatrix()),
			event.getRenderTick(),
			event.getPartialTick(),
			event.getPartialTick().getGameTimeDeltaPartialTick(false),
			event.getPartialTick().getGameTimeDeltaPartialTick(true),
			event.getCamera(),
			event.getCamera().getPosition().x,
			event.getCamera().getPosition().y,
			event.getCamera().getPosition().z,
			event.getFrustum(),
			mc.player.isReplayCamera(),
			new Vector3f()
		);
	}

	public MultiBufferSource buffers() {
		return mc.renderBuffers().bufferSource();
	}

	public float x(double x) {
		return (float) (x - cameraX);
	}

	public float y(double y) {
		return (float) (y - cameraY);
	}

	public float z(double z) {
		return (float) (z - cameraZ);
	}

	public void translate(double x, double y, double z) {
		poseStack.translate(x - cameraX, y - cameraY, z - cameraZ);
	}

	public void translate(Vec3 pos) {
		translate(pos.x, pos.y, pos.z);
	}

	public boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		int f = frustum.cubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
		return f == FrustumIntersection.INSIDE || f == FrustumIntersection.INTERSECT;
	}

	public boolean isVisible(AABB aabb) {
		return aabb.isInfinite() || isVisible(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	public boolean is(TerrainRenderLayer layer) {
		return this.layer == null || this.layer == layer;
	}
}
