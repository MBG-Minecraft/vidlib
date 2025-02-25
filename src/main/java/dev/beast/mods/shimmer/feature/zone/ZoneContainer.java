package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.util.IdentityKey;

import java.util.IdentityHashMap;
import java.util.Map;

public class ZoneContainer {
	public final Map<IdentityKey, ZoneInstance> zones;

	public ZoneContainer() {
		this.zones = new IdentityHashMap<>();
	}

	public ZoneContainer add(String id, Zone zone) {
		var instance = new ZoneInstance(IdentityKey.create(id), zone);
		zones.put(instance.id, instance);
		return this;
	}
}
