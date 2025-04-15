package dev.beast.mods.shimmer.util;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public record FrameInfo(
	Minecraft mc,
	ShimmerLocalClientSessionData session,
	PoseStack poseStack,
	Matrix4f projectionMatrix,
	Matrix4f modelViewMatrix,
	int renderTick,
	DeltaTracker deltaTracker,
	float worldDelta,
	float screenDelta,
	Camera camera,
	double cameraX,
	double cameraY,
	double cameraZ,
	Frustum frustum,
	boolean replay
) {
	public FrameInfo(Minecraft mc, ShimmerLocalClientSessionData session, RenderLevelStageEvent event) {
		this(
			mc,
			session,
			event.getPoseStack(),
			event.getProjectionMatrix(),
			event.getModelViewMatrix(),
			event.getRenderTick(),
			event.getPartialTick(),
			event.getPartialTick().getGameTimeDeltaPartialTick(false),
			event.getPartialTick().getGameTimeDeltaPartialTick(true),
			event.getCamera(),
			event.getCamera().getPosition().x,
			event.getCamera().getPosition().y,
			event.getCamera().getPosition().z,
			event.getFrustum(),
			mc.player.isReplayCamera()
		);
	}

	public Matrix4f worldMatrix() {
		return new Matrix4f(projectionMatrix).mul(modelViewMatrix);
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
}
