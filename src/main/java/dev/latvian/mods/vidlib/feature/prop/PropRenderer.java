package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.util.Lazy;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.client.renderer.LightTexture;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;
import java.util.Set;

public interface PropRenderer<P extends Prop> {
	Set<RenderLevelStageEvent.Stage> DEFAULT_STAGES = Set.of(RenderLevelStageEvent.Stage.AFTER_ENTITIES);

	PropRenderer<?> INVISIBLE = new PropRenderer<>() {
		@Override
		public void renderProp(Prop prop, FrameInfo frame) {
		}

		@Override
		public Set<RenderLevelStageEvent.Stage> getStages(Prop prop) {
			return Set.of();
		}

		@Override
		public double getRenderDistance(Prop prop) {
			return 0D;
		}

		@Override
		public boolean isVisible(Prop prop, FrameInfo frame) {
			return false;
		}
	};

	record Holder(PropType<?> type, PropRenderer<?> renderer) {
	}

	Lazy<Map<PropType<?>, PropRenderer<?>>> ALL = Lazy.identityMap(map -> {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof Holder(PropType<?> type, PropRenderer<?> renderer)) {
				map.put(type, renderer);
			}
		}
	});

	void renderProp(P prop, FrameInfo frame);

	default Set<RenderLevelStageEvent.Stage> getStages(P prop) {
		return DEFAULT_STAGES;
	}

	default int getPackedLight(P prop) {
		return LightTexture.FULL_BRIGHT;
	}

	default double getRenderDistance(P prop) {
		return 8192D;
	}

	default boolean isVisible(P prop, FrameInfo frame) {
		double w = prop.width / 2D;
		double minX = prop.pos.x - w;
		double minY = prop.pos.y;
		double minZ = prop.pos.z - w;
		double maxX = prop.pos.x + w;
		double maxY = prop.pos.y + prop.height;
		double maxZ = prop.pos.z + w;
		double d = getRenderDistance(prop);
		return frame.distanceSq(minX, minY, minZ, maxX, maxY, maxZ) <= d * d && frame.isVisible(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
