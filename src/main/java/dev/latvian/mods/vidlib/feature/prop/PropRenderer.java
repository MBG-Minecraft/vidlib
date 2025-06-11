package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import net.minecraft.client.renderer.LightTexture;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;
import java.util.Set;

public interface PropRenderer<P extends Prop> {
	Set<RenderLevelStageEvent.Stage> DEFAULT_STAGES = Set.of(
		RenderLevelStageEvent.Stage.AFTER_ENTITIES
	);

	Set<RenderLevelStageEvent.Stage> STRUCTURE_STAGES = Set.of(
		RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS,
		RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS,
		RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS,
		RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS,
		RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS
	);

	Set<RenderLevelStageEvent.Stage> SOLID_STRUCTURE_STAGES = Set.of(
		RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS
	);

	PropRenderer<?> INVISIBLE = new PropRenderer<>() {
		@Override
		public void render(PropRenderContext<Prop> ctx) {
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

	void render(PropRenderContext<P> ctx);

	default Set<RenderLevelStageEvent.Stage> getStages(P prop) {
		return DEFAULT_STAGES;
	}

	default int getPackedLight(PropRenderContext<P> ctx) {
		return LightTexture.FULL_BRIGHT;
	}

	default boolean shouldSort(PropRenderContext<P> ctx) {
		return false;
	}
}
