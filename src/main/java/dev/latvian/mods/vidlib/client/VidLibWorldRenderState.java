package dev.latvian.mods.vidlib.client;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.decal.DecalEvent;
import dev.latvian.mods.vidlib.feature.decal.DecalTexture;
import dev.latvian.mods.vidlib.feature.environment.FluidPlaneRenderState;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VidLibWorldRenderState {
	public List<Decal> decals = null;
	public List<PropRenderState<?>> props = null;
	public List<AAIBB> anchorAreas = null;
	public FluidPlaneRenderState fluidPlaneState = null;

	public void addDecal(Decal decal) {
		if (decals == null) {
			decals = new LinkedList<>();
		}

		decal.addToList(decals);
	}

	public boolean extract(ExtractLevelRenderStateEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return false;
		}

		boolean hasState = false;

		props = mc.level.getProps().extractAll(new ExtractContext(mc, this, event));

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

		var session = mc.player.vl$sessionData();

		if (session.fluidPlane != null) {
			fluidPlaneState = new FluidPlaneRenderState();
			fluidPlaneState.zoneFluid = session.fluidPlane.fluid();
			fluidPlaneState.y = session.fluidPlane.y();
			hasState = true;
		}

		NeoForge.EVENT_BUS.post(new DecalEvent(decals));

		if (decals != null) {
			hasState = true;
		}

		return hasState;
	}

	public void submit(SubmitCustomGeometryEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var levelRenderState = event.getLevelRenderState();
		var collector = event.getSubmitNodeCollector();
		var poseStack = event.getPoseStack();
		var cam = levelRenderState.cameraRenderState.pos;
		var offsetX = -cam.x;
		var offsetY = -cam.y;
		var offsetZ = -cam.z;
		var lineWidth = mc.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;

		if (props != null && !props.isEmpty()) {
			var ctx = new SubmitContext(mc, event, poseStack, collector, lineWidth);

			for (var state : props) {
				state.submitOffset(ctx, offsetX, offsetY, offsetZ);
			}
		}

		if (anchorAreas != null && !anchorAreas.isEmpty()) {
			collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buffer) -> {
				var callback = pose.transform(buffer).withColor(Color.YELLOW.withAlpha(80));

				for (var a : anchorAreas) {
					float minX = (float) (a.minX() + 0.5D - cam.x);
					float minY = (float) (a.minY() + 0.5D - cam.y);
					float minZ = (float) (a.minZ() + 0.5D - cam.z);
					float maxX = (float) (a.maxX() + 0.5D - cam.x);
					float maxY = (float) (a.maxY() + 0.5D - cam.y);
					float maxZ = (float) (a.maxZ() + 0.5D - cam.z);
					callback.frameQuads(minX, minY, minZ, maxX, maxY, maxZ, 1F, 1F);
				}
			});

			collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
				var callback = pose.transform(buffer).withColor(Color.YELLOW).withLineWidth(lineWidth);

				for (var a : anchorAreas) {
					float minX = (float) (a.minX() + 0.5D - cam.x);
					float minY = (float) (a.minY() + 0.5D - cam.y);
					float minZ = (float) (a.minZ() + 0.5D - cam.z);
					float maxX = (float) (a.maxX() + 0.5D - cam.x);
					float maxY = (float) (a.maxY() + 0.5D - cam.y);
					float maxZ = (float) (a.maxZ() + 0.5D - cam.z);
					callback.frameLines(minX, minY, minZ, maxX, maxY, maxZ, 1F, 1F);
				}
			});
		}

		if (fluidPlaneState != null) {
			// FIXME FluidPlaneRenderer.render(frame, session.fluidPlane);
		}

		if (!decals.isEmpty()) {
			var texture = DecalTexture.HOLDER.texture().get();
			// uCount = texture.update(decals, mc.gameRenderer.mainCamera().position());
			decals.clear();
		}
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
}
