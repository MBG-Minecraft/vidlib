package dev.latvian.mods.vidlib.feature.prop;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;

public final class ReplayProp {
	public static Int2ObjectMap<ReplayProp> MAP = null;
	public static List<ReplayProp> LIST = null;

	public final int id;
	public final PropType<?> type;
	public long spawn;
	public long remove;
	public final Map<PropData<?, ?>, Object> data;
	public boolean manuallyAdded;

	public ReplayProp(int id, PropType<?> type) {
		this.id = id;
		this.type = type;
		this.spawn = 0L;
		this.remove = 0L;
		this.data = new Reference2ObjectOpenHashMap<>();
		this.manuallyAdded = false;
	}

	public boolean exists(long now) {
		return now >= spawn && now < remove;
	}

	@Override
	public String toString() {
		return "ReplayProp[" +
			"id=" + id + ", " +
			"type=" + type + ", " +
			"spawn=" + spawn + ", " +
			"remove=" + remove + ", " +
			"data=" + data + ']';
	}
}
