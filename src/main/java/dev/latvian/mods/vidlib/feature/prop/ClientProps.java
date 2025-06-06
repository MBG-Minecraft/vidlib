package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.util.Cast;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClientProps extends Props<ClientLevel> {
	public final Map<PropListType, Map<RenderLevelStageEvent.Stage, List<Prop>>> renderedPropLists;

	public ClientProps(ClientLevel level) {
		super(level);
		this.propLists.put(PropListType.ASSETS, new PropList(this, PropListType.ASSETS));
		this.renderedPropLists = new EnumMap<>(PropListType.class);
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
			renderedProps.computeIfAbsent(stage, k -> new ArrayList<>()).add(prop);
		}

		prop.cachedRenderer = null;
	}

	@Override
	protected void onRemoved(Prop prop) {
		var renderedProps = renderedPropLists.get(prop.spawnType.listType);

		if (renderedProps != null && !renderedProps.isEmpty()) {
			for (var stage : getRenderer(prop).getStages(Cast.to(prop))) {
				var list = renderedProps.get(stage);

				if (list != null) {
					list.remove(prop);
				}
			}
		}

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

			for (var prop : list) {
				double x = KMath.lerp(frame.worldDelta(), prop.prevPos.x, prop.pos.x);
				double y = KMath.lerp(frame.worldDelta(), prop.prevPos.y, prop.pos.y);
				double z = KMath.lerp(frame.worldDelta(), prop.prevPos.z, prop.pos.z);
				double r = prop.getMaxRenderDistance();

				if (r >= Double.MAX_VALUE || cam.distanceToSqr(x, y + prop.height / 2D, z) <= r * r) {
					if (prop.isVisible(x, y, z, frame)) {
						var renderer = getRenderer(prop);

						if (renderer != PropRenderer.INVISIBLE) {
							ms.pushPose();
							ms.translate(frame.x(x), frame.y(y), frame.z(z));
							renderer.renderProp(Cast.to(prop), frame);
							ms.popPose();
						}
					}
				}
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
