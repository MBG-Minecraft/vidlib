package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.gl.GLDebugLog;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientProps extends Props<ClientLevel> {
	public static final ImBoolean VISIBLE = new ImBoolean(true);
	public static final IntSet OPEN_PROPS = new IntOpenHashSet();
	public static final IntSet HIDDEN_PROPS = new IntOpenHashSet();
	public static final Set<PropType<?>> HIDDEN_PROP_TYPES = new ReferenceOpenHashSet<>();
	public static final Visuals DEBUG_VISUALS = new Visuals();
	public static final ImBoolean HIDE_OUTLINE = new ImBoolean(true);

	public static boolean isPropHidden(Prop prop) {
		return HIDDEN_PROPS.contains(prop.id) || HIDDEN_PROP_TYPES.contains(prop.type);
	}

	@AutoInit(AutoInit.Type.CHUNKS_RENDERED)
	public static void chunksRendered(ClientLevel level) {
		level.getProps().reloadAll();
	}

	public final Map<RenderLevelStageEvent.Stage, List<RenderedProp<?>>> stages;
	private final List<PropRenderContext<?>> sortedProps;

	public ClientProps(ClientLevel level) {
		super(level);
		this.stages = new Reference2ObjectOpenHashMap<>();
		this.sortedProps = new ArrayList<>();
	}

	@Override
	public void add(Prop prop) {
		super.add(prop);
	}

	@Override
	protected void onAdded(Prop prop) {
		var rendererFactory = PropRenderer.ALL.get().get(prop.type);

		if (rendererFactory == null) {
			return;
		}

		var renderer = rendererFactory.apply(prop);

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
	public void tick(boolean tick) {
		if (RecordedProp.MAP != null && RecordedProp.LIST != null) {
			var now = level.getGameTime();

			for (var existing : levelProps) {
				if (existing.clientSideOnly) {
					continue;
				}

				var p = RecordedProp.MAP.get(existing.id);

				if (p == null || !p.exists(now)) {
					existing.remove(PropRemoveType.TIME_TRAVEL);
				}
			}

			for (var p : RecordedProp.LIST) {
				var existing = levelProps.get(p.id);

				if (p.exists(now)) {
					if (existing == null) {
						create(context(p.type, PropSpawnType.GAME, p.spawn), true, true, null, null, prop -> {
							prop.id = p.id;

							for (var entry : p.data.entrySet()) {
								prop.setData(entry.getKey(), Cast.to(entry.getValue()));
							}

							prop.tick = (int) (now - p.spawn);
						});
					}
				} else if (existing != null) {
					existing.remove(PropRemoveType.TIME_TRAVEL);
				}
			}
		}

		super.tick(tick);

		if (RecordedProp.MAP != null && RecordedProp.LIST != null && !tick) {
			levelProps.map.values().removeIf(prop -> prop.removed == PropRemoveType.TIME_TRAVEL);
		}
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
			} else if (isPropHidden(prop)) {
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
						boolean selected = !HIDE_OUTLINE.get() && OPEN_PROPS.contains(prop.id);

						if (selected || frame.mc().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
							var progress = prop.getDebugVisualsProgress(delta);

							if (progress >= 0F && progress <= 1F) {
								prop.debugVisuals(Visuals.TEMP, x, y, z, delta, selected);
								MiscClientUtils.renderVisuals(ms, cam, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Visuals.TEMP, progress);
								Visuals.TEMP.clear();
							} else {
								prop.debugVisuals(DEBUG_VISUALS, x, y, z, delta, selected);
							}
						}
					}
				}
			}
		}

		MiscClientUtils.renderVisuals(ms, cam, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, DEBUG_VISUALS, 1F);
		DEBUG_VISUALS.clear();
	}
}
