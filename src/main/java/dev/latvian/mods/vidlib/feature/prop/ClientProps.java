package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.gl.GLDebugLog;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.imgui.PropExplorerPanel;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.misc.VLFlashbackIntegration;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClientProps extends Props<ClientLevel> {
	public static final ImBoolean VISIBLE = new ImBoolean(true);

	@AutoInit(AutoInit.Type.CHUNKS_RENDERED)
	public static void chunksRendered(ClientLevel level) {
		level.getProps().reloadAll();
	}

	public final PropList assetProps;
	public final Map<RenderLevelStageEvent.Stage, List<RenderedProp<?>>> stages;
	private final List<PropRenderContext<?>> sortedProps;

	public ClientProps(ClientLevel level) {
		super(level);
		this.propLists.put(PropListType.ASSETS, assetProps = new PropList(this, PropListType.ASSETS));
		this.stages = new Reference2ObjectOpenHashMap<>();
		this.sortedProps = new ArrayList<>();
	}

	@Override
	public void add(Prop prop) {
		super.add(prop);
	}

	@Override
	protected void onAdded(Prop prop) {
		var renderer = PropRenderer.ALL.get().getOrDefault(prop.type, PropRenderer.INVISIBLE);

		if (renderer == PropRenderer.INVISIBLE) {
			return;
		}

		var renderedProp = new RenderedProp(prop, renderer);

		for (var stage : renderer.getStages(Cast.to(prop))) {
			stages.computeIfAbsent(stage, k -> new LinkedList<>()).add(renderedProp);
		}
	}

	public void reloadAll() {
		stages.clear();

		for (var list : propLists.values()) {
			for (var prop : list) {
				onAdded(prop);
			}
		}
	}

	@Override
	public void tick() {
		if (VLFlashbackIntegration.ENABLED && !VLFlashbackIntegration.RECORDED_PROPS.isEmpty()) {
			var now = level.getGameTime();
			var ops = level.jsonOps();

			for (var existing : levelProps) {
				var p = VLFlashbackIntegration.RECORDED_PROPS.get(existing.id);

				if (p == null || !p.exists(now)) {
					existing.remove(PropRemoveType.TIME_TRAVEL);
				}
			}

			for (var p : VLFlashbackIntegration.RECORDED_PROPS.values()) {
				var existing = levelProps.get(p.id());

				if (p.exists(now)) {
					if (existing == null) {
						create(context(p.type(), PropSpawnType.GAME, p.spawn()), true, true, null, null, prop -> {
							prop.id = p.id();
							prop.setDataJson(ops, p.data());
							prop.tick = (int) (now - p.spawn());
						});
					}
				} else if (existing != null) {
					existing.remove(PropRemoveType.TIME_TRAVEL);
				}
			}
		}

		super.tick();
	}

	public void renderAll(FrameInfo frame, PoseStack ms) {
		var cam = frame.camera().getPosition();
		float delta = frame.worldDelta();

		var list = stages.get(frame.stage());

		if (list == null || list.isEmpty()) {
			return;
		}

		GLDebugLog.pushGroup("[VidLib] Render Props " + frame.stage());
		var props = list.iterator();

		while (props.hasNext()) {
			var renderedProp = props.next();
			var prop = renderedProp.prop();
			var renderer = renderedProp.renderer();

			if (prop.isRemoved()) {
				props.remove();
				continue;
			} else if (!VISIBLE.get() || prop.isTimeTraveling(frame.gameTime())) {
				continue;
			} else if (PropExplorerPanel.HIDDEN_PROPS.contains(prop.id) || PropExplorerPanel.HIDDEN_PROP_TYPES.contains(prop.type)) {
				continue;
			}

			double x = KMath.lerp(delta, prop.prevPos.x, prop.pos.x);
			double y = KMath.lerp(delta, prop.prevPos.y, prop.pos.y);
			double z = KMath.lerp(delta, prop.prevPos.z, prop.pos.z);
			double r = prop.getMaxRenderDistance();
			double cd = cam.distanceToSqr(x, y + prop.height / 2D, z);

			if (r >= Double.MAX_VALUE || cd <= r * r) {
				if (prop.isVisible(x, y, z, frame)) {
					var ctx = new PropRenderContext(prop, renderer, x, y, z, cd, ms, frame, delta);

					if (renderer.shouldSort(ctx)) {
						sortedProps.add(ctx);
					} else {
						GLDebugLog.pushGroup("[VidLib] " + prop);
						ctx.render();
						GLDebugLog.popGroup();
					}
				}
			}
		}

		if (!sortedProps.isEmpty()) {
			if (sortedProps.size() >= 2) {
				sortedProps.sort(PropRenderContext.COMPARATOR);
			}

			for (var p : sortedProps) {
				GLDebugLog.pushGroup("[VidLib] " + p.prop());
				p.render();
				GLDebugLog.popGroup();
			}

			sortedProps.clear();
		}

		GLDebugLog.popGroup();
	}

	public void renderDebug(FrameInfo frame) {
		var ms = frame.poseStack();
		var cam = frame.camera().getPosition();
		var delta = frame.worldDelta();

		for (var list : propLists.values()) {
			for (var prop : list) {
				double x = KMath.lerp(delta, prop.prevPos.x, prop.pos.x);
				double y = KMath.lerp(delta, prop.prevPos.y, prop.pos.y);
				double z = KMath.lerp(delta, prop.prevPos.z, prop.pos.z);
				double r = prop.getMaxRenderDistance();

				if (r >= Double.MAX_VALUE || cam.distanceToSqr(x, y + prop.height / 2D, z) <= r * r) {
					if (prop.isVisible(x, y, z, frame)) {
						boolean selected = PropExplorerPanel.OPEN_PROPS.contains(prop.id);

						if (selected || frame.mc().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
							var visuals = prop.getDebugVisuals(x, y, z, selected);
							MiscClientUtils.renderVisuals(ms, cam, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, visuals, prop.getDebugVisualsProgress(delta));
						}
					}
				}
			}
		}
	}
}
