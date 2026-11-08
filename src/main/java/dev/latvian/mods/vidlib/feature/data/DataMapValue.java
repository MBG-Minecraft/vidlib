package dev.latvian.mods.vidlib.feature.data;

public record DataMapValue<K>(DataKey<K, ?> key, Object value) {
}
