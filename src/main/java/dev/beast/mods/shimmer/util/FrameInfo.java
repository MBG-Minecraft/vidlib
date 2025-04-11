package dev.beast.mods.shimmer.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public record FrameInfo(
	Minecraft mc,
	PoseStack poseStack,
	Matrix4f projectionMatrix,
	Matrix4f modelViewMatrix,
	int renderTick,
	DeltaTracker deltaTracker,
	float worldDelta,
	float screenDelta,
	Camera camera,
	Frustum frustum,
	boolean replay
) {
	public FrameInfo(Minecraft mc, RenderLevelStageEvent event) {
		this(
			mc,
			event.getPoseStack(),
			event.getProjectionMatrix(),
			event.getModelViewMatrix(),
			event.getRenderTick(),
			event.getPartialTick(),
			event.getPartialTick().getGameTimeDeltaPartialTick(false),
			event.getPartialTick().getGameTimeDeltaPartialTick(true),
			event.getCamera(),
			event.getFrustum(),
			mc.player.isReplayCamera()
		);
	}

	public Matrix4f worldMatrix() {
		return new Matrix4f(projectionMatrix).mul(modelViewMatrix);
	}
}
