package dev.latvian.mods.vidlib.feature.prop;

import java.util.Map;

public record RecordedProp(int id, PropType<?> type, long spawn, long remove, Map<PropData<?, ?>, Object> data) {
	public RecordedProp finish(long remove) {
		return new RecordedProp(id, type, spawn, remove, data);
	}

	public boolean exists(long now) {
		return now >= spawn && now < remove;
	}
}
