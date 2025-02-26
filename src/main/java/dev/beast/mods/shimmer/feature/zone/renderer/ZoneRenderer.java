package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.feature.zone.ZoneShapeType;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public interface ZoneRenderer<T extends ZoneShape> {
	Map<ZoneShapeType<?>, Function<ZoneInstance, ZoneRenderer<?>>> RENDERERS = new IdentityHashMap<>();

	static void register(ZoneShapeType<?> type, Function<ZoneInstance, ZoneRenderer<?>> renderer) {
		RENDERERS.put(type, renderer);
	}

	static ZoneRenderer<?> get(ZoneInstance instance) {
		var func = RENDERERS.get(instance.zone.shape().type());
		return func == null ? BoxZoneRenderer.INSTANCE : func.apply(instance);
	}

	static void renderAll(@Nullable ZoneContainer container, Minecraft mc, RenderLevelStageEvent event) {
		if (container != null && mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			for (var instance : container.zones) {
				var renderer = get(instance);

				if (renderer != EmptyZoneRenderer.INSTANCE) {
					renderer.render(Cast.to(instance.zone.shape()), instance, mc, event);
				}
			}
		}
	}

	void render(T shape, ZoneInstance instance, Minecraft mc, RenderLevelStageEvent event);
}
