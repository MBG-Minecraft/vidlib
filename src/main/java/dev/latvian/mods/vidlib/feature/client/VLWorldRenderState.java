package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.icon.renderer.IconRenderer;
import dev.latvian.mods.klib.icon.renderer.IconWorldExactContext;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.render.Submittable;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.visual.renderer.VisualExactContext;
import dev.latvian.mods.klib.visual.renderer.VisualRenderer;
import dev.latvian.mods.vidlib.feature.data.InternalGlobalData;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.decal.DecalCanvas;
import dev.latvian.mods.vidlib.feature.decal.DecalTexture;
import dev.latvian.mods.vidlib.feature.decal.DecalType;
import dev.latvian.mods.vidlib.feature.environment.FluidPlaneRenderState;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderState;
import dev.latvian.mods.vidlib.integration.iris.IrisIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class VLWorldRenderState {
	public final List<Submittable> submittable = new ArrayList<>();
	public final List<Decal> decals = new ArrayList<>();
	public final List<PropRenderState<?>> props = new ArrayList<>();
	public final List<AAIBB> anchorVolumes = new ArrayList<>();
	public FluidPlaneRenderState fluidPlane = null;
	public final List<PhysicsParticleManager> physicsParticles = new ArrayList<>();
	public final List<ZoneRenderState> zones = new ArrayList<>();
	public final Visuals visuals = new Visuals();

	public VLWorldRenderState() {
		VLBuffers.build();
	}

	public void add(Submittable s) {
		submittable.add(s);
	}

	public void reset() {
		submittable.clear();
		decals.clear();
		props.clear();
		anchorVolumes.clear();
		fluidPlane = null;

		for (var manager : physicsParticles) {
			manager.reset();
		}

		physicsParticles.clear();
		zones.clear();
		visuals.clear();
	}

	public void extract(ExtractLevelRenderStateEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var session = ClientSessionData.of(mc);
		var levelSession = ClientLevelSessionData.of(mc.level);

		var worldDelta = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		var playerDelta = event.getDeltaTracker().getGameTimeDeltaPartialTick(!mc.level.tickRateManager().isEntityFrozen(mc.player));
		var frustum = event.getRenderState().cameraRenderState.cullFrustum;
		var cameraPos = event.getRenderState().cameraRenderState.pos;
		var cameraRotation = event.getCamera().rotation();
		var gameTime = mc.level.getGameTime();
		var numberContext = levelSession.createGlobalContext();
		numberContext.progress = 1D;

		var ctx = new VidLibExtractContext(
			mc,
			visuals,
			worldDelta,
			playerDelta,
			event.getRenderState().cameraRenderState.cullFrustum,
			event.getRenderState().cameraRenderState.pos,
			gameTime,
			numberContext
		);

		levelSession.props.extractAll(ctx);

		for (var decal : session.debugDecals) {
			if (decal.type != DecalType.NONE) {
				decals.add(decal);
			}
		}

		if (VidLibClientOptions.getShowAnchor()) {
			var anchored = levelSession.getAnchor().volumes();

			if (!anchored.isEmpty()) {
				for (var area : anchored) {
					if (event.getFrustum().isVisible(area.aabb())) {
						anchorVolumes.add(area);
					}
				}
			}

			for (var zone : levelSession.zones.zones) {
				if (zone.anchor) {
					var box = zone.volume.intBox();

					if (event.getFrustum().isVisible(box.aabb())) {
						anchorVolumes.add(box);
					}
				}
			}
		}

		fluidPlane = null;

		var fluidPlaneData = levelSession.getFluidPlane();

		if (fluidPlaneData != null) {
			fluidPlane = new FluidPlaneRenderState();
			fluidPlane.zoneFluid = fluidPlaneData.fluid();
			fluidPlane.y = fluidPlaneData.y();
		}

		var tempBlockPos = new BlockPos.MutableBlockPos();

		if (!session.dataMap.get(InternalGlobalData.HIDE_PLUMBOBS)) {
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
				}
			}
		}

		PhysicsParticleManager.extractAll(mc, frustum, cameraPos, worldDelta, physicsParticles);

		var tool = VidLibTool.of(mc.player);

		if (tool != null) {
			tool.getSecond().visuals(mc.player, tool.getFirst(), visuals, ctx.screenPartialTick());
		}

		if (VidLibClientOptions.getShowZones()) {
			var zoneWalls = VidLibClientOptions.getShowZoneWalls();
			var zoneOuterBounds = VidLibClientOptions.getShowZoneOuterBounds();

			for (var zone : levelSession.zones) {
				var state = ZoneRenderState.extract(zone, zoneWalls, zoneOuterBounds);

				if (state != null) {
					zones.add(state);
				}
			}
		}

		GhostStructure.extract(this, ctx);

		NeoForge.EVENT_BUS.post(new VidLibWorldRenderEvent(ctx));

		visuals.copyFrom(Visuals.TICK);

		if (visuals.hasAny()) {
			var visualExactContext = new VisualExactContext(
				mc,
				cameraPos,
				worldDelta,
				numberContext
			);

			for (var visual : visuals.visuals()) {
				var state = VisualRenderer.extract(visual, visualExactContext);

				if (state != null) {
					add(state);
				}
			}
		}

		DecalCanvas.count = DecalTexture.HOLDER.texture().get().update(decals, cameraPos);
		DecalCanvas.irisCompat = IrisIntegration.INSTANCE.isShaderPackInUse() ? 1 : 0;
	}

	public void submit(SubmitCustomGeometryEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var ctx = new SubmitContext(mc, event);

		if (!submittable.isEmpty()) {
			for (var s : submittable) {
				s.submit(ctx);
			}
		}

		if (!props.isEmpty()) {
			for (var state : props) {
				ctx.push(state.x, state.y, state.z);
				state.submit(ctx);
				ctx.pop();
			}
		}

		if (!anchorVolumes.isEmpty()) {
			for (var a : anchorVolumes) {
				ctx.push(a.minX(), a.minY(), a.minZ());

				ctx.submit(RenderTypes.debugFilledBox(), (pose, buffer) -> {
					var callback = pose.transform(buffer).withColor(0x50FFFF00);
					float minX = 0.5F;
					float minY = 0.5F;
					float minZ = 0.5F;
					float maxX = a.maxX() - a.minX() + 0.5F;
					float maxY = a.maxY() - a.minY() + 0.5F;
					float maxZ = a.maxZ() - a.minZ() + 0.5F;
					callback.frameQuads(minX, minY, minZ, maxX, maxY, maxZ, 1F, 1F);
				});

				ctx.submit(RenderTypes.lines(), (pose, buffer) -> {
					var callback = pose.transform(buffer).withColor(0xFFFFFF00).withLineWidth(ctx.lineWidth());
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

		if (!physicsParticles.isEmpty()) {
			for (var manager : physicsParticles) {
				manager.submit(ctx);
			}
		}

		if (!zones.isEmpty()) {
			for (var state : zones) {
				state.submit(ctx);
			}
		}
	}
}
