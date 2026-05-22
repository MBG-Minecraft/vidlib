package dev.latvian.mods.vidlib.feature.registry;

@FunctionalInterface
public interface SimpleRegistryCollector<T> {
	void register(SimpleRegistryType<? extends T> type);
}
