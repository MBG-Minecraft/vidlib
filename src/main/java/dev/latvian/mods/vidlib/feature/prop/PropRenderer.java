package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
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

	default boolean shouldSort() {
		return false;
	}
}
