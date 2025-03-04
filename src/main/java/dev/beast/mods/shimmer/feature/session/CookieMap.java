package dev.beast.mods.shimmer.feature.session;

import java.util.IdentityHashMap;
import java.util.Map;

public class CookieMap {
	private Map<CookieType<?>, Cookie> map;

	@SuppressWarnings({"ReassignedVariable", "unchecked"})
	public <T extends Cookie> T get(CookieType<T> type) {
		if (map == null) {
			map = new IdentityHashMap<>(1);
		}

		var cookie = map.get(type);

		if (cookie == null) {
			cookie = type.factory().get();
			map.put(type, cookie);
		}

		return (T) cookie;
	}
}
