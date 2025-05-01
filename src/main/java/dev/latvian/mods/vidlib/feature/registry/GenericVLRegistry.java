package dev.latvian.mods.vidlib.feature.registry;

import dev.latvian.mods.vidlib.util.Side;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GenericVLRegistry<K, V> implements Iterable<V> {
	static final List<GenericVLRegistry<?, ?>> DATA_PACK_HOLDERS = new ArrayList<>();

	public static void releaseServerHolders() {
		for (var holder : DATA_PACK_HOLDERS) {
			holder.release();
		}
	}

	public static <K, V> GenericVLRegistry<K, V> createServer() {
		var holder = new GenericVLRegistry<K, V>(Side.SERVER);
		DATA_PACK_HOLDERS.add(holder);
		return holder;
	}

	public static <K, V> GenericVLRegistry<K, V> createClient() {
		return new GenericVLRegistry<>(Side.CLIENT);
	}

	private final Side side;
	final Map<K, BasicRegistryRef<K, V>> refMap;
	protected Map<K, V> map;
	protected Map<V, K> reverseMap;

	GenericVLRegistry(Side side) {
		this.side = side;
		this.refMap = new HashMap<>();
		this.map = Map.of();
		this.reverseMap = Map.of();
	}

	public Side getSide() {
		return side;
	}

	@Override
	@NotNull
	public Iterator<V> iterator() {
		return map.values().iterator();
	}

	public Map<K, V> getMap() {
		return map;
	}

	public synchronized BasicRegistryRef<K, V> ref(K id) {
		var ref = refMap.get(id);

		if (ref == null) {
			ref = new BasicRegistryRef<>(id);
			ref.value = map.get(id);
			refMap.put(id, ref);
		}

		return ref;
	}

	@Nullable
	public V get(K id) {
		return map.get(id);
	}

	public V get(K id, V def) {
		return map.getOrDefault(id, def);
	}

	public K getId(V value) {
		return reverseMap.get(value);
	}

	public synchronized void update(Map<K, V> values) {
		map = values;

		for (var ref : refMap.values()) {
			ref.value = values.get(ref.id);
		}

		if (values.isEmpty()) {
			reverseMap = Map.of();
		} else {
			reverseMap = new Reference2ObjectOpenHashMap<>(map.size());

			for (var entry : map.entrySet()) {
				reverseMap.put(entry.getValue(), entry.getKey());
			}
		}
	}

	public void release() {
		update(Map.of());
	}
}