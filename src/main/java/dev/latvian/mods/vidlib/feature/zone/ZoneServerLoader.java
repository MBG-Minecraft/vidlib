package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.Map;

public class ZoneServerLoader extends JsonRegistryReloadListener<Zone> {
	public ZoneServerLoader() {
		super("vidlib/zone", Zone.REGISTRY);
	}

	@Override
	protected void apply(ResourceManager resourceManager, Map<Identifier, Zone> map) {
		var entries = new ArrayList<Map.Entry<String, Zone>>(map.size());

		for (var entry : map.entrySet()) {
			entries.add(Map.entry(entry.getKey().getPath(), entry.getValue()));
		}

		VLPlatformHelper.INSTANCE.generateZones((id, zone) -> entries.add(Map.entry(id, zone)));
		Zone.REGISTRY.updateValues(entries);
	}
}
