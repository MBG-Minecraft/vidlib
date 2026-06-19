package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;
import org.joml.Vector3f;

public record FrameInfo(
	Minecraft mc,
	LocalClientSessionData session,
	@Nullable Object stage,
	@Nullable TerrainRenderLayer layer,
	PoseStack poseStack,
	int renderTick,
	DeltaTracker deltaTracker,
	float worldDelta,
	float screenDelta,
	Camera camera,
	double cameraX,
	double cameraY,
	double cameraZ,
	Frustum frustum,
	CameraRenderState cameraState,
	@Nullable SubmitNodeCollector submitNodeCollector,
	boolean replay,
	Vector3f tempNormal,
	long gameTime
) implements FramePoseStack, FrustumCheck {
	public static FrameInfo CURRENT;

	public FrameInfo(Minecraft mc, LocalClientSessionData session, RenderLevelStageEvent event) {
		this(mc, session, event, null);
	}

	public FrameInfo(Minecraft mc, LocalClientSessionData session, RenderLevelStageEvent event, @Nullable TerrainRenderLayer layer) {
		this(
			mc,
			session,
			layer == null ? event.getClass() : layer,
			layer,
			event.getPoseStack(),
			mc.levelRenderer.getTicks(),
			mc.getDeltaTracker(),
			mc.getDeltaTracker().getGameTimeDeltaPartialTick(false),
			mc.getDeltaTracker().getGameTimeDeltaPartialTick(true),
			mc.gameRenderer.getMainCamera(),
			event.getLevelRenderState().cameraRenderState.pos.x,
			event.getLevelRenderState().cameraRenderState.pos.y,
			event.getLevelRenderState().cameraRenderState.pos.z,
			event.getLevelRenderState().cameraRenderState.cullFrustum,
			event.getLevelRenderState().cameraRenderState,
			null,
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
			mc.levelRenderer.getTicks(),
			event.getDeltaTracker(),
			event.getDeltaTracker().getGameTimeDeltaPartialTick(false),
			event.getDeltaTracker().getGameTimeDeltaPartialTick(true),
			mc.gameRenderer.getMainCamera(),
			event.getCameraState().pos.x,
			event.getCameraState().pos.y,
			event.getCameraState().pos.z,
			event.getFrustum(),
			event.getCameraState(),
			null,
			mc.player.isReplayCamera(),
			new Vector3f(),
			mc.level.getGameTime()
		);
	}

	public FrameInfo(Minecraft mc, LocalClientSessionData session, SubmitCustomGeometryEvent event) {
		this(
			mc,
			session,
			SubmitCustomGeometryEvent.class,
			null,
			event.getPoseStack(),
			mc.levelRenderer.getTicks(),
			mc.getDeltaTracker(),
			mc.getDeltaTracker().getGameTimeDeltaPartialTick(false),
			mc.getDeltaTracker().getGameTimeDeltaPartialTick(true),
			mc.gameRenderer.getMainCamera(),
			event.getLevelRenderState().cameraRenderState.pos.x,
			event.getLevelRenderState().cameraRenderState.pos.y,
			event.getLevelRenderState().cameraRenderState.pos.z,
			event.getLevelRenderState().cameraRenderState.cullFrustum,
			event.getLevelRenderState().cameraRenderState,
			event.getSubmitNodeCollector(),
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
