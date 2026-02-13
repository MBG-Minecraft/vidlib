package dev.latvian.mods.vidlib.feature.data;

import org.jetbrains.annotations.Nullable;

public interface DataMapHolder {
	@Nullable
	DataMap getDataMap();

	@Nullable
	default <T> T getOptional(DataKey<T> type) {
		var dataMap = getDataMap();
		return dataMap == null ? null : dataMap.get(type);
	}

	default <T> T get(DataKey<T> type) {
		var value = getOptional(type);
		return value == null ? type.defaultValue() : value;
	}

	default <T> void set(DataKey<T> type, T value) {
		var dataMap = getDataMap();

		if (dataMap != null) {
			dataMap.set(type, value);
		}
	}

	default <T> void reset(DataKey<T> type) {
		var dataMap = getDataMap();

		if (dataMap != null) {
			dataMap.reset(type);
		}
	}
}
