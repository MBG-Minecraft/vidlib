package dev.beast.mods.shimmer.util.registry;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class RegistryReference<K, V> implements Supplier<V> {
	private static final List<Holder<?, ?>> DATA_PACK_HOLDERS = new ArrayList<>();

	public static <K, V> Holder<K, V> createRuntimeHolder() {
		return new Holder<>(Holder.Type.RUNTIME);
	}

	public static <K, V> Holder<K, V> createServerHolder() {
		var holder = new Holder<K, V>(Holder.Type.SERVER);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <K, V> Holder<K, V> createClientHolder() {
		return new Holder<>(Holder.Type.CLIENT);
	}

	public static void releaseServerHolders() {
		for (var holder : DATA_PACK_HOLDERS) {
			holder.release();
		}
	}

	public static class Holder<K, V> {
		public enum Type {
			RUNTIME,
			SERVER,
			CLIENT
		}

		private final Type type;
		private final Map<K, RegistryReference<K, V>> refMap;
		private Map<K, V> map;

		private Holder(Type type) {
			this.type = type;
			this.refMap = new HashMap<>();
			this.map = Map.of();
		}

		public Type getType() {
			return type;
		}

		public Map<K, V> getMap() {
			return map;
		}

		public RegistryReference<K, V> reference(K id) {
			var ref = refMap.get(id);

			if (ref == null) {
				ref = new RegistryReference<>(id);
				ref.value = map.get(id);
				refMap.put(id, ref);
			}

			return ref;
		}

		@Nullable
		public V get(K id) {
			return map.get(id);
		}

		public void update(Map<K, V> values) {
			this.map = values;

			for (var ref : refMap.values()) {
				ref.value = values.get(ref.id);
			}
		}

		public void release() {
			update(Map.of());
		}
	}

	private final K id;
	private V value;

	private RegistryReference(K id) {
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
