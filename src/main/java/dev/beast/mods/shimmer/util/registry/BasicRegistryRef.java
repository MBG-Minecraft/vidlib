package dev.beast.mods.shimmer.util.registry;

import java.util.Optional;
import java.util.function.Supplier;

public class BasicRegistryRef<K, V> implements Supplier<V> {
	final K id;
	V value;

	BasicRegistryRef(K id) {
		this.id = id;
	}

	public K id() {
		return id;
	}

	@Override
	public V get() {
		if (value == null) {
			throw new NullPointerException("Value for " + id + " is null");
		}

		return value;
	}

	public Optional<V> optional() {
		return Optional.ofNullable(value);
	}

	public boolean isSet() {
		return value != null;
	}
}
