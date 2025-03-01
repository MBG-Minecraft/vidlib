package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

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

	void render(T shape, Minecraft mc, RenderLevelStageEvent event, float delta, Color color, Color outlineColor);
}
