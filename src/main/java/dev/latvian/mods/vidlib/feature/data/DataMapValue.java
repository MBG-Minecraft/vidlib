package dev.latvian.mods.vidlib.feature.data;

public record DataMapValue(DataKey<?> key, Object value) {
	public static final DataMapValue INVALID = new DataMapValue(null, null);
}
