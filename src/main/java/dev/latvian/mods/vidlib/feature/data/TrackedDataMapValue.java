package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;

public class TrackedDataMapValue<K> {
	public final DataKey<K, ?> dataKey;
	public Object data = null;
	public int save = -1;
	public int sync = -1;
	public int changeCount = 0;

	public TrackedDataMapValue(DataKey<K, ?> dataKey) {
		this.dataKey = dataKey;
	}

	public void setChanged() {
		if (changeCount == Integer.MAX_VALUE) {
			changeCount = 0;
		} else {
			changeCount++;
		}
	}

	public void update(GlobalSessionData worldData, K key, Object update) {
		data = update;

		if (!dataKey.onReceived.isEmpty()) {
			for (var listener : dataKey.onReceived) {
				listener.onUpdate(worldData, key, Cast.to(data));
			}
		}
	}
}
