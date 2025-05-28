package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.vidlib.util.Cast;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientPropList extends PropList<ClientLevel> {
	public final Map<RenderLevelStageEvent.Stage, List<Prop>> renderedProps;

	public ClientPropList(ClientLevel level) {
		super(level);
		this.renderedProps = new Reference2ObjectOpenHashMap<>();
	}

	public PropRenderer<?> getRenderer(Prop prop) {
		if (prop.cachedRenderer == null) {
			prop.cachedRenderer = PropRenderer.ALL.get().getOrDefault(prop.type, PropRenderer.INVISIBLE);
		}

		return (PropRenderer<?>) prop.cachedRenderer;
	}

	@Override
	protected void onAdded(Prop prop) {
		for (var stage : getRenderer(prop).getStages(Cast.to(prop))) {
			renderedProps.computeIfAbsent(stage, k -> new ArrayList<>()).add(prop);
		}
	}

	@Override
	protected void onRemoved(Prop prop) {
		for (var stage : getRenderer(prop).getStages(Cast.to(prop))) {
			var list = renderedProps.get(stage);

			if (list != null) {
				list.remove(prop);
			}
		}

		prop.cachedRenderer = null;
	}

	public void renderAll(FrameInfo frame) {
		var list = renderedProps.get(frame.stage());

		if (list == null || list.isEmpty()) {
			return;
		}

		var ms = frame.poseStack();

		for (var prop : list) {
			ms.pushPose();
			ms.translate(
				frame.x(KMath.lerp(frame.worldDelta(), prop.prevPos.x, prop.pos.x)),
				frame.y(KMath.lerp(frame.worldDelta(), prop.prevPos.y, prop.pos.y)),
				frame.z(KMath.lerp(frame.worldDelta(), prop.prevPos.z, prop.pos.z))
			);
			getRenderer(prop).renderProp(Cast.to(prop), frame);
			ms.popPose();
		}
	}
}
