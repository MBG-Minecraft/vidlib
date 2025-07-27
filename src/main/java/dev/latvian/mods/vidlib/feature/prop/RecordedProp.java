package dev.latvian.mods.vidlib.feature.prop;

import com.google.gson.JsonObject;

public record RecordedProp(int id, PropType<?> type, long spawn, long remove, JsonObject data) {
	public RecordedProp finish(long remove) {
		return new RecordedProp(id, type, spawn, remove, data);
	}

	public boolean exists(long now) {
		return now >= spawn && now < remove;
	}
}
