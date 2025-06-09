package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClientProps extends Props<ClientLevel> {
	private record SortedProp(Prop prop, PropRenderer<?> renderer, double x, double y, double z, double distance) {
		public int compareTo(SortedProp b) {
			return Double.compare(b.distance, distance);
		}
	}

	public final Map<PropListType, Map<RenderLevelStageEvent.Stage, Int2ObjectMap<Prop>>> renderedPropLists;
	private final List<SortedProp> sortedProps;

	public ClientProps(ClientLevel level) {
		super(level);
		this.propLists.put(PropListType.ASSETS, new PropList(this, PropListType.ASSETS));
		this.renderedPropLists = new EnumMap<>(PropListType.class);
		this.sortedProps = new ArrayList<>();
	}

	public PropRenderer<?> getRenderer(Prop prop) {
		if (prop.cachedRenderer == null) {
			prop.cachedRenderer = PropRenderer.ALL.get().getOrDefault(prop.type, PropRenderer.INVISIBLE);
		}

		return (PropRenderer<?>) prop.cachedRenderer;
	}

	@Override
	protected void onAdded(Prop prop) {
		var renderedProps = renderedPropLists.computeIfAbsent(prop.spawnType.listType, k -> new Reference2ObjectOpenHashMap<>());

		for (var stage : getRenderer(prop).getStages(Cast.to(prop))) {
			renderedProps.computeIfAbsent(stage, k -> new Int2ObjectOpenHashMap<>()).put(prop.id, prop);
		}

		prop.cachedRenderer = null;
	}

	@Override
	protected void onRemoved(Prop prop) {
		prop.cachedRenderer = null;
	}

	public void renderAll(FrameInfo frame) {
		var ms = frame.poseStack();
		var cam = frame.camera().getPosition();

		for (var renderedProps : renderedPropLists.values()) {
			var list = renderedProps.get(frame.stage());

			if (list == null || list.isEmpty()) {
				continue;
			}

			var props = list.values().iterator();

			while (props.hasNext()) {
				var prop = props.next();

				if (prop.isRemoved()) {
					props.remove();
					continue;
				}

				double x = KMath.lerp(frame.worldDelta(), prop.prevPos.x, prop.pos.x);
				double y = KMath.lerp(frame.worldDelta(), prop.prevPos.y, prop.pos.y);
				double z = KMath.lerp(frame.worldDelta(), prop.prevPos.z, prop.pos.z);
				double r = prop.getMaxRenderDistance();
				double cd = cam.distanceToSqr(x, y + prop.height / 2D, z);

				if (r >= Double.MAX_VALUE || cd <= r * r) {
					if (prop.isVisible(x, y, z, frame)) {
						var renderer = getRenderer(prop);

						if (renderer != PropRenderer.INVISIBLE) {
							if (renderer.shouldSort()) {
								sortedProps.add(new SortedProp(prop, renderer, x, y, z, cd));
							} else {
								ms.pushPose();
								ms.translate(frame.x(x), frame.y(y), frame.z(z));
								renderer.renderProp(Cast.to(prop), frame);
								ms.popPose();
							}
						}
					}
				}
			}

			if (!sortedProps.isEmpty()) {
				if (sortedProps.size() >= 2) {
					sortedProps.sort(SortedProp::compareTo);
				}

				for (var p : sortedProps) {
					ms.pushPose();
					ms.translate(frame.x(p.x), frame.y(p.y), frame.z(p.z));
					p.renderer.renderProp(Cast.to(p.prop), frame);
					ms.popPose();
				}

				sortedProps.clear();
			}
		}
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
						var visuals = prop.getDebugVisuals(x, y, z);
						MiscClientUtils.renderVisuals(ms, cam, frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, visuals, prop.getDebugVisualsProgress(delta));
					}
				}
			}
		}
	}
}
