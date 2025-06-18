package dev.latvian.mods.vidlib.feature.registry;

import java.util.Optional;
import java.util.function.Supplier;

public class BasicRegistryRef<K, V> implements Supplier<V> {
	final K id;
	V value;

	BasicRegistryRef(K id) {
		this.id = id;
	}

	BasicRegistryRef(K id, V value) {
		this.id = id;
		this.value = value;
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + id + ":" + value + "]";
	}
}
