package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.Zone;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.feature.zone.ZoneType;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public interface ZoneRenderer<T extends Zone> {
	Map<ZoneType<?>, Function<ZoneInstance, ZoneRenderer<?>>> RENDERERS = new IdentityHashMap<>();

	static void register(ZoneType<?> type, Function<ZoneInstance, ZoneRenderer<?>> renderer) {
		RENDERERS.put(type, renderer);
	}

	static ZoneRenderer<?> get(ZoneInstance instance) {
		var func = RENDERERS.get(instance.zone.type());
		return func == null ? BoxZoneRenderer.INSTANCE : func.apply(instance);
	}

	void render(T zone, ZoneInstance instance, Minecraft mc, RenderLevelStageEvent event);
}
