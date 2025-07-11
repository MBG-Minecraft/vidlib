package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public record FrameInfo(
	Minecraft mc,
	LocalClientSessionData session,
	@Nullable RenderLevelStageEvent.Stage stage,
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
	Vector3f tempNormal,
	long gameTime
) implements FramePoseStack, FrustumCheck {
	public static FrameInfo CURRENT;

	@Nullable
	private static TerrainRenderLayer renderLayer(RenderLevelStageEvent.Stage stage) {
		for (var layer : dev.latvian.mods.vidlib.util.TerrainRenderLayer.ALL) {
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
			event.getStage(),
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
			new Vector3f(),
			mc.level.getGameTime()
		);
	}

	public FrameInfo(Minecraft mc, LocalClientSessionData session, FrameGraphSetupEvent event) {
		this(
			mc,
			session,
			null,
			null,
			new PoseStack(),
			event.getProjectionMatrix(),
			event.getModelViewMatrix(),
			new Matrix4f(event.getProjectionMatrix()).mul(event.getModelViewMatrix()),
			mc.levelRenderer.getTicks(),
			mc.getDeltaTracker(),
			mc.getDeltaTracker().getGameTimeDeltaPartialTick(false),
			mc.getDeltaTracker().getGameTimeDeltaPartialTick(true),
			event.getCamera(),
			event.getCamera().getPosition().x,
			event.getCamera().getPosition().y,
			event.getCamera().getPosition().z,
			event.getFrustum(),
			mc.player.isReplayCamera(),
			new Vector3f(),
			mc.level.getGameTime()
		);
	}

	public MultiBufferSource buffers() {
		return mc.renderBuffers().bufferSource();
	}

	@Override
	public boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		int f = frustum.cubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
		return f == FrustumIntersection.INSIDE || f == FrustumIntersection.INTERSECT;
	}

	public boolean is(TerrainRenderLayer layer) {
		return this.layer == null || this.layer == layer;
	}
}
