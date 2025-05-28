package dev.latvian.mods.vidlib.feature.prop;

import java.util.HashMap;
import java.util.Map;

public interface PropDataProvider {
	record Fixed(Map<String, PropData<?, ?>> map) implements PropDataProvider {
		@Override
		public Map<String, PropData<?, ?>> data() {
			return map;
		}
	}

	static PropDataProvider join(PropDataProvider... providers) {
		var map = new HashMap<String, PropData<?, ?>>();

		for (var provider : providers) {
			map.putAll(provider.data());
		}

		return new Fixed(Map.copyOf(map));
	}

	Map<String, PropData<?, ?>> data();
}
