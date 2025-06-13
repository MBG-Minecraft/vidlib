package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PropList implements Iterable<Prop> {
	public final Props<?> props;
	public final PropListType type;
	private final Int2ObjectMap<Prop> map;
	public final List<Prop> pending;
	public final Map<PropRemoveType, IntList> removed;

	public PropList(Props<?> props, PropListType type) {
		this.props = props;
		this.type = type;
		this.map = new Int2ObjectOpenHashMap<>();
		this.pending = new ArrayList<>();
		this.removed = new EnumMap<>(PropRemoveType.class);

		for (var removeType : PropRemoveType.values()) {
			removed.put(removeType, new IntArrayList());
		}
	}

	private boolean fullTick(Prop prop) {
		if (prop.fullTick(props.level.getGameTime())) {
			prop.onRemoved();
			props.onRemoved(prop);
			removed.get(prop.removed).add(prop.id);
			return true;
		}

		return false;
	}

	public void tick(@Nullable S2CPacketBundleBuilder updates) {
		if (!map.isEmpty()) {
			map.values().removeIf(this::fullTick);
		}

		if (updates != null) {
			for (var prop : map.values()) {
				var update = prop.createUpdatePacket();

				if (update != null) {
					updates.s2c(update);
				}
			}
		}

		for (var entry : removed.entrySet()) {
			var list = entry.getValue();

			if (!list.isEmpty()) {
				if (updates != null) {
					updates.s2c(new RemovePropsPayload(type, new IntArrayList(list), entry.getKey()));
				}

				list.clear();
			}
		}

		if (!pending.isEmpty()) {
			for (var prop : pending) {
				if (prop.id == 0) {
					prop.id = generateNewId();
				} else {
					var old = map.get(prop.id);

					if (old != null) {
						old.snap();
						old.removed = PropRemoveType.REPLACED;
					}
				}

				map.put(prop.id, prop);
				prop.onAdded();
				props.onAdded(prop);
				prop.snap();

				if (updates != null) {
					for (var data : prop.type.data().values()) {
						prop.sync(data);
					}

					updates.s2c(prop.createAddPacket());
				}
			}

			pending.clear();
		}
	}

	public void removeAll() {
		for (var prop : map.values()) {
			prop.remove();
		}
	}

	public void removeAll(Predicate<Prop> predicate) {
		for (var prop : this) {
			if (predicate.test(prop)) {
				prop.remove();
			}
		}
	}

	public int generateNewId() {
		int id = 0;

		while (id == 0 || map.containsKey(id)) {
			id = props.level.random.nextInt() & 0x7FFFFFFF;
		}

		return id;
	}

	@Nullable
	public Prop get(int id) {
		return map.get(id);
	}

	@Override
	public @NotNull Iterator<Prop> iterator() {
		return map.values().iterator();
	}

	public Collection<Prop> all() {
		return map.values();
	}

	public int size() {
		return map.size();
	}
}
