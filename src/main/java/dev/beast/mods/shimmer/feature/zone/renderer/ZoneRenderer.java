package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.Cast;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public interface ZoneRenderer<T extends ZoneShape> {
	Map<SimpleRegistryType<?>, ZoneRenderer<?>> RENDERERS = new IdentityHashMap<>();

	static void register(SimpleRegistryType<?> type, ZoneRenderer<?> renderer) {
		RENDERERS.put(type, renderer);
	}

	static ZoneRenderer<?> get(SimpleRegistryType<?> type) {
		var renderer = RENDERERS.get(type);
		return renderer == null ? BoxZoneRenderer.INSTANCE : renderer;
	}

	static void renderAll(@Nullable ZoneContainer container, Minecraft mc, RenderLevelStageEvent event) {
		if (container != null && mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			for (var instance : container.zones) {
				var renderer = get(instance.zone.shape().type());

				if (renderer != EmptyZoneRenderer.INSTANCE) {
					renderer.render(Cast.to(instance.zone.shape()), mc, event, instance.zone.color().withAlpha(0.2F), instance.entities.isEmpty() ? Color.WHITE : Color.GREEN);
				}
			}
		}
	}

	void render(T shape, Minecraft mc, RenderLevelStageEvent event, Color color, Color outlineColor);
}
