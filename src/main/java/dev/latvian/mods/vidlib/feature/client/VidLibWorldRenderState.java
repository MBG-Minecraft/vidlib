package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.icon.renderer.IconRenderer;
import dev.latvian.mods.klib.icon.renderer.IconWorldExactContext;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.render.Submittable;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.visual.Visual;
import dev.latvian.mods.klib.visual.renderer.VisualExactContext;
import dev.latvian.mods.klib.visual.renderer.VisualRenderer;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.environment.FluidPlaneRenderState;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ClientWorldSessionData;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Quaternionf;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VidLibWorldRenderState {
	public float worldDelta;
	public float playerDelta;
	public Frustum frustum;
	public Vec3 cameraPos;
	public Quaternionf cameraRotation;
	public long gameTime;
	public KNumberContext numberContext;
	public VisualExactContext visualExactContext;
	public Visuals visuals = new Visuals();

	public List<Submittable> submittable = null;
	public List<Decal> decals = null;
	public List<PropRenderState<?>> props = null;
	public List<AAIBB> anchorAreas = null;
	public FluidPlaneRenderState fluidPlane = null;

	public void add(Submittable s) {
		if (submittable == null) {
			submittable = new LinkedList<>();
		}

		submittable.add(s);
	}

	public void addVisual(Visual visual) {
		var state = VisualRenderer.extract(visual, visualExactContext);

		if (state != null) {
			add(state);
		}
	}

	public boolean extract(ExtractLevelRenderStateEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return false;
		}

		var session = ClientWorldSessionData.of(mc);
		var levelSession = ClientLevelSessionData.of(mc.level);

		worldDelta = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		playerDelta = event.getDeltaTracker().getGameTimeDeltaPartialTick(!mc.level.tickRateManager().isEntityFrozen(mc.player));
		frustum = event.getRenderState().cameraRenderState.cullFrustum;
		cameraPos = event.getRenderState().cameraRenderState.pos;
		cameraRotation = event.getCamera().rotation();
		gameTime = mc.level.getGameTime();
		numberContext = levelSession.createGlobalContext();
		visualExactContext = new VisualExactContext(
			mc,
			cameraPos,
			worldDelta,
			numberContext
		);

		var ctx = new VidLibExtractContext(mc, this, event);
		boolean hasState = false;

		props = levelSession.props.extractAll(ctx);

		if (props != null) {
			hasState = true;
		}

		if (VidLibClientOptions.getShowAnchor()) {
			var anchored = mc.getAnchor().volumeMap().get(mc.level.dimension());

			if (anchored != null && !anchored.isEmpty()) {
				anchorAreas = new ArrayList<>(anchored.size());

				for (var area : anchored) {
					if (event.getFrustum().isVisible(area.aabb())) {
						anchorAreas.add(area);
						hasState = true;
					}
				}
			}
		}

		if (session.fluidPlane != null) {
			fluidPlane = new FluidPlaneRenderState();
			fluidPlane.zoneFluid = session.fluidPlane.fluid();
			fluidPlane.y = session.fluidPlane.y();
			hasState = true;
		}

		var tempBlockPos = new BlockPos.MutableBlockPos();

		if (!session.dataMap.get(InternalServerData.HIDE_PLUMBOBS)) {
			for (var player : mc.level.players()) {
				if (player.isInvisibleTo(mc.player)) {
					continue;
				}

				var icon = ClientGameEngine.INSTANCE.getPlumbob(player);

				if (icon == Icon.EMPTY || player == mc.player && mc.options.getCameraType().isFirstPerson()) {
					continue;
				}

				float delta = player == mc.player ? playerDelta : worldDelta;
				var pos = player.getEyePosition(delta);

				if (Vector2d.distanceSquared(pos.x, pos.z, cameraPos.x, cameraPos.z) <= 0.01D * 0.01D) {
					continue;
				}

				var iconState = IconRenderer.extract(icon, new IconWorldExactContext(mc, cameraPos, delta, 255, null));

				if (iconState != null) {
					// var crouching = player.isCrouching();
					var scoreText = !Empty.isEmpty(ClientGameEngine.INSTANCE.getScoreText(player));

					iconState.x = pos.x;
					iconState.y = pos.y + 0.52D;
					iconState.z = pos.z;

					if (scoreText) {
						iconState.y += 0.3D;
					}

					iconState.rotation = cameraRotation;
					iconState.scale = 0.4F;

					tempBlockPos.set(pos.x, pos.y, pos.z);
					iconState.light = BlockUtils.getPackedLight(mc.level, tempBlockPos);

					add(iconState);
					hasState = true;
				}
			}
		}

		NeoForge.EVENT_BUS.post(new VidLibWorldRenderEvent(ctx));

		if (submittable != null && !submittable.isEmpty()) {
			hasState = true;
		}

		return hasState;
	}

	public void submit(SubmitCustomGeometryEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var ctx = new SubmitContext(mc, event);

		if (submittable != null) {
			for (var s : submittable) {
				s.submit(ctx);
			}
		}

		if (props != null && !props.isEmpty()) {
			for (var state : props) {
				ctx.push(state.x, state.y, state.z);
				state.submit(ctx);
				ctx.pop();
			}
		}

		if (anchorAreas != null && !anchorAreas.isEmpty()) {
			for (var a : anchorAreas) {
				ctx.push(a.minX(), a.minY(), a.minZ());

				ctx.submit(RenderTypes.debugFilledBox(), (pose, buffer) -> {
					var callback = pose.transform(buffer).withColor(Color.YELLOW.withAlpha(80));
					float minX = 0.5F;
					float minY = 0.5F;
					float minZ = 0.5F;
					float maxX = a.maxX() - a.minX() + 0.5F;
					float maxY = a.maxY() - a.minY() + 0.5F;
					float maxZ = a.maxZ() - a.minZ() + 0.5F;
					callback.frameQuads(minX, minY, minZ, maxX, maxY, maxZ, 1F, 1F);
				});

				ctx.submit(RenderTypes.lines(), (pose, buffer) -> {
					var callback = pose.transform(buffer).withColor(Color.YELLOW).withLineWidth(ctx.lineWidth());
					float minX = 0.5F;
					float minY = 0.5F;
					float minZ = 0.5F;
					float maxX = a.maxX() - a.minX() + 0.5F;
					float maxY = a.maxY() - a.minY() + 0.5F;
					float maxZ = a.maxZ() - a.minZ() + 0.5F;
					callback.frameLines(minX, minY, minZ, maxX, maxY, maxZ, 1F, 1F);
				});

				ctx.pop();
			}
		}

		if (fluidPlane != null) {
			// FIXME FluidPlaneRenderer.render(frame, session.fluidPlane);
		}

		/* FIXME
		if (!decals.isEmpty()) {
			var texture = DecalTexture.HOLDER.texture().get();
			// uCount = texture.update(decals, mc.gameRenderer.mainCamera().position());
			decals.clear();
		}
		 */
	}

	/*
	public static void renderWorld(FrameInfo frame) {
		var mc = frame.mc();
		var event = frame.stage();

		if (mc.level == null || mc.player == null || event == null) {
			return;
		}

		FrameInfo.CURRENT = frame;
		var session = frame.session();

		if (frame.layer() == TerrainRenderLayer.PARTICLE) {
			if (VidLibClientOptions.getShowZones()) {
				ZoneRenderer.renderAll(frame);
			} else {
				ZoneRenderer.renderSolid(frame);
			}

			if (!session.serverDataMap.get(InternalServerData.HIDE_PLUMBOBS)) {
				PlumbobRenderer.render(mc, frame);
			}

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				tool.getSecond().visuals(mc.player, tool.getFirst(), Visuals.TEMP, frame.screenDelta());
				MiscClientUtils.renderVisuals(frame.poseStack(), frame.camera().position(), frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Visuals.TEMP, 1F);
				Visuals.TEMP.clear();
			}

			if (mc.debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES) || !ClientProps.OPEN_PROPS.isEmpty()) {
				mc.level.getProps().renderDebug(frame);
				ClientProps.OPEN_PROPS.clear();
			}

			Visuals.FRAME_DEBUG.copyFrom(Visuals.TICK_DEBUG);

			if (Visuals.FRAME_DEBUG.hasAny()) {
				MiscClientUtils.renderVisuals(frame.poseStack(), frame.camera().position(), frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Visuals.FRAME_DEBUG, 1F);
				Visuals.FRAME_DEBUG.clear();
			}
		}

		if (frame.layer() != null) {
			GhostStructure.render(frame);

			var tool = VidLibTool.of(mc.player);
			if (tool != null) {
				tool.getSecond().visuals(mc.player, tool.getFirst(), Visuals.TEMP, frame.screenDelta());

				for (var cube : Visuals.TEMP.texturedCubes()) {
					TexturedCubeRenderer.render(frame, LightUV.FULLBRIGHT, cube, Color.WHITE);
				}

				Visuals.TEMP.clear();
			}

			if (!VidLibClientOptions.getShowZones()) {
				ZoneRenderer.renderVisible(frame);
			}

			PhysicsParticleManager.render(frame);
		}
	}
	 */

	/*
	private static int uCount = 0;

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(ID.vidlib("decals"), builder -> {
		builder.setDrawSetupCallback(DecalRenderer::setup);
		builder.addUniform(CanvasUniform.int1("Count", () -> uCount));
		builder.addUniform(CanvasUniform.mat4("InverseViewProjectionMat", () -> ClientMatrices.INVERSE_WORLD));
		builder.addUniform(CanvasUniform.int1("NoSceneSample", () -> IrisIntegration.INSTANCE.isShaderPackInUse() ? 1 : 0));
	});

	if (!TEMP_LIST.isEmpty()) {
			var texture = DecalTexture.HOLDER.texture().get();
			uCount = texture.update(TEMP_LIST, mc.gameRenderer.mainCamera().position());
			// CANVAS.markActive();
			TEMP_LIST.clear();
		}
	 */
}
