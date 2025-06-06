package dev.latvian.mods.vidlib.feature.prop;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class PropList implements Iterable<Prop> {
	public final Props<?> props;
	public final PropListType type;
	private final Int2ObjectMap<Prop> map;
	public final IntList removed;
	public int nextId;

	public PropList(Props<?> props, PropListType type) {
		this.props = props;
		this.type = type;
		this.map = new Int2ObjectOpenHashMap<>();
		this.removed = new IntArrayList();
		this.nextId = 0;
	}

	public int nextId() {
		return ++nextId;
	}

	public void tick() {
		if (!map.isEmpty()) {
			map.values().removeIf(Prop::fullTick);
		}
	}

	public void reset() {
		for (var prop : map.values()) {
			prop.onRemoved();
			props.onRemoved(prop);
		}

		map.clear();
		nextId = 0;
	}

	public void add(Prop prop) {
		if (prop.id == 0) {
			prop.id = nextId();
		}

		map.put(prop.id, prop);
	}

	@Nullable
	public Prop get(int id) {
		return map.get(id);
	}

	@Override
	public @NotNull Iterator<Prop> iterator() {
		return map.values().iterator();
	}
}
